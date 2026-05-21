package com.glotrush.dto.request.ai;

import com.glotrush.enumerations.LessonType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AILessonGenerateRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should validate Flashcard and QCM count boundaries (5-20)")
    void shouldValidateFlashcardAndQcmBoundaries() {
        AILessonGenerateRequest request = new AILessonGenerateRequest();
        request.setTopicId(UUID.randomUUID());
        request.setDescription("Valid description");
        request.setLessonType(LessonType.FLASHCARD);

        // Under min (4)
        request.setItemCount(4);
        Set<ConstraintViolation<AILessonGenerateRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();

        // Valid min (5)
        request.setItemCount(5);
        violations = validator.validate(request);
        assertThat(violations).isEmpty();

        // Valid max (20)
        request.setItemCount(20);
        violations = validator.validate(request);
        assertThat(violations).isEmpty();

        // Over max (21)
        request.setItemCount(21);
        violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("Should validate Matching Pair and Sorting count boundaries (3-10)")
    void shouldValidateMatchingPairAndSortingBoundaries() {
        AILessonGenerateRequest request = new AILessonGenerateRequest();
        request.setTopicId(UUID.randomUUID());
        request.setDescription("Valid description");
        request.setLessonType(LessonType.SORTING_EXERCISE);

        // Under min (2)
        request.setItemCount(2);
        Set<ConstraintViolation<AILessonGenerateRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();

        // Valid min (3)
        request.setItemCount(3);
        violations = validator.validate(request);
        assertThat(violations).isEmpty();

        // Valid max (10)
        request.setItemCount(10);
        violations = validator.validate(request);
        assertThat(violations).isEmpty();

        // Over max (11)
        request.setItemCount(11);
        violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }
}
