package com.glotrush.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.glotrush.entities.TopicReview;
import com.glotrush.enumerations.ReviewStatus;

@Repository
public interface TopicReviewRepository extends JpaRepository<TopicReview, UUID> {

    List<TopicReview> findByTopic_Id(UUID topicId);
    List<TopicReview> findByTopic_IdAndStatus(UUID topicId, ReviewStatus status);
    List<TopicReview> findByStatus(ReviewStatus status);
    boolean existsByAccount_IdAndTopic_Id(UUID accountId, UUID topicId);
    
    Optional<TopicReview> findByIdAndAccount_Id(UUID reviewId, UUID accountId);
    Optional<TopicReview> findByAccount_IdAndTopic_Id(UUID accountId, UUID topicId);

    List<TopicReview> findByStatusAndUpdatedAtBefore(ReviewStatus status, LocalDateTime lastDelay);

}
    
