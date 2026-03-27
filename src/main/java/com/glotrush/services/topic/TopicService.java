package com.glotrush.services.topic;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.glotrush.dto.request.ExamResultRequest;
import com.glotrush.dto.request.TopicRequest;
import com.glotrush.dto.response.*;
import com.glotrush.dto.response.exercice.*;
import com.glotrush.dto.response.lesson.*;
import com.glotrush.entities.Language;
import com.glotrush.entities.Lesson;
import com.glotrush.entities.lesson.*;
import com.glotrush.enumerations.LessonStatus;
import com.glotrush.enumerations.ProficiencyLevel;
import com.glotrush.mapping.LessonEntityToLessonResponse;
import com.glotrush.mapping.TopicMapper;
import com.glotrush.repositories.LanguageRepository;
import com.glotrush.repositories.LessonRepository;
import com.glotrush.services.progress.IProgressService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.context.MessageSource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.glotrush.entities.Topic;
import com.glotrush.entities.UserProgress;
import com.glotrush.exceptions.TopicNotFoundException;
import com.glotrush.builder.TopicBuilder;
import com.glotrush.repositories.TopicRepository;
import com.glotrush.repositories.UserProgressRepository;
import com.glotrush.utils.LocaleUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
public class TopicService implements ITopicService {
    private final MessageSource messageSource;
    private final TopicRepository topicRepository;
    private final UserProgressRepository userProgressRepository;
    private final LanguageRepository languageRepository;
    private final LessonRepository lessonRepository;
    private final TopicBuilder topicBuilder;
    private final TopicMapper topicMapper;
    private final LessonEntityToLessonResponse lessonMapper;
    private final com.glotrush.repositories.UserLessonProgressRepository userLessonProgressRepositoryLegacy;
    private final IProgressService progressService;

    @Override
    public List<TopicResponse> getAllTopics(UUID accountId) {
        return topicRepository.findByIsActiveTrueOrderByOrderIndexAsc().stream()
                .map(topic -> {
                    Optional<UserProgress> progressOpt = userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topic.getId());
                    return topicBuilder.mapToTopicResponse(topic, progressOpt);
                })
                .toList();
    }

    @Override
    public List<TopicResponse> getAllTopics() {
        return topicRepository.findAll().stream()
                .map(topicMapper::mapTopicEntitiesToTopicResponse)
                .toList();
    }

    @Override
    public List<TopicResponse> getTopicsByLanguage(UUID languageId, UUID accountId) {
        return topicRepository.findByLanguage_IdAndIsActiveTrueOrderByOrderIndexAsc(languageId).stream().map(topic -> {
            Optional<UserProgress> progressOpt = userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topic.getId());
            return topicBuilder.mapToTopicResponse(topic, progressOpt);
        }).toList();
    }

    @Override
    public TopicResponse getTopicById(UUID topicId, UUID accountId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new TopicNotFoundException(messageSource.getMessage("error.topic.notfound", null, LocaleUtils.getCurrentLocale())));
        Optional<UserProgress> progressOpt = userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId);
        return topicBuilder.mapToTopicResponse(topic, progressOpt);
    }

    @Override
    public TopicResponse createTopic(TopicRequest topicRequest) {
        Language language = languageRepository.findById(topicRequest.getLanguageId())
                .orElseThrow(() -> new TopicNotFoundException(messageSource.getMessage("error.topic.language_notfound", null, LocaleUtils.getCurrentLocale())));
        
        Topic topicEntity = topicMapper.mapTopicRequestToMapTopicEntities(topicRequest);
        topicEntity.setLanguage(language);
        
        topicRepository.save(topicEntity);
        return topicMapper.mapTopicEntitiesToTopicResponse(topicEntity);
    }

    @Override
    public void removeTopic(UUID topicId) {
        if(!topicRepository.existsById(topicId)) {
            throw new TopicNotFoundException(messageSource.getMessage("error.topic.notfound", null, LocaleUtils.getCurrentLocale()));
        }
        topicRepository.deleteById(topicId);
    }

    @Override
    public TopicResponse updateTopic(UUID topicId, TopicRequest topicRequest) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new TopicNotFoundException(messageSource.getMessage("error.topic.notfound", null, LocaleUtils.getCurrentLocale())));

        if (topicRequest.getLanguageId() != null && !topicRequest.getLanguageId().equals(topic.getLanguage().getId())) {
            Language language = languageRepository.findById(topicRequest.getLanguageId())
                    .orElseThrow(() -> new TopicNotFoundException(messageSource.getMessage("error.topic.language_notfound", null, LocaleUtils.getCurrentLocale())));
            topic.setLanguage(language);
        }

        topicMapper.updateTopicFromRequest(topicRequest, topic);
        
        return topicMapper.mapTopicEntitiesToTopicResponse(topicRepository.save(topic));
    }

    @Override
    public List<TopicResponse> searchTopics(String name, ProficiencyLevel difficulty, Boolean isActive) {
        Specification<Topic> spec = createSearchSpecification(null, name, difficulty, isActive);

        return topicRepository.findAll(spec).stream()
                .map(topicMapper::mapTopicEntitiesToTopicResponse)
                .toList();
    }

    @Override
    public List<TopicResponse> searchActiveTopics(UUID languageId, String name, ProficiencyLevel difficulty) {
        Specification<Topic> spec = createSearchSpecification(languageId, name, difficulty, true);

        return topicRepository.findAll(spec).stream()
                .map(topicMapper::mapTopicEntitiesToTopicResponse)
                .toList();
    }

    private Specification<Topic> createSearchSpecification(UUID languageId, String name, ProficiencyLevel difficulty, Boolean isActive) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (languageId != null) {
                predicates.add(criteriaBuilder.equal(root.get("language").get("id"), languageId));
            }

            if (name != null && !name.isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (difficulty != null) {
                predicates.add(criteriaBuilder.equal(root.get("difficulty"), difficulty));
            }

            if (isActive != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), isActive));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public ExamResponse generateTopicExam(UUID accountId, UUID topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new TopicNotFoundException(messageSource.getMessage("error.topic.notfound", null, LocaleUtils.getCurrentLocale())));

        // Vérifier si toutes les leçons sont terminées
        List<Lesson> lessons = lessonRepository.findByTopic_IdAndIsActiveTrueOrderByOrderIndexAsc(topicId);
        long completedCount = userLessonProgressRepositoryLegacy.findByAccount_Id(accountId).stream()
                .filter(p -> p.getLesson().getTopic().getId().equals(topicId) && p.getStatus() == LessonStatus.COMPLETED)
                .count();

        if (completedCount < lessons.size()) {
            throw new IllegalStateException(messageSource.getMessage("error.topic.exam.not_ready", null, LocaleUtils.getCurrentLocale()));
        }

        List<QcmQuestionExamResponse> qcmQuestions = new ArrayList<>();
        List<FlashcardExamResponse> flashcards = new ArrayList<>();
        List<MatchingPairResponse> matchingPairs = new ArrayList<>();
        List<SortingExerciseExamResponse> sortingExercises = new ArrayList<>();

        for (Lesson lesson : lessons) {
            if (Boolean.FALSE.equals(lesson.getIsIncludedInExam())) continue;

            if (lesson instanceof QcmLesson qcmLesson) {
                qcmQuestions.addAll(qcmLesson.getQuestions().stream()
                        .map(lessonMapper::mapQcmQuestionEntityToQcmQuestionExamResponse)
                        .toList());
            } else if (lesson instanceof FlashcardLesson flashcardLesson) {
                flashcards.addAll(flashcardLesson.getFlashcards().stream()
                        .map(lessonMapper::mapFlashcardEntityToFlashcardExamResponse)
                        .toList());
            } else if (lesson instanceof MatchingPairLesson matchingPairLesson) {
                LessonResponse resp = lessonMapper.lessonEntityToLessonResponse(matchingPairLesson, messageSource);
                if (resp instanceof MatchingPairLessonResponse mpResp) {
                    matchingPairs.addAll(mpResp.getMatchingPairs());
                }
            } else if (lesson instanceof SortingExerciseLesson sortingLesson) {
                sortingExercises.addAll(sortingLesson.getSortingExercise().stream()
                        .map(lessonMapper::mapSortingExerciseEntityToSortingExerciseExamResponse)
                        .toList());
            }
        }

        Collections.shuffle(qcmQuestions);
        Collections.shuffle(flashcards);
        Collections.shuffle(matchingPairs);
        Collections.shuffle(sortingExercises);

        // Limiter à un certain nombre (ex: 5 de chaque pour un examen varié)
        return ExamResponse.builder()
                .topicId(topicId)
                .topicName(topic.getName())
                .qcmQuestions(qcmQuestions.stream().limit(5).collect(Collectors.toList()))
                .flashcards(flashcards.stream().limit(5).collect(Collectors.toList()))
                .matchingPairs(matchingPairs.stream().limit(5).collect(Collectors.toList()))
                .sortingExercises(sortingExercises.stream().limit(5).collect(Collectors.toList()))
                .build();
    }

    @Override
    public CompleteLessonResponse completeTopicExam(UUID accountId, UUID topicId, ExamResultRequest examRequest) {
        UserProgress progress = userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId)
                .orElseThrow(() -> new TopicNotFoundException(messageSource.getMessage("error.topic.notfound", null, LocaleUtils.getCurrentLocale())));

        boolean isSuccessful = examRequest.getScore() >= 80; // Seuil arbitraire pour l'examen

        if (isSuccessful) {
            progress.setExamPassed(true);
            if (progress.getBestExamScore() == null || examRequest.getScore() > progress.getBestExamScore()) {
                progress.setBestExamScore(examRequest.getScore());
            }
        }

        // Mise à jour des stats globales si nécessaire
        if (examRequest.getCorrectAnswers() != null) {
            progress.setCorrectAnswers(progress.getCorrectAnswers() + examRequest.getCorrectAnswers());
        }
        if (examRequest.getTotalAnswers() != null) {
            progress.setTotalAnswers(progress.getTotalAnswers() + examRequest.getTotalAnswers());
        }
        progress.calculateAccuracy();
        
        userProgressRepository.save(progress);

        return CompleteLessonResponse.builder()
                .success(isSuccessful)
                .message(isSuccessful ? messageSource.getMessage("info.topic.exam.success", null, LocaleUtils.getCurrentLocale()) 
                                    : messageSource.getMessage("error.topic.exam.failed", null, LocaleUtils.getCurrentLocale()))
                .xpEarned(isSuccessful ? 50 : 0) // Bonus XP examen
                .totalXP(progress.getTotalXP())
                .progress(progressService.getProgressByTopic(accountId, topicId))
                .build();
    }
}
