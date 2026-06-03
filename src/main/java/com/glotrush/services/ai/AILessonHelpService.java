package com.glotrush.services.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.glotrush.dto.request.ai.AILessonHelpRequest;
import com.glotrush.dto.response.ai.AILessonHelpResponse;
import com.glotrush.entities.Language;
import com.glotrush.entities.Lesson;
import com.glotrush.entities.Topic;
import com.glotrush.entities.ai.AIGenerationLog;
import com.glotrush.entities.exercice.FlashcardEntity;
import com.glotrush.entities.exercice.MatchingPairEntity;
import com.glotrush.entities.exercice.QcmQuestionEntity;
import com.glotrush.entities.exercice.SortingExerciseEntity;
import com.glotrush.enumerations.AILessonHelpType;
import com.glotrush.enumerations.LessonType;
import com.glotrush.repositories.LessonRepository;
import com.glotrush.repositories.ai.AIGenerationLogRepository;
import com.glotrush.repositories.exercice.FlashcardRepository;
import com.glotrush.repositories.exercice.MatchingPairRepository;
import com.glotrush.repositories.exercice.QcmQuestionRepository;
import com.glotrush.repositories.exercice.SortingExerciseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AILessonHelpService {

    private final IAIService aiService;
    private final AIQuotaService aiQuotaService;
    private final LessonRepository lessonRepository;
    private final AIGenerationLogRepository aiGenerationLogRepository;
    private final QcmQuestionRepository qcmQuestionRepository;
    private final FlashcardRepository flashcardRepository;
    private final MatchingPairRepository matchingPairRepository;
    private final SortingExerciseRepository sortingExerciseRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public AILessonHelpResponse getLessonHelp(UUID accountId, AILessonHelpRequest request) {
        aiQuotaService.verifyAndConsumeAIQuota(accountId);

        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new RuntimeException("Lesson not found: " + request.getLessonId()));

        Topic topic = lesson.getTopic();
        Language sourceLanguage = topic.getSourceLanguage();
        Language targetLanguage = topic.getTargetLanguage();

        String exerciseContext = extractExerciseContext(request.getExerciseId(), request.getExerciseType());
        
        String fullPrompt = buildSystemPrompt(request, exerciseContext, sourceLanguage, targetLanguage);

        log.info("Requesting AI memorization help type {} for user {}", request.getHelpType(), accountId);

        AILessonHelpResponse response = aiService.generateJsonContent(fullPrompt, AILessonHelpResponse.class);

        saveGenerationLog(accountId, request.getExerciseType(), fullPrompt, response);

        return response;
    }

    private String extractExerciseContext(UUID exerciseId, LessonType exerciseType) {
        if (exerciseType == LessonType.QCM) {
            QcmQuestionEntity question = qcmQuestionRepository.findById(exerciseId)
                    .orElseThrow(() -> new RuntimeException("QCM question not found: " + exerciseId));

            String correctAnswer = "Non definie";
            if (question.getCorrectOptionIndex() != null && question.getCorrectOptionIndex() >= 0 && question.getCorrectOptionIndex() < question.getOptions().size()) {
                correctAnswer = question.getOptions().get(question.getCorrectOptionIndex());
            }

            return "Type d'exercice: QCM (Question a choix multiples)\n" +
                    "Question: " + question.getQuestion() + "\n" +
                    "Options: " + String.join(", ", question.getOptions()) + "\n" +
                    "Bonne reponse: " + correctAnswer + "\n" +
                    "Explication existante: " + (question.getExplanation() != null ? question.getExplanation() : "Aucune");
        }

        if (exerciseType == LessonType.FLASHCARD) {
            FlashcardEntity flashcard = flashcardRepository.findById(exerciseId)
                    .orElseThrow(() -> new RuntimeException("Flashcard not found: " + exerciseId));

            return "Type d'exercice: Flashcard (Meme mot d'une langue a l'autre)\n" +
                    "Recto (Mot/Phrase source): " + flashcard.getFront() + " (Langue: " + flashcard.getFrontLanguage() + ")\n" +
                    "Verso (Traduction/Reponse cible): " + flashcard.getBack() + " (Langue: " + flashcard.getBackLanguage() + ")";
        }

        if (exerciseType == LessonType.MATCHING_PAIR) {
            MatchingPairEntity pair = matchingPairRepository.findById(exerciseId)
                    .orElseThrow(() -> new RuntimeException("Matching pair not found: " + exerciseId));

            return "Type d'exercice: Association de paires (Matching Pair)\n" +
                    "Element 1 (Langue source): " + pair.getItem1() + "\n" +
                    "Element 2 (Langue cible): " + pair.getItem2();
        }

        if (exerciseType == LessonType.SORTING_EXERCISE) {
            SortingExerciseEntity sorting = sortingExerciseRepository.findById(exerciseId)
                    .orElseThrow(() -> new RuntimeException("Sorting exercise not found: " + exerciseId));

            String correctSentence = "Non definie";
            if (sorting.getCorrectOrder() != null && sorting.getItems() != null) {
                List<String> orderedWords = new ArrayList<>();
                for (Integer index : sorting.getCorrectOrder()) {
                    if (index >= 0 && index < sorting.getItems().size()) {
                        orderedWords.add(sorting.getItems().get(index));
                    }
                }
                correctSentence = String.join(" ", orderedWords);
            }

            return "Type d'exercice: Ordonnancement de phrase (Sorting Exercise)\n" +
                    "Mots a trier (melanges): " + String.join(", ", sorting.getItems()) + "\n" +
                    "Phrase correcte ordonnee (Reponse): " + correctSentence;
        }

        throw new IllegalArgumentException("Unknown exercise type: " + exerciseType);
    }

    private String buildSystemPrompt(AILessonHelpRequest request, String exerciseContext, Language sourceLanguage, Language targetLanguage) {
        StringBuilder promptBuilder = new StringBuilder();

        promptBuilder.append("Tu es un assistant d'apprentissage linguistique d'IA expert en pedagogie.\n");
        promptBuilder.append("Un utilisateur effectue une lecon de langue de la langue source (")
                .append(sourceLanguage.getName()).append(") vers la langue cible (")
                .append(targetLanguage.getName()).append(").\n\n");

        promptBuilder.append("Voici le contexte technique de l'exercice en cours :\n");
        promptBuilder.append(exerciseContext).append("\n\n");

        // Regle de securite essentielle pour eviter de donner la solution brute a l'eleve
        promptBuilder.append("IMPORTANT - REGLE ABSOLUE DE SECURITE :\n");
        promptBuilder.append("- Tu ne dois ABSOLUMENT PAS donner directement la reponse ou la traduction correcte a l'utilisateur.\n");
        promptBuilder.append("- Tu dois eviter de donner un indice trop evident ou trop facile (ex: ne pas dire 'le mot commence par A' s'il n'y a qu'une option commencant par A).\n");
        promptBuilder.append("- Aide l'utilisateur a reflechir par lui-meme grace a des indices pedagogiques adaptes au type d'aide demande.\n\n");

        promptBuilder.append("Le type d'aide demande est : ").append(request.getHelpType()).append(".\n\n");

        promptBuilder.append("CONSIGNES POUR GENERER LA REPONSE :\n");
        if (request.getHelpType() == AILessonHelpType.STRUCTURE_AND_RULE) {
            promptBuilder.append("- Pour STRUCTURE_AND_RULE :\n");
            promptBuilder.append("  * Remplis le champ 'title' avec 'Structure & Regles Grammaticales'.\n");
            promptBuilder.append("  * Explique dans le champ 'explanation' la structure grammaticale, la conjugaison ou la syntaxe testee par la phrase/le mot.\n");
            promptBuilder.append("  * Fournis dans le champ 'examples' 1 ou 2 exemples de phrases correctes utilisant la meme structure grammaticale mais avec d'autres mots.\n");
            promptBuilder.append("  * Laisse 'visualAnchor' et 'warning' a null.\n");
        } else if (request.getHelpType() == AILessonHelpType.ASSOCIATION_AND_MNEMONIC) {
            promptBuilder.append("- Pour ASSOCIATION_AND_MNEMONIC :\n");
            promptBuilder.append("  * Remplis le champ 'title' avec 'Association & Moyen Mnemotechnique'.\n");
            promptBuilder.append("  * Propose dans 'explanation' un moyen mnemotechnique, un lien sonore ou une association d'idees rigolote.\n");
            promptBuilder.append("  * Propose dans 'visualAnchor' une scene mentale ridicule ou marquante a s'imaginer pour retenir l'element.\n");
            promptBuilder.append("  * Laisse 'examples' et 'warning' a null.\n");
        } else if (request.getHelpType() == AILessonHelpType.TRAP_WARNING) {
            promptBuilder.append("- Pour TRAP_WARNING :\n");
            promptBuilder.append("  * Remplis le champ 'title' avec 'Mise en garde contre les pieges'.\n");
            promptBuilder.append("  * Explique dans 'explanation' les erreurs classiques de confusion, de prononciation ou de syntaxe liees a cet exercice.\n");
            promptBuilder.append("  * Fournis dans 'warning' une alerte precise sur un faux-ami ou un piege classique sans dire explicitement quelle option est la bonne ou mauvaise.\n");
            promptBuilder.append("  * Laisse 'visualAnchor' et 'examples' a null.\n");
        } else if (request.getHelpType() == AILessonHelpType.USAGE_CONTEXT) {
            promptBuilder.append("- Pour USAGE_CONTEXT :\n");
            promptBuilder.append("  * Remplis le champ 'title' avec 'Usage en contexte'.\n");
            promptBuilder.append("  * Explique dans 'explanation' comment le concept ou le mot s'utilise au quotidien.\n");
            promptBuilder.append("  * Fournis dans 'examples' 1 ou 2 phrases simples de la vie courante en langue cible illustrant le mot ou la structure, mais en masquant la reponse cible par '___' pour que l'utilisateur la devine.\n");
            promptBuilder.append("  * Laisse 'visualAnchor' and 'warning' a null.\n");
        }

        promptBuilder.append("\nTu dois retourner UNIQUEMENT un objet JSON valide correspondant a la structure de classe Java 'AILessonHelpResponse'.\n");
        promptBuilder.append("Voici la structure JSON attendue :\n");
        promptBuilder.append("{\n");
        promptBuilder.append("  \"title\": \"Le titre\",\n");
        promptBuilder.append("  \"explanation\": \"L'explication\",\n");
        promptBuilder.append("  \"visualAnchor\": \"La scene mentale (ou null)\",\n");
        promptBuilder.append("  \"examples\": [\"Exemple 1\", \"Exemple 2\"] (ou null),\n");
        promptBuilder.append("  \"warning\": \"L'avertissement de piege (ou null)\"\n");
        promptBuilder.append("}\n");

        return promptBuilder.toString();
    }

    private void saveGenerationLog(UUID accountId, LessonType lessonType, String prompt, AILessonHelpResponse response) {
        try {
            String jsonResponse = objectMapper.writeValueAsString(response);
            AIGenerationLog logEntry = AIGenerationLog.builder()
                    .accountId(accountId)
                    .lessonType(lessonType)
                    .userPrompt(prompt)
                    .generatedResponse(jsonResponse)
                    .build();
            aiGenerationLogRepository.save(logEntry);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize AI help response for generation logs", e);
        }
    }
}
