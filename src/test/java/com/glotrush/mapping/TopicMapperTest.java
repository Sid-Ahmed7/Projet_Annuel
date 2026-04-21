package com.glotrush.mapping;

import com.glotrush.dto.request.TopicRequest;
import com.glotrush.dto.response.TopicResponse;
import com.glotrush.entities.Language;
import com.glotrush.entities.Topic;
import com.glotrush.enumerations.ProficiencyLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("TopicMapper Unit Tests")
class TopicMapperTest {

    @Autowired
    private TopicMapper topicMapper;

    @Test
    @DisplayName("Should map TopicRequest to Topic entity")
    void shouldMapTopicRequestToTopicEntity() {
        UUID languageId = UUID.randomUUID();
        TopicRequest request = TopicRequest.builder()
                .languageId(languageId)
                .name("Test Topic")
                .description("Test Description")
                .difficulty(ProficiencyLevel.B1)
                .isActive(true)
                .build();

        Topic entity = topicMapper.mapTopicRequestToMapTopicEntities(request);

        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo(request.getName());
        assertThat(entity.getDescription()).isEqualTo(request.getDescription());
        assertThat(entity.getDifficulty()).isEqualTo(request.getDifficulty());
        assertThat(entity.getIsActive()).isEqualTo(request.getIsActive());
        // languageId n'est pas mappé directement car Topic a une entité Language
    }

    @Test
    @DisplayName("Should map Topic entity to TopicResponse")
    void shouldMapTopicEntityToTopicResponse() {
        UUID topicId = UUID.randomUUID();
        UUID languageId = UUID.randomUUID();
        Language language = Language.builder()
                .id(languageId)
                .code("FR")
                .name("Français")
                .build();

        Topic entity = Topic.builder()
                .id(topicId)
                .language(language)
                .name("Test Topic")
                .description("Test Description")
                .difficulty(ProficiencyLevel.B2)
                .isActive(true)
                .build();

        TopicResponse response = topicMapper.mapTopicEntitiesToTopicResponse(entity);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(entity.getId());
        assertThat(response.getName()).isEqualTo(entity.getName());
        assertThat(response.getDescription()).isEqualTo(entity.getDescription());
        assertThat(response.getDifficulty()).isEqualTo(entity.getDifficulty());
        assertThat(response.getIsActive()).isEqualTo(entity.getIsActive());

        // Vérification des clés étrangères / données liées
        assertThat(response.getLanguageId()).isEqualTo(languageId);
    }
}
