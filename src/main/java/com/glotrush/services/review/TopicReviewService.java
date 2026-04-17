package com.glotrush.services.review;

import java.util.List;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.glotrush.builder.TopicReviewBuilder;
import com.glotrush.constants.TopicConstants;
import com.glotrush.dto.request.TopicReviewRequest;
import com.glotrush.dto.response.TopicReviewResponse;
import com.glotrush.dto.response.TopicReviewsResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Topic;
import com.glotrush.entities.TopicReview;
import com.glotrush.enumerations.LessonStatus;
import com.glotrush.enumerations.ReviewStatus;
import com.glotrush.exceptions.ReviewAlreadyExistsException;
import com.glotrush.exceptions.ReviewBannedException;
import com.glotrush.exceptions.ReviewNotAllowedException;
import com.glotrush.exceptions.ReviewNotFoundException;
import com.glotrush.exceptions.TopicNotFoundException;
import com.glotrush.exceptions.UserNotFoundException;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.LessonRepository;
import com.glotrush.repositories.TopicRepository;
import com.glotrush.repositories.TopicReviewRepository;
import com.glotrush.repositories.UserLessonProgressRepository;
import com.glotrush.services.moderation.ModerationService;
import com.glotrush.utils.LocaleUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TopicReviewService implements ITopicReviewService {

    private final TopicReviewRepository topicReviewRepository;
    private final TopicRepository topicRepository;
    private final AccountsRepository accountsRepository;
    private final LessonRepository lessonRepository;
    private final UserLessonProgressRepository userLessonProgressRepository;
    private final TopicReviewBuilder topicReviewBuilder;
    private final ModerationService moderationService;
    private final MessageSource messageSource;


    @Override
    public TopicReviewResponse addReview(UUID accountId, UUID topicId, TopicReviewRequest data) {
        
        Accounts account = accountsRepository.findById(accountId).orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.user.not_found", null, LocaleUtils.getCurrentLocale())));
        if(Boolean.TRUE.equals(account.getIsBannedOfReview())) {
            throw new ReviewBannedException(messageSource.getMessage("error.review.banned", null, LocaleUtils.getCurrentLocale()));
        }

        Topic topic = topicRepository.findById(topicId).orElseThrow(() -> new TopicNotFoundException(messageSource.getMessage("error.topic.notfound", null, LocaleUtils.getCurrentLocale())));

        if(topicReviewRepository.existsByAccount_IdAndTopic_Id(accountId, topicId)) {
            throw new ReviewAlreadyExistsException(messageSource.getMessage("error.review.already_exists", null, LocaleUtils.getCurrentLocale()));
        }

        long totalLessons = lessonRepository.findByTopic_IdAndIsActiveTrueOrderByOrderIndexAsc(topicId).size();
        long completedLessons = userLessonProgressRepository.findByAccount_Id(accountId).stream().filter(progress -> progress.getLesson().getTopic().getId().equals(topicId) && progress.getStatus() == LessonStatus.COMPLETED).count();
        if(completedLessons < totalLessons) {
            throw new ReviewNotAllowedException(messageSource.getMessage("error.review.not_allowed", null, LocaleUtils.getCurrentLocale()));
        }

        boolean flagged = moderationService.isContentToxic(data.getComment());
        ReviewStatus status = flagged ? ReviewStatus.PENDING : ReviewStatus.PUBLISHED;
        
        TopicReview review = TopicReview.builder()
                .account(account)
                .topic(topic)
                .rating(data.getRating())
                .comment(data.getComment())
                .status(status)
                .build();

        // if(flagged) {
        //     String warningMessage = messageSource.getMessage("warning.review.flagged", null, LocaleUtils.getCurrentLocale());
        // }

        return topicReviewBuilder.buildTopicReviewResponse(topicReviewRepository.save(review));
    }


    @Override
    public TopicReviewResponse updateReview(UUID accountId, UUID reviewId, TopicReviewRequest data) {
        TopicReview review = topicReviewRepository.findByIdAndAccount_Id(reviewId, accountId).orElseThrow(() -> new ReviewNotFoundException(messageSource.getMessage("error.review.not_found", null, LocaleUtils.getCurrentLocale())));
        
        boolean flagged = moderationService.isContentToxic(data.getComment());
        ReviewStatus status = flagged ? ReviewStatus.PENDING : ReviewStatus.PUBLISHED;

        review.setRating(data.getRating());
        review.setComment(data.getComment());
        review.setStatus(status);

        return topicReviewBuilder.buildTopicReviewResponse(topicReviewRepository.save(review));
    }


    @Override
    public void deleteReview(UUID reviewId) {
        if(!topicReviewRepository.existsById(reviewId)) {
            throw new ReviewNotFoundException(messageSource.getMessage("error.review.not_found", null, LocaleUtils.getCurrentLocale()));
        }
        topicReviewRepository.deleteById(reviewId);
    }


    @Override
    public List<TopicReviewResponse> getAllReview() {
        return topicReviewRepository.findAll().stream()
                .map(topicReviewBuilder::buildTopicReviewResponse)
                .toList();
    }

    
    @Override
    public TopicReviewResponse getUserReview(UUID accountId, UUID topicId) {
        return topicReviewRepository.findByAccount_IdAndTopic_Id(accountId, topicId)
                .map(topicReviewBuilder::buildTopicReviewResponse)
                .orElse(null);
    }

    @Override
    public List<TopicReviewResponse> getPendingReview() {
        return topicReviewRepository.findByStatus(ReviewStatus.PENDING).stream()
                .map(topicReviewBuilder::buildTopicReviewResponse)
                .toList();
    }


    @Override
    public TopicReviewResponse acceptReview(UUID reviewId) {
        TopicReview review = topicReviewRepository.findById(reviewId).orElseThrow(() -> new ReviewNotFoundException(messageSource.getMessage("error.review.not_found", null, LocaleUtils.getCurrentLocale())));
        review.setStatus(ReviewStatus.PUBLISHED);
        topicReviewRepository.save(review);

        return topicReviewBuilder.buildTopicReviewResponse(review);
    }


    @Override
    public void rejectReview(UUID reviewId) {
        TopicReview review = topicReviewRepository.findById(reviewId).orElseThrow(() -> new ReviewNotFoundException(messageSource.getMessage("error.review.not_found", null, LocaleUtils.getCurrentLocale())));
        review.setStatus(ReviewStatus.REJECTED);
        topicReviewRepository.save(review);
        Accounts account = review.getAccount();
        int nbRejectedCount = account.getRejectedReviewCount() + 1;
        account.setRejectedReviewCount(nbRejectedCount);

        if(nbRejectedCount >= TopicConstants.LIMIT_REJECTED_REVIEWS) {
            account.setIsBannedOfReview(true);
            accountsRepository.save(account);
        } else {
            accountsRepository.save(account);
        }
    }


    @Override
    public TopicReviewsResponse getTopicReviews(UUID topicId) {
        if(!topicRepository.existsById(topicId)) {
            throw new TopicNotFoundException(messageSource.getMessage("error.topic.notfound", null, LocaleUtils.getCurrentLocale()));
        }
        List<TopicReview> reviews = topicReviewRepository.findByTopic_IdAndStatus(topicId, ReviewStatus.PUBLISHED);
        double averageRating = reviews.stream().mapToInt(TopicReview::getRating).average().orElse(0.0);
        return TopicReviewsResponse.builder()
                .averageRating(Math.round(averageRating * 10.0) / 10.0)
                .reviews(reviews.stream().map(topicReviewBuilder::buildTopicReviewResponse).toList())
                .totalReviews(reviews.size())
                .build();
    }


    

    
}
