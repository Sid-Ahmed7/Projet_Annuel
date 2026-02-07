package com.glotrush.dto.request.lesson;

import com.glotrush.dto.request.LessonRequest;
import com.glotrush.dto.request.exercice.MatchingPairRequest;
import com.glotrush.dto.request.exercice.QcmQuestionRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class QcmLessonRequest extends LessonRequest {
    private List<QcmQuestionRequest> qcmQuestionRequests;

}
