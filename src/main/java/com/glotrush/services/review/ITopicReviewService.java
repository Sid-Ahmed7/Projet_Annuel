package com.glotrush.services.review;

import java.util.List;
import java.util.UUID;

import com.glotrush.dto.request.TopicReviewRequest;
import com.glotrush.dto.response.TopicReviewResponse;
import com.glotrush.dto.response.TopicReviewsResponse;

public interface ITopicReviewService {

    TopicReviewResponse addReview(UUID accountId, UUID topicId, TopicReviewRequest data);
    TopicReviewResponse updateReview(UUID accountId, UUID reviewId, TopicReviewRequest data);
    void deleteReview(UUID reviewId);    
    List<TopicReviewResponse> getAllReview();
    TopicReviewResponse getUserReview(UUID accountId, UUID topicId);
    TopicReviewsResponse getTopicReviews(UUID topicId);
    List<TopicReviewResponse> getPendingReview();
    TopicReviewResponse acceptReview(UUID reviewId);
    void rejectReview(UUID reviewId);
}
