package com.glotrush.dto.response.lesson;

import com.glotrush.dto.request.LessonRequest;
import com.glotrush.dto.response.LessonResponse;
import com.glotrush.dto.response.exercice.MatchingPairResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class MatchingPairLessonResponse extends LessonResponse {
    private List<MatchingPairResponse> matchingPair;
}
