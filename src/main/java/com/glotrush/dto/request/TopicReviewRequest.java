package com.glotrush.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TopicReviewRequest {
    
    @NotNull(message = "{error.rating.required}")
    @Min(value = 1, message = "{error.rating.min}")
    @Max(value = 5, message = "{error.rating.max}")
    private Integer rating;

    @Size(max = 1000, message = "{error.comment.maxLength}")
    private String comment;
}
