package com.glotrush.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.glotrush.enumerations.ReviewStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopicReviewResponse {
    
    private UUID id;
    private UUID accountId;
    private String username;
    private UUID topicId;
    private Integer rating;
    private String comment;
    private String topicName;
    private ReviewStatus status;
    private LocalDateTime createdAt;
}
