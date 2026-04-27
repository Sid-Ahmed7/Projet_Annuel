package com.glotrush.dto.response.lesson;

import com.glotrush.dto.response.LessonResponse;
import com.glotrush.dto.response.exercice.FlashcardResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class FlashcardLessonResponse extends LessonResponse {
    private List<FlashcardResponse> flashcards;
}
