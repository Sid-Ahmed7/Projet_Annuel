package com.glotrush.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class LessonRulePropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Configuration
    @EnableConfigurationProperties(LessonRuleProperties.class)
    static class TestConfig {
    }

    @Test
    @DisplayName("Should successfully bind all properties when provided")
    void shouldBindProperties() {
        contextRunner.withPropertyValues(
                "glotrush.lesson.rules.xp-per-flashcard=5",
                "glotrush.lesson.rules.seconds-per-flashcard=30",
                "glotrush.lesson.rules.xp-per-qcm=10",
                "glotrush.lesson.rules.seconds-per-qcm=60",
                "glotrush.lesson.rules.matching-pair-fixed-xp=50",
                "glotrush.lesson.rules.matching-pair-fixed-seconds=300",
                "glotrush.lesson.rules.sorting-fixed-xp=60",
                "glotrush.lesson.rules.sorting-fixed-seconds=360"
        ).run(context -> {
            assertThat(context).hasSingleBean(LessonRuleProperties.class);
            LessonRuleProperties properties = context.getBean(LessonRuleProperties.class);
            assertThat(properties.getXpPerFlashcard()).isEqualTo(5);
            assertThat(properties.getSecondsPerFlashcard()).isEqualTo(30);
            assertThat(properties.getMatchingPairFixedXp()).isEqualTo(50);
        });
    }

    @Test
    @DisplayName("Should fail startup when mandatory property is missing")
    void shouldFailWhenPropertyMissing() {
        contextRunner.withPropertyValues(
                "glotrush.lesson.rules.xp-per-flashcard=5"
                // Other properties missing
        ).run(context -> {
            assertThat(context).hasFailed();
            assertThat(context).getFailure().hasStackTraceContaining("obligatoire");
        });
    }

    @Test
    @DisplayName("Should use snake-case mapping correctly")
    void shouldHandleSnakeCaseMapping() {
        contextRunner.withPropertyValues(
                "glotrush.lesson.rules.xp_per_flashcard=10",
                "glotrush.lesson.rules.seconds_per_flashcard=45",
                "glotrush.lesson.rules.xp_per_qcm=20",
                "glotrush.lesson.rules.seconds_per_qcm=90",
                "glotrush.lesson.rules.matching_pair_fixed_xp=100",
                "glotrush.lesson.rules.matching_pair_fixed_seconds=600",
                "glotrush.lesson.rules.sorting_fixed_xp=120",
                "glotrush.lesson.rules.sorting_fixed_seconds=720"
        ).run(context -> {
            LessonRuleProperties properties = context.getBean(LessonRuleProperties.class);
            assertThat(properties.getXpPerFlashcard()).isEqualTo(10);
            assertThat(properties.getSortingFixedSeconds()).isEqualTo(720);
        });
    }
}
