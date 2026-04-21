package com.glotrush.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.glotrush.config.TestMessageSourceConfig;
import com.glotrush.dto.request.LanguageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.glotrush.builder.LanguageBuilder;
import com.glotrush.dto.response.LanguageResponse;
import com.glotrush.entities.Language;
import com.glotrush.exceptions.LanguageException;
import com.glotrush.mapping.LanguageMapper;
import com.glotrush.repositories.LanguageRepository;
import com.glotrush.repositories.LessonRepository;
import com.glotrush.repositories.TopicRepository;
import com.glotrush.repositories.UserLanguageRepository;
import com.glotrush.services.languages.LanguageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ContextConfiguration(classes = TestMessageSourceConfig.class)
@DisplayName("LanguageService Unit Tests")
class LanguageServiceTest {
    
    @Mock
    private LanguageRepository languageRepository;

    private LanguageService languageService;
    @Autowired
    private MessageSource messageSource;
    @Mock
    private LanguageBuilder languageBuilder;
    @Mock
    private LanguageMapper languageMapper;
    @Mock
    private TopicRepository topicRepository;
    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private UserLanguageRepository userLanguageRepository;
    private Language japanese;
    private Language french;

    @BeforeEach
    void setUp() {
        languageService = new LanguageService(messageSource, languageRepository, languageBuilder, languageMapper, topicRepository, lessonRepository, userLanguageRepository);
        japanese = Language.builder()
                .id(UUID.randomUUID())
                .code("ja")
                .name("Japanese")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        french = Language.builder()
                .id(UUID.randomUUID())
                .code("fr")
                .name("French")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should retrieve all active languages")
    void shouldGetAllActiveLanguages() {
        LanguageResponse japaneseResponse = LanguageResponse.builder()
                .id(japanese.getId())
                .code("ja")
                .name("Japanese")
                .build();

        LanguageResponse frenchResponse = LanguageResponse.builder()
                .id(french.getId())
                .code("fr")
                .name("French")
                .build();

        when(languageRepository.findByIsActiveTrueOrderByNameAsc())
                .thenReturn(Arrays.asList(japanese, french));
        when(userLanguageRepository.findMostPopularLanguageIdsByLearnerCount(any(), any())).thenReturn(Collections.emptyList());
        when(topicRepository.findByLanguage_Id(japanese.getId())).thenReturn(Collections.emptyList());
        when(topicRepository.findByLanguage_Id(french.getId())).thenReturn(Collections.emptyList());
        when(lessonRepository.countByTopic_Language_Id(japanese.getId())).thenReturn(0);
        when(lessonRepository.countByTopic_Language_Id(french.getId())).thenReturn(0);
        when(languageBuilder.mapToLanguageResponse(japanese, null, 0, 0, false)).thenReturn(japaneseResponse);
        when(languageBuilder.mapToLanguageResponse(french, null, 0, 0, false)).thenReturn(frenchResponse);

        List<LanguageResponse> result = languageService.getAllActiveLanguages();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCode()).isEqualTo("ja");
        assertThat(result.get(1).getCode()).isEqualTo("fr");
        verify(languageRepository).findByIsActiveTrueOrderByNameAsc();
    }

    @Test
    @DisplayName("Should get language by ID")
    void shouldGetLanguageById() {
        UUID languageId = japanese.getId();

        LanguageResponse japaneseResponse = LanguageResponse.builder()
                .id(japanese.getId())
                .code("ja")
                .name("Japanese")
                .build();

        when(languageRepository.findById(languageId)).thenReturn(Optional.of(japanese));
        when(userLanguageRepository.findMostPopularLanguageIdsByLearnerCount(any(), any())).thenReturn(Collections.emptyList());
        when(topicRepository.findByLanguage_Id(japanese.getId())).thenReturn(Collections.emptyList());
        when(lessonRepository.countByTopic_Language_Id(japanese.getId())).thenReturn(0);
        when(languageBuilder.mapToLanguageResponse(japanese, null, 0, 0, false)).thenReturn(japaneseResponse);

        LanguageResponse result = languageService.getLanguageById(languageId);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("ja");
        assertThat(result.getName()).isEqualTo("Japanese");
        verify(languageRepository).findById(languageId);
    }

    @Test
    @DisplayName("Should throw exception when language ID not found")
    void shouldThrowExceptionWhenLanguageIdNotFound() {
        UUID languageId = UUID.randomUUID();
        when(languageRepository.findById(languageId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> languageService.getLanguageById(languageId))
                .isInstanceOf(LanguageException.class)
                .hasMessage("Language not found");
    }

    @Test
    @DisplayName("Should get language by code")
    void shouldGetLanguageByCode() {
        LanguageResponse japaneseResponse = LanguageResponse.builder()
                .id(japanese.getId())
                .code("ja")
                .name("Japanese")
                .build();

        when(languageRepository.findByCode("ja")).thenReturn(Optional.of(japanese));
        when(userLanguageRepository.findMostPopularLanguageIdsByLearnerCount(any(), any())).thenReturn(Collections.emptyList());
        when(topicRepository.findByLanguage_Id(japanese.getId())).thenReturn(Collections.emptyList());
        when(lessonRepository.countByTopic_Language_Id(japanese.getId())).thenReturn(0);
        when(languageBuilder.mapToLanguageResponse(japanese, null, 0, 0, false)).thenReturn(japaneseResponse);

        LanguageResponse result = languageService.getLanguageByCode("ja");

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("ja");
    }

    @Test
    @DisplayName("Should create language")
    void shouldCreateLanguage() {
        LanguageRequest request = new LanguageRequest();
        request.setCode("es");
        request.setName("Spanish");

        Language language = Language.builder().code("es").name("Spanish").build();
        LanguageResponse response = LanguageResponse.builder().code("es").name("Spanish").build();

        when(languageMapper.mapLanguageRequestToMapLanguageEntities(request)).thenReturn(language);
        when(languageMapper.mapLanguageEntitiesToLanguageResponse(language)).thenReturn(response);

        LanguageResponse result = languageService.createLanguage(request);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("es");
        verify(languageRepository).save(language);
    }

    @Test
    @DisplayName("Should update language")
    void shouldUpdateLanguage() {
        UUID languageId = japanese.getId();
        LanguageRequest request = new com.glotrush.dto.request.LanguageRequest();
        request.setCode("ja-updated");
        request.setName("Japanese Updated");

        when(languageRepository.findById(languageId)).thenReturn(Optional.of(japanese));
        doNothing().when(languageMapper).updateLanguageFromRequest(eq(request), eq(japanese));
        when(languageMapper.mapLanguageEntitiesToLanguageResponse(japanese)).thenReturn(LanguageResponse.builder().code("ja-updated").build());

        LanguageResponse result = languageService.updateLanguage(languageId, request);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("ja-updated");
        verify(languageRepository).save(japanese);
    }

    @Test
    @DisplayName("Should remove language")
    void shouldRemoveLanguage() {
        UUID languageId = japanese.getId();
        when(languageRepository.existsById(languageId)).thenReturn(true);

        languageService.removeLanguage(languageId);

        verify(languageRepository).deleteById(languageId);
    }

    @Test
    @DisplayName("Should throw exception when removing non-existent language")
    void shouldThrowExceptionWhenRemovingNonExistentLanguage() {
        UUID languageId = UUID.randomUUID();
        when(languageRepository.existsById(languageId)).thenReturn(false);

        assertThatThrownBy(() -> languageService.removeLanguage(languageId))
                .isInstanceOf(com.glotrush.exceptions.LessonNotFoundException.class);
    }
}
