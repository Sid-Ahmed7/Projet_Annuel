package com.glotrush.mapping;

import com.glotrush.dto.request.TopicRequest;
import com.glotrush.dto.response.TopicResponse;
import com.glotrush.entities.Topic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

@Mapper(componentModel = "spring")
public abstract class TopicMapper {

    // ALL MAPPING TYPE OF TOPIC
    @Mapping(target = "id", ignore = true)
    public abstract Topic mapTopicRequestToMapTopicEntities(TopicRequest topicRequest);

    @Mapping(source = "language.id", target = "languageId")
    public abstract TopicResponse mapTopicEntitiesToTopicResponse(Topic topic);

}
