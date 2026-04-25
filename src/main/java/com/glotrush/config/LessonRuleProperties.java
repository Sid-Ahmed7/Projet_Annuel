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
    
    @NotNull(message = "La configuration glotrush.lesson.rules.xp-per-flashcard est obligatoire")
    private Integer xpPerFlashcard;

    @NotNull(message = "La configuration glotrush.lesson.rules.seconds-per-flashcard est obligatoire")
    private Integer secondsPerFlashcard;

    @NotNull(message = "La configuration glotrush.lesson.rules.xp-per-qcm est obligatoire")
    private Integer xpPerQcm;

    @NotNull(message = "La configuration glotrush.lesson.rules.seconds-per-qcm est obligatoire")
    private Integer secondsPerQcm;

    @NotNull(message = "La configuration glotrush.lesson.rules.matching-pair-fixed-xp est obligatoire")
    private Integer matchingPairFixedXp;

    @NotNull(message = "La configuration glotrush.lesson.rules.matching-pair-fixed-seconds est obligatoire")
    private Integer matchingPairFixedSeconds;

    @NotNull(message = "La configuration glotrush.lesson.rules.sorting-fixed-xp est obligatoire")
    private Integer sortingFixedXp;

    @NotNull(message = "La configuration glotrush.lesson.rules.sorting-fixed-seconds est obligatoire")
    private Integer sortingFixedSeconds;
}
