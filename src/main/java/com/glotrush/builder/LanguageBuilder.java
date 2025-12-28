package com.glotrush.builder;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.glotrush.dto.response.LanguageResponse;
import com.glotrush.entities.Language;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LanguageBuilder {
    public LanguageResponse mapToLanguageResponse(Language language) {
        Objects.requireNonNull(language, "Language must not be null");

        return LanguageResponse.builder()
                .id(language.getId())
                .code(language.getCode())
                .name(language.getName())
                .isActive(language.getIsActive())
                .orderIndex(language.getOrderIndex())
                .build();
    }
}
