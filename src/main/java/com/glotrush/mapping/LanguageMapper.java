package com.glotrush.mapping;

import com.glotrush.dto.request.LanguageRequest;
import com.glotrush.dto.response.LanguageResponse;
import com.glotrush.entities.Language;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class LanguageMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    public abstract Language mapLanguageRequestToMapLanguageEntities(LanguageRequest languageRequest);

    public abstract LanguageResponse mapLanguageEntitiesToLanguageResponse(Language language);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    public abstract void updateLanguageFromRequest(LanguageRequest languageRequest, @MappingTarget Language language);
}
