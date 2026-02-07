package com.glotrush.dto.response.exercice;

import com.glotrush.dto.request.LessonRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MatchingPairResponse extends LessonRequest {
    @NotNull
    @NotEmpty
    private String item1;
    @NotNull
    @NotEmpty
    private String item2;
}
