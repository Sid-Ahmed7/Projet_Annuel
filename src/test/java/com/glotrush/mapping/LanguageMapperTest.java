package com.glotrush.mapping;

import com.glotrush.dto.request.LanguageRequest;
import com.glotrush.dto.response.LanguageResponse;
import com.glotrush.entities.Language;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LanguageMapper Unit Tests")
class LanguageMapperTest {

    private final LanguageMapper mapper = Mappers.getMapper(LanguageMapper.class);

    @Test
    @DisplayName("Should map LanguageRequest to Language entity")
    void shouldMapLanguageRequestToLanguageEntity() {
        // Given
        LanguageRequest request = new LanguageRequest();
        request.setCode("en");
        request.setName("English");
        request.setOrderIndex(1);
        request.setIsActive(true);

        // When
        Language language = mapper.mapLanguageRequestToMapLanguageEntities(request);

        // Then
        assertThat(language).isNotNull();
        assertThat(language.getCode()).isEqualTo(request.getCode());
        assertThat(language.getName()).isEqualTo(request.getName());
        assertThat(language.getOrderIndex()).isEqualTo(request.getOrderIndex());
        assertThat(language.getIsActive()).isEqualTo(request.getIsActive());
        assertThat(language.getId()).isNull();
    }

    @Test
    @DisplayName("Should map Language entity to LanguageResponse")
    void shouldMapLanguageEntityToLanguageResponse() {
        // Given
        Language language = Language.builder()
                .id(UUID.randomUUID())
                .code("fr")
                .name("French")
                .orderIndex(2)
                .isActive(true)
                .build();

        // When
        LanguageResponse response = mapper.mapLanguageEntitiesToLanguageResponse(language);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(language.getId());
        assertThat(response.getCode()).isEqualTo(language.getCode());
        assertThat(response.getName()).isEqualTo(language.getName());
        assertThat(response.getOrderIndex()).isEqualTo(language.getOrderIndex());
        assertThat(response.getIsActive()).isEqualTo(language.getIsActive());
    }

    @Test
    @DisplayName("Should handle null values")
    void shouldHandleNullValues() {
        assertThat(mapper.mapLanguageRequestToMapLanguageEntities(null)).isNull();
        assertThat(mapper.mapLanguageEntitiesToLanguageResponse(null)).isNull();
    }
}
