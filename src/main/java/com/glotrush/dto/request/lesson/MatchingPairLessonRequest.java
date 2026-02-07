package com.glotrush.dto.request.lesson;

import com.glotrush.dto.request.LessonRequest;
import com.glotrush.dto.request.exercice.FlashcardRequest;
import com.glotrush.dto.request.exercice.MatchingPairRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class MatchingPairLessonRequest extends LessonRequest {
    private List<MatchingPairRequest> matchingPairRequests;
}
