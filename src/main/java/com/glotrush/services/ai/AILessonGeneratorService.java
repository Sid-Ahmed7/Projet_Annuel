package com.glotrush.services.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.glotrush.dto.request.LessonRequest;
import com.glotrush.dto.request.exercice.InteractiveQuestionRequest;
import com.glotrush.dto.request.lesson.FlashcardLessonRequest;
import com.glotrush.dto.request.lesson.MatchingPairLessonRequest;
import com.glotrush.dto.request.lesson.QcmLessonRequest;
import com.glotrush.dto.request.lesson.SortingExerciseLessonRequest;
import com.glotrush.dto.request.lesson.InteractiveLessonRequest;
import com.glotrush.dto.response.LessonResponse;
import com.glotrush.entities.Language;
import com.glotrush.entities.Lesson;
import com.glotrush.entities.Topic;
import com.glotrush.entities.ai.AIGenerationLog;
import com.glotrush.enumerations.LessonType;
import com.glotrush.mapping.LessonEntityToLessonResponse;
import com.glotrush.repositories.LessonRepository;
import com.glotrush.repositories.TopicRepository;
import com.glotrush.repositories.ai.AIGenerationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AILessonGeneratorService implements IAILessonGeneratorService {

    private final IAIService aiService;
    private final AIGenerationLogRepository logRepository;
    private final TopicRepository topicRepository;
    private final LessonRepository lessonRepository;
    private final LessonEntityToLessonResponse lessonEntityToLessonResponse;
    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;
    private final AIQuotaService aiQuotaService;

    @Override
    @Transactional
    public LessonRequest generateLesson(UUID accountId, UUID topicId, LessonType lessonType, String description, Integer itemCount) {
        aiQuotaService.verifyAndConsumeAIQuota(accountId);

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found: " + topicId));

        Class<? extends LessonRequest> targetClass = getTargetClass(lessonType);
        
        String systemPrompt = buildSystemPrompt(lessonType, targetClass, topic.getSourceLanguage(), topic.getTargetLanguage(), itemCount);
        String userPrompt = "Sujet de la leçon : " + description + "\nNom du topic : " + topic.getName();
        
        String fullPrompt = systemPrompt + "\n\nDescription de l'utilisateur : " + userPrompt;

        log.info("Génération d'une leçon de type {} pour l'utilisateur {}", lessonType, accountId);

        LessonRequest generatedRequest = aiService.generateJsonContent(fullPrompt, targetClass);
        
        limitLessonRequestItemCountToRequestedSize(generatedRequest, itemCount);
        enforceEmptyMediaLists(generatedRequest);
        
        generatedRequest.setTopicId(topicId);
        generatedRequest.setLessonType(lessonType);
        
        if (generatedRequest.getIsActive() == null) {
            generatedRequest.setIsActive(true);
        }

        saveLog(accountId, lessonType, fullPrompt, generatedRequest);

        return generatedRequest;
    }

    @Override
    @Transactional
    public LessonRequest modifyLesson(UUID accountId, UUID lessonId, String prompt, Integer itemCount, LessonRequest currentLessonRequest) {
        aiQuotaService.verifyAndConsumeAIQuota(accountId);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found: " + lessonId));
        
        Topic topic = lesson.getTopic();
        LessonType lessonType = currentLessonRequest.getLessonType();
        Class<? extends LessonRequest> targetClass = getTargetClass(lessonType);

        String currentLessonJson;
        try {
            currentLessonJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(currentLessonRequest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing current lesson request", e);
        }

        StringBuilder systemPromptBuilder = new StringBuilder(buildSystemPrompt(lessonType, targetClass, topic.getSourceLanguage(), topic.getTargetLanguage(), itemCount));
        systemPromptBuilder.append("\n--- MODE MODIFICATION ---\n");
        systemPromptBuilder.append("Tu dois modifier une leçon existante en te basant sur son contenu actuel et les instructions de l'utilisateur.\n");
        systemPromptBuilder.append("Voici le contenu actuel de la leçon (format JSON) :\n");
        systemPromptBuilder.append(currentLessonJson).append("\n\n");
        systemPromptBuilder.append("CONSIGNES DE MODIFICATION :\n");
        systemPromptBuilder.append("- Garde la structure globale mais applique les changements demandés.\n");
        systemPromptBuilder.append("- Ne change PAS le topicId ni le lessonType.\n");
        systemPromptBuilder.append("- Retourne le JSON complet mis à jour.\n");

        String fullPrompt = systemPromptBuilder.toString() + "\n\nInstructions de modification de l'utilisateur : " + prompt;

        log.info("Modification via IA d'une leçon {} ({}) pour l'utilisateur {}", lessonType, lessonId, accountId);

        LessonRequest modifiedRequest = aiService.generateJsonContent(fullPrompt, targetClass);

        limitLessonRequestItemCountToRequestedSize(modifiedRequest, itemCount);
        enforceEmptyMediaLists(modifiedRequest);

        modifiedRequest.setTopicId(topic.getId());
        modifiedRequest.setLessonType(lessonType);
        
        if (modifiedRequest.getIsActive() == null) {
            modifiedRequest.setIsActive(lesson.getIsActive());
        }

        saveLog(accountId, lessonType, fullPrompt, modifiedRequest);

        return modifiedRequest;
    }

    private Class<? extends LessonRequest> getTargetClass(LessonType lessonType) {
        return switch (lessonType) {
            case QCM -> QcmLessonRequest.class;
            case FLASHCARD -> FlashcardLessonRequest.class;
            case MATCHING_PAIR -> MatchingPairLessonRequest.class;
            case SORTING_EXERCISE -> SortingExerciseLessonRequest.class;
            case INTERACTIVE -> InteractiveLessonRequest.class;
        };
    }

    private String buildSystemPrompt(LessonType lessonType, Class<?> targetClass, Language sourceLang, Language targetLang, Integer itemCount) {
        String exampleJson = getExampleJson(lessonType);
        
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Tu es un expert en pédagogie et en apprentissage des langues.\n");
        promptBuilder.append("Ta tâche est de générer une leçon au format JSON strict.\n");
        promptBuilder.append("Le type de leçon demandé est : ").append(lessonType).append(".\n");
        promptBuilder.append("Tu DOIS respecter scrupuleusement la structure de la classe Java suivante : ").append(targetClass.getSimpleName()).append(".\n\n");
        
        promptBuilder.append("Contexte linguistique :\n");
        promptBuilder.append("- L'utilisateur parle : ").append(sourceLang.getName()).append(" (Langue source).\n");
        promptBuilder.append("- L'utilisateur apprend : ").append(targetLang.getName()).append(" (Langue cible).\n");
        promptBuilder.append("- Génère le contenu en conséquence. Par exemple, pour un QCM, les questions peuvent être en ").append(sourceLang.getName());
        promptBuilder.append(" ou ").append(targetLang.getName()).append(" selon le contexte pédagogique, mais les réponses et explications doivent aider à l'apprentissage du ").append(targetLang.getName()).append(".\n\n");

        if (itemCount != null) {
            String elementsDescription = "éléments";
            if (lessonType == LessonType.QCM) {
                elementsDescription = "questions";
            } else if (lessonType == LessonType.FLASHCARD) {
                elementsDescription = "flashcards";
            } else if (lessonType == LessonType.MATCHING_PAIR) {
                elementsDescription = "paires à associer";
            } else if (lessonType == LessonType.SORTING_EXERCISE) {
                elementsDescription = "phrases à ordonner";
            } else if (lessonType == LessonType.INTERACTIVE) {
                elementsDescription = "questions interactives";
            }
            promptBuilder.append("Quantité : Tu dois générer exactement ").append(itemCount).append(" ").append(elementsDescription).append(".\n");
            promptBuilder.append("ATTENTION : La liste d'éléments générés doit contenir STRICTEMENT ").append(itemCount).append(" ").append(elementsDescription).append(". C'est une contrainte absolue.\n\n");
        }

        if (lessonType == LessonType.INTERACTIVE) {
            promptBuilder.append("Consignes de structure pour le type INTERACTIVE :\n");
            promptBuilder.append("- Pour chaque question, le champ \"systemType\" doit valoir SOIT \"MULTIPLE_CHOICE\", SOIT \"OPEN_TEXT\". Ne jamais utiliser de valeur comme \"MATCHING\" ou tout autre nom.\n");
            promptBuilder.append("- Si \"systemType\" est \"MULTIPLE_CHOICE\" : tu dois fournir un tableau de 2 à 4 choix dans \"options\", et l'index de la bonne réponse dans \"correctOptionIndex\" (de 0 à 3). Le champ \"correctWord\" doit être null.\n");
            promptBuilder.append("- Si \"systemType\" est \"OPEN_TEXT\" : tu dois fournir le mot attendu dans \"correctWord\". Le champ \"options\" doit être vide ou null, et \"correctOptionIndex\" doit être null.\n");
            promptBuilder.append("- Les listes \"imagePaths\" et \"audioPaths\" DOIVENT impérativement être vides (c'est-à-dire []). Ne génère aucun nom de fichier image ou audio.\n\n");
        }

        promptBuilder.append("Voici un exemple du format JSON attendu pour ce type :\n");
        promptBuilder.append(exampleJson).append("\n\n");
        
        promptBuilder.append("Instructions importantes :\n");
        promptBuilder.append("- Le champ \"lessonType\" est OBLIGATOIRE et doit valoir exactement : \"").append(lessonType).append("\".\n");
        promptBuilder.append("- Ne retourne QUE le JSON, aucun texte avant ou après.\n");
        promptBuilder.append("- N'ajoute PAS de champs qui ne sont pas dans l'exemple.\n");
        promptBuilder.append("- Utilise le format camelCase pour les noms de champs.\n");
        promptBuilder.append("- Assure-toi que le contenu est éducatif et de haute qualité.\n");
        promptBuilder.append("- Pour les QCM, fournis des options plausibles et une explication claire.\n");
        promptBuilder.append("- Pour les Flashcards, assure-toi que le recto et le verso sont pertinents.\n");

        return promptBuilder.toString();
    }

    private String getExampleJson(LessonType lessonType) {
        return switch (lessonType) {
            case QCM -> """
                {
                  "lessonType": "QCM",
                  "title": "Titre de la leçon",
                  "description": "Description de la leçon",
                  "isActive": true,
                  "questions": [
                    {
                      "question": "Quelle est la traduction de 'Apple' ?",
                      "options": ["Pomme", "Poire", "Banane", "Fraise"],
                      "correctOptionIndex": 0,
                      "explanation": "Apple signifie Pomme en français."
                    }
                  ]
                }
                """;
            case FLASHCARD -> """
                {
                  "lessonType": "FLASHCARD",
                  "title": "Titre de la leçon",
                  "description": "Description de la leçon",
                  "isActive": true,
                  "flashcards": [
                    {
                      "front": "Apple",
                      "back": "Pomme",
                      "frontLanguage": "English",
                      "backLanguage": "French"
                    }
                  ]
                }
                """;
            case MATCHING_PAIR -> """
                {
                  "lessonType": "MATCHING_PAIR",
                  "title": "Titre de la leçon",
                  "description": "Description de la leçon",
                  "isActive": true,
                  "matchingPairs": [
                    {
                      "item1": "Apple",
                      "item2": "Pomme"
                    }
                  ]
                }
                """;
            case SORTING_EXERCISE -> """
                {
                  "lessonType": "SORTING_EXERCISE",
                  "title": "Titre de la leçon",
                  "description": "Description de la leçon",
                  "isActive": true,
                  "sortingExercise": [
                    {
                      "items": ["I", "love", "apples"],
                      "correctOrder": [0, 1, 2]
                    }
                  ]
                }
                """;
            case INTERACTIVE -> """
                {
                  "lessonType": "INTERACTIVE",
                  "title": "Titre de la leçon",
                  "description": "Description de la leçon",
                  "isActive": true,
                  "questions": [
                    {
                      "questionText": "Écoutez et choisissez le bon mot",
                      "imagePaths": [],
                      "audioPaths": [],
                      "systemType": "MULTIPLE_CHOICE",
                      "options": ["Pomme", "Poire", "Banane"],
                      "correctOptionIndex": 0
                    }
                  ]
                }
                """;
        };
    }

    private void saveLog(UUID accountId, LessonType lessonType, String prompt, LessonRequest response) {
        try {
            String jsonResponse = objectMapper.writeValueAsString(response);
            AIGenerationLog logEntry = AIGenerationLog.builder()
                    .accountId(accountId)
                    .lessonType(lessonType)
                    .userPrompt(prompt)
                    .generatedResponse(jsonResponse)
                    .build();
            logRepository.save(logEntry);
        } catch (JsonProcessingException e) {
            log.warn("Impossible de sérialiser la réponse AI pour le log", e);
        }
    }

    private void limitLessonRequestItemCountToRequestedSize(LessonRequest lessonRequest, Integer itemCount) {
        if (itemCount == null) {
            return;
        }

        // Limiter la taille des listes d'exercices au quota requis en cas de sur-génération par le modèle d'IA.
        if (lessonRequest instanceof QcmLessonRequest qcmLessonRequest) {
            if (qcmLessonRequest.getQuestions() != null && qcmLessonRequest.getQuestions().size() > itemCount) {
                qcmLessonRequest.setQuestions(new ArrayList<>(qcmLessonRequest.getQuestions().subList(0, itemCount)));
            }
        } else if (lessonRequest instanceof FlashcardLessonRequest flashcardLessonRequest) {
            if (flashcardLessonRequest.getFlashcards() != null && flashcardLessonRequest.getFlashcards().size() > itemCount) {
                flashcardLessonRequest.setFlashcards(new ArrayList<>(flashcardLessonRequest.getFlashcards().subList(0, itemCount)));
            }
        } else if (lessonRequest instanceof MatchingPairLessonRequest matchingPairLessonRequest) {
            if (matchingPairLessonRequest.getMatchingPairs() != null && matchingPairLessonRequest.getMatchingPairs().size() > itemCount) {
                matchingPairLessonRequest.setMatchingPairs(new ArrayList<>(matchingPairLessonRequest.getMatchingPairs().subList(0, itemCount)));
            }
        } else if (lessonRequest instanceof SortingExerciseLessonRequest sortingExerciseLessonRequest) {
            if (sortingExerciseLessonRequest.getSortingExercise() != null && sortingExerciseLessonRequest.getSortingExercise().size() > itemCount) {
                sortingExerciseLessonRequest.setSortingExercise(new ArrayList<>(sortingExerciseLessonRequest.getSortingExercise().subList(0, itemCount)));
            }
        } else if (lessonRequest instanceof InteractiveLessonRequest interactiveLessonRequest) {
            if (interactiveLessonRequest.getQuestions() != null && interactiveLessonRequest.getQuestions().size() > itemCount) {
                interactiveLessonRequest.setQuestions(new ArrayList<>(interactiveLessonRequest.getQuestions().subList(0, itemCount)));
            }
        }
    }

    private void enforceEmptyMediaLists(LessonRequest lessonRequest) {
        if (lessonRequest instanceof InteractiveLessonRequest interactiveLessonRequest) {
            if (interactiveLessonRequest.getQuestions() != null) {
                for (InteractiveQuestionRequest question : interactiveLessonRequest.getQuestions()) {
                    question.setImagePaths(new ArrayList<>());
                    question.setAudioPaths(new ArrayList<>());
                }
            }
        }
    }
}
