package com.glotrush.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.glotrush.builder.TopicBuilder;
import com.glotrush.dto.response.TopicResponse;
import com.glotrush.dto.response.UserProgressSummary;
import com.glotrush.entities.Language;
import com.glotrush.entities.Topic;
import com.glotrush.entities.UserProgress;
import com.glotrush.exceptions.TopicNotFoundException;
import com.glotrush.repositories.TopicRepository;
import com.glotrush.repositories.UserProgressRepository;
import com.glotrush.services.topic.TopicService;

@ExtendWith(MockitoExtension.class)
@DisplayName("TopicService Unit Tests")
class TopicServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private UserProgressRepository userProgressRepository;

    @Mock
    private TopicBuilder topicBuilder;

    @InjectMocks
    private TopicService topicService;

    private UUID accountId;
    private UUID topicId;
    private UUID languageId;
    private Topic topic;
    private Language language;
    private UserProgress userProgress;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        topicId = UUID.randomUUID();
        languageId = UUID.randomUUID();

        language = Language.builder()
                .id(languageId)
                .code("EN")
                .name("English")
                .build();

        topic = Topic.builder()
                .id(topicId)
                .name("Basics")
                .description("Basics Course")
                .language(language)
                .totalLessons(10)
                .orderIndex(1)
                .isActive(true)
                .minLevelRequired(1)
                .build();

        userProgress = UserProgress.builder()
                .id(UUID.randomUUID())
                .topic(topic)
                .level(2)
                .totalXP(500L)
                .completedLessons(5)
                .completionPercentage(40.0)
                .build();
    }
    
    @Test
    @DisplayName("Should return all active topics with user progress")
    void shouldGetAllTopicsWithProgress() {
        TopicResponse expectedResponse = TopicResponse.builder()
                .id(topicId)
                .name("Basics Course")
                .userProgress(UserProgressSummary.builder()
                        .level(2)
                        .completedLessons(5)
                        .build())
                .build();

        when(topicRepository.findByIsActiveTrueOrderByOrderIndexAsc()).thenReturn(List.of(topic));
        when(userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId))
                .thenReturn(Optional.of(userProgress));
        when(topicBuilder.mapToTopicResponse(eq(topic), any())).thenReturn(expectedResponse);

        List<TopicResponse> result = topicService.getAllTopics(accountId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Basics Course");
        assertThat(result.get(0).getUserProgress()).isNotNull();

        verify(topicRepository).findByIsActiveTrueOrderByOrderIndexAsc();
    }

    @Test
    @DisplayName("Should return topics without progress for new user")
    void shouldReturnTopicsWithoutProgressForNewUser() {
        TopicResponse expectedResponse = TopicResponse.builder()
                .id(topicId)
                .name("Basics Course")
                .userProgress(null)
                .build();

        when(topicRepository.findByIsActiveTrueOrderByOrderIndexAsc()).thenReturn(List.of(topic));
        when(userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId))
                .thenReturn(Optional.empty());
        when(topicBuilder.mapToTopicResponse(eq(topic), eq(Optional.empty()))).thenReturn(expectedResponse);

        List<TopicResponse> result = topicService.getAllTopics(accountId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserProgress()).isNull();
    }

    @Test
    @DisplayName("Should return empty list when no active topics")
    void shouldReturnEmptyListWhenNoActiveTopics() {
        when(topicRepository.findByIsActiveTrueOrderByOrderIndexAsc()).thenReturn(Collections.emptyList());

        List<TopicResponse> result = topicService.getAllTopics(accountId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return topics for specific language")
    void shouldGetTopicsByLanguage() {
        TopicResponse expectedResponse = TopicResponse.builder()
                .id(topicId)
                .name("Basics Course")
                .languageId(languageId)
                .build();

        when(topicRepository.findByLanguage_IdAndIsActiveTrueOrderByOrderIndexAsc(languageId))
                .thenReturn(List.of(topic));
        when(userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId))
                .thenReturn(Optional.of(userProgress));
        when(topicBuilder.mapToTopicResponse(eq(topic), any())).thenReturn(expectedResponse);

        List<TopicResponse> result = topicService.getTopicsByLanguage(languageId, accountId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLanguageId()).isEqualTo(languageId);

        verify(topicRepository).findByLanguage_IdAndIsActiveTrueOrderByOrderIndexAsc(languageId);
    }

    @Test
    @DisplayName("Should return empty list when no topics for language")
    void shouldReturnEmptyListWhenNoTopicsForLanguage() {
        when(topicRepository.findByLanguage_IdAndIsActiveTrueOrderByOrderIndexAsc(languageId))
                .thenReturn(Collections.emptyList());

        List<TopicResponse> result = topicService.getTopicsByLanguage(languageId, accountId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return topic by id with user progress")
    void shouldGetTopicByIdWithProgress() {
        TopicResponse expectedResponse = TopicResponse.builder()
                .id(topicId)
                .name("Grammar Basics")
                .userProgress(UserProgressSummary.builder().level(2).build())
                .build();

        when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));
        when(userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId))
                .thenReturn(Optional.of(userProgress));
        when(topicBuilder.mapToTopicResponse(eq(topic), any())).thenReturn(expectedResponse);

        TopicResponse result = topicService.getTopicById(topicId, accountId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(topicId);
        assertThat(result.getUserProgress()).isNotNull();

        verify(topicRepository).findById(topicId);
    }

    @Test
    @DisplayName("Should throw exception when topic not found")
    void shouldThrowExceptionWhenTopicNotFound() {
        when(topicRepository.findById(topicId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> topicService.getTopicById(topicId, accountId))
                .isInstanceOf(TopicNotFoundException.class)
                .hasMessage("Topic not found");

        verify(topicRepository).findById(topicId);
    }
}
