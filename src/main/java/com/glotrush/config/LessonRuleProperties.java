package com.glotrush.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "glotrush.lesson.rules")
@Data
@Validated
public class LessonRuleProperties {
    
    @NotNull(message = "{validation.lesson.rules.xp-per-flashcard.required}")
    private Integer xpPerFlashcard;

    @NotNull(message = "{validation.lesson.rules.seconds-per-flashcard.required}")
    private Integer secondsPerFlashcard;

    @NotNull(message = "{validation.lesson.rules.xp-per-qcm.required}")
    private Integer xpPerQcm;

    @NotNull(message = "{validation.lesson.rules.seconds-per-qcm.required}")
    private Integer secondsPerQcm;

    @NotNull(message = "{validation.lesson.rules.matching-pair-fixed-xp.required}")
    private Integer matchingPairFixedXp;

    @NotNull(message = "{validation.lesson.rules.matching-pair-fixed-seconds.required}")
    private Integer matchingPairFixedSeconds;

    @NotNull(message = "{validation.lesson.rules.sorting-fixed-xp.required}")
    private Integer sortingFixedXp;

    @NotNull(message = "{validation.lesson.rules.sorting-fixed-seconds.required}")
    private Integer sortingFixedSeconds;
}
