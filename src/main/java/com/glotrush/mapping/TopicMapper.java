package com.glotrush.mapping;

import com.glotrush.dto.request.TopicRequest;
import com.glotrush.dto.response.TopicResponse;
import com.glotrush.entities.Topic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class TopicMapper {

    // ALL MAPPING TYPE OF TOPIC
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "language", ignore = true)
    public abstract Topic mapTopicRequestToMapTopicEntities(TopicRequest topicRequest);

    @Mapping(source = "language.id", target = "languageId")
    public abstract TopicResponse mapTopicEntitiesToTopicResponse(Topic topic);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "language", ignore = true)
    public abstract void updateTopicFromRequest(TopicRequest topicRequest, @MappingTarget Topic topic);

}
