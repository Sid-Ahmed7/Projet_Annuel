package com.glotrush.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.glotrush.config.TestMessageSourceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.glotrush.builder.TopicBuilder;
import com.glotrush.dto.response.TopicResponse;
import com.glotrush.entities.Language;
import com.glotrush.entities.Topic;
import com.glotrush.entities.UserProgress;
import com.glotrush.exceptions.TopicNotFoundException;
import com.glotrush.dto.request.TopicRequest;
import com.glotrush.mapping.TopicMapper;
import com.glotrush.repositories.LanguageRepository;
import com.glotrush.repositories.TopicRepository;
import com.glotrush.repositories.UserProgressRepository;
import com.glotrush.services.topic.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ContextConfiguration(classes = TestMessageSourceConfig.class)
@DisplayName("TopicService Unit Tests")
class TopicServiceTest {
    @Autowired
    private MessageSource messageSource;
    @Mock
    private TopicRepository topicRepository;

    @Mock
    private UserProgressRepository userProgressRepository;

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private TopicBuilder topicBuilder;

    @Mock
    private TopicMapper topicMapper;

    private TopicService topicService;

    private UUID accountId;
    private UUID topicId;
    private UUID languageId;
    private Topic topic;
    private Language language;
    private UserProgress userProgress;

    @BeforeEach
    void setUp() {
        topicService = new TopicService(messageSource, topicRepository, userProgressRepository, languageRepository, topicBuilder, topicMapper);
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
                .orderIndex(1)
                .isActive(true)
                .build();

        userProgress = UserProgress.builder()
                .id(UUID.randomUUID())
                .topic(topic)
                .totalXP(500L)
                .completedLessons(5)
                .completionPercentage(40.0)
                .build();
    }
    
    @Test
    @DisplayName("Should return all topics without progress")
    void shouldGetAllTopicsWithoutProgress() {
        TopicResponse expectedResponse = TopicResponse.builder()
                .id(topicId)
                .name("Basics")
                .build();

        when(topicRepository.findAll()).thenReturn(List.of(topic));
        when(topicMapper.mapTopicEntitiesToTopicResponse(topic)).thenReturn(expectedResponse);

        List<TopicResponse> result = topicService.getAllTopics();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Basics");

        verify(topicRepository).findAll();
    }

    @Test
    @DisplayName("Should return all active topics with user progress")
    void shouldGetAllTopicsWithProgress() {
        TopicResponse expectedResponse = TopicResponse.builder()
                .id(topicId)
                .name("Basics Course")
                .build();

        when(topicRepository.findByIsActiveTrueOrderByOrderIndexAsc()).thenReturn(List.of(topic));
        when(userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId))
                .thenReturn(Optional.of(userProgress));
        when(topicBuilder.mapToTopicResponse(eq(topic), any())).thenReturn(expectedResponse);

        List<TopicResponse> result = topicService.getAllTopics(accountId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Basics Course");

        verify(topicRepository).findByIsActiveTrueOrderByOrderIndexAsc();
    }

    @Test
    @DisplayName("Should return topics without progress for new user")
    void shouldReturnTopicsWithoutProgressForNewUser() {
        TopicResponse expectedResponse = TopicResponse.builder()
                .id(topicId)
                .name("Basics Course")
                .build();

        when(topicRepository.findByIsActiveTrueOrderByOrderIndexAsc()).thenReturn(List.of(topic));
        when(userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId))
                .thenReturn(Optional.empty());
        when(topicBuilder.mapToTopicResponse(eq(topic), eq(Optional.empty()))).thenReturn(expectedResponse);

        List<TopicResponse> result = topicService.getAllTopics(accountId);

        assertThat(result).hasSize(1);
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
                .build();

        when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));
        when(userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId))
                .thenReturn(Optional.of(userProgress));
        when(topicBuilder.mapToTopicResponse(eq(topic), any())).thenReturn(expectedResponse);

        TopicResponse result = topicService.getTopicById(topicId, accountId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(topicId);

        verify(topicRepository).findById(topicId);
    }

    @Test
    @DisplayName("Should throw exception when topic not found")
    void shouldThrowExceptionWhenTopicNotFound() {
        when(topicRepository.findById(topicId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> topicService.getTopicById(topicId, accountId))
                .isInstanceOf(TopicNotFoundException.class);

        verify(topicRepository).findById(topicId);
    }

    @Test
    @DisplayName("Should create topic successfully")
    void shouldCreateTopicSuccessfully() {
        TopicRequest request = TopicRequest.builder()
                .languageId(languageId)
                .name("New Topic")
                .build();
        TopicResponse expectedResponse = TopicResponse.builder().id(topicId).name("New Topic").build();

        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));
        when(topicMapper.mapTopicRequestToMapTopicEntities(any())).thenReturn(topic);
        when(topicMapper.mapTopicEntitiesToTopicResponse(any())).thenReturn(expectedResponse);

        TopicResponse result = topicService.createTopic(request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("New Topic");
        verify(topicRepository).save(any());
    }

    @Test
    @DisplayName("Should throw exception when creating topic with invalid language")
    void shouldThrowExceptionWhenCreatingTopicWithInvalidLanguage() {
        TopicRequest request = TopicRequest.builder().languageId(languageId).build();
        when(languageRepository.findById(languageId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> topicService.createTopic(request))
                .isInstanceOf(TopicNotFoundException.class);
    }

    @Test
    @DisplayName("Should remove topic successfully")
    void shouldRemoveTopicSuccessfully() {
        when(topicRepository.existsById(topicId)).thenReturn(true);

        topicService.removeTopic(topicId);

        verify(topicRepository).deleteById(topicId);
    }

    @Test
    @DisplayName("Should throw exception when removing non-existent topic")
    void shouldThrowExceptionWhenRemovingNonExistentTopic() {
        when(topicRepository.existsById(topicId)).thenReturn(false);

        assertThatThrownBy(() -> topicService.removeTopic(topicId))
                .isInstanceOf(TopicNotFoundException.class);
    }

    @Test
    @DisplayName("Should update topic successfully")
    void shouldUpdateTopicSuccessfully() {
        TopicRequest request = TopicRequest.builder()
                .languageId(languageId)
                .name("Updated Name")
                .build();
        TopicResponse expectedResponse = TopicResponse.builder().id(topicId).name("Updated Name").build();

        when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));
        doNothing().when(topicMapper).updateTopicFromRequest(eq(request), eq(topic));
        when(topicMapper.mapTopicEntitiesToTopicResponse(any())).thenReturn(expectedResponse);
        when(topicRepository.save(any())).thenReturn(topic);

        TopicResponse result = topicService.updateTopic(topicId, request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Name");
        verify(topicRepository).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent topic")
    void shouldThrowExceptionWhenUpdatingNonExistentTopic() {
        TopicRequest request = TopicRequest.builder().languageId(languageId).build();
        when(topicRepository.findById(topicId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> topicService.updateTopic(topicId, request))
                .isInstanceOf(TopicNotFoundException.class);
    }
}
