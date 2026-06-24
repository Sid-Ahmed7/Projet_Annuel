package com.glotrush.dto.request.lesson;

import com.glotrush.dto.request.LessonRequest;
import com.glotrush.dto.request.exercice.QcmQuestionRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class QcmLessonRequest extends LessonRequest {
    @NotNull
    @Size(min = 5, max = 20)
    private List<QcmQuestionRequest> questions;
}
