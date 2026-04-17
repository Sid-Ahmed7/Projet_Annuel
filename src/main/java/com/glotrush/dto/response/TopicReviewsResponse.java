package com.glotrush.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopicReviewsResponse {
    private List<TopicReviewResponse> reviews;
    private Double averageRating;
    private Integer totalReviews;
    
}
