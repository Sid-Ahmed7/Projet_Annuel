package com.glotrush.mapping;

import com.glotrush.dto.request.LanguageRequest;
import com.glotrush.dto.response.LanguageResponse;
import com.glotrush.entities.Language;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class LanguageMapper {
    @Mapping(target = "id", ignore = true)
    public abstract Language mapLanguageRequestToMapLanguageEntities(LanguageRequest languageRequest);

    public abstract LanguageResponse mapLanguageEntitiesToLanguageResponse(Language language);
}
