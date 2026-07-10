package com.glotrush.dto.request.lesson;

import com.glotrush.dto.request.LessonRequest;
import com.glotrush.dto.request.exercice.InteractiveQuestionRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class InteractiveLessonRequest extends LessonRequest {

    @NotNull
    @Size(min = 3, max = 10)
    private List<InteractiveQuestionRequest> interactiveQuestions;
}
