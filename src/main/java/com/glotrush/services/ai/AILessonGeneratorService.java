package com.glotrush.services.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.glotrush.dto.request.LessonRequest;
import com.glotrush.dto.request.lesson.FlashcardLessonRequest;
import com.glotrush.dto.request.lesson.MatchingPairLessonRequest;
import com.glotrush.dto.request.lesson.QcmLessonRequest;
import com.glotrush.dto.request.lesson.SortingExerciseLessonRequest;
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

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AILessonGeneratorService {

    private final IAIService aiService;
    private final AIGenerationLogRepository logRepository;
    private final TopicRepository topicRepository;
    private final LessonRepository lessonRepository;
    private final LessonEntityToLessonResponse lessonEntityToLessonResponse;
    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;
    private final AIQuotaService aiQuotaService;

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
        
        generatedRequest.setTopicId(topicId);
        generatedRequest.setLessonType(lessonType);
        if (generatedRequest.getIsActive() == null) {
            generatedRequest.setIsActive(true);
        }

        saveLog(accountId, lessonType, fullPrompt, generatedRequest);

        return generatedRequest;
    }

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
            }
            promptBuilder.append("Quantité : Tu dois générer exactement ").append(itemCount).append(" ").append(elementsDescription).append(".\n\n");
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
}
