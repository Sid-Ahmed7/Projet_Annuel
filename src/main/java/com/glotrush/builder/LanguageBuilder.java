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
        return mapToLanguageResponse(language, null, 0, 0, false);
    }

    public LanguageResponse mapToLanguageResponse(Language language, String levelRange, int topicsCount, int lessonsCount, boolean isPopular) {
        Objects.requireNonNull(language, "Language must not be null");

        return LanguageResponse.builder()
                .id(language.getId())
                .code(language.getCode())
                .name(language.getName())
                .levelRange(levelRange)
                .topicsCount(topicsCount)
                .lessonsCount(lessonsCount)
                .isActive(language.getIsActive())
                .orderIndex(language.getOrderIndex())
                .isPopular(isPopular)
                .build();
    }
}
