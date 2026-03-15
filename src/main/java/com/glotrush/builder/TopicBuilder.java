package com.glotrush.builder;

import java.util.Optional;

import com.glotrush.utils.LevelUtils;
import org.springframework.stereotype.Component;

import com.glotrush.dto.response.TopicResponse;
import com.glotrush.entities.Topic;
import com.glotrush.entities.UserProgress;

@Component
public class TopicBuilder {

    public TopicResponse mapToTopicResponse(Topic topic, Optional<UserProgress> progressOpt) {

        return TopicResponse.builder()
                .id(topic.getId())
                .languageId(topic.getLanguage().getId())
                .name(topic.getName())
                .description(topic.getDescription())
                .difficulty(topic.getDifficulty())
                .orderIndex(topic.getOrderIndex())
                .isActive(topic.getIsActive())
                .build();
    }
}
