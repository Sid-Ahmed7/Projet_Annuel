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

        request.setItemCount(4);
        Set<ConstraintViolation<AILessonGenerateRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();

        request.setItemCount(5);
        violations = validator.validate(request);
        assertThat(violations).isEmpty();

        request.setItemCount(20);
        violations = validator.validate(request);
        assertThat(violations).isEmpty();

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

        request.setItemCount(2);
        Set<ConstraintViolation<AILessonGenerateRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();

        request.setItemCount(3);
        violations = validator.validate(request);
        assertThat(violations).isEmpty();

        request.setItemCount(10);
        violations = validator.validate(request);
        assertThat(violations).isEmpty();

        request.setItemCount(11);
        violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("Should validate Interactive count boundaries (3-10)")
    void shouldValidateInteractiveBoundaries() {
        AILessonGenerateRequest request = new AILessonGenerateRequest();
        request.setTopicId(UUID.randomUUID());
        request.setDescription("Valid description");
        request.setLessonType(LessonType.INTERACTIVE);

        request.setItemCount(2);
        Set<ConstraintViolation<AILessonGenerateRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();

        request.setItemCount(3);
        violations = validator.validate(request);
        assertThat(violations).isEmpty();

        request.setItemCount(10);
        violations = validator.validate(request);
        assertThat(violations).isEmpty();

        request.setItemCount(11);
        violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }
}
