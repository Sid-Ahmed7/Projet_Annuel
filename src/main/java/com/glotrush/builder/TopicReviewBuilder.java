package com.glotrush.builder;

import org.springframework.stereotype.Component;

import com.glotrush.dto.response.TopicReviewResponse;
import com.glotrush.entities.TopicReview;

@Component
public class TopicReviewBuilder {

    public TopicReviewResponse buildTopicReviewResponse(TopicReview review) {
        return TopicReviewResponse.builder()
                .id(review.getId())
                .accountId(review.getAccount().getId())
                .username(review.getAccount().getUsername())
                .rating(review.getRating())
                .comment(review.getComment())
                .topicId(review.getTopic().getId())
                .topicName(review.getTopic().getName())
                .status(review.getStatus())
                .createdAt(review.getCreatedAt())
                .build();
    }
    
}
