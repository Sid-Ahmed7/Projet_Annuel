package com.glotrush.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.glotrush.enumerations.LearningType;
import com.glotrush.enumerations.LessonType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_mistakes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMistake {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Accounts account;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "lesson_type", nullable = false)
    private LessonType lessonType;

    @Enumerated(EnumType.STRING)
    @Column(name = "learning_type", nullable = false)
    private LearningType learningType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;


    @Column(name = "rate_multiplier", nullable = false)
    @Builder.Default
    private Double rateMultiplier = 2.5;

    @Column(name ="interval_hours", nullable = false)
    @Builder.Default
    private Integer intervalHours = 24;

    @Column(name= "consecutive_good_answers", nullable = false)
    @Builder.Default
    private Integer consecutiveGoodAnswers = 0;

    @Column(name = "next_review_at", nullable = false)
    @Builder.Default
    private LocalDateTime nextReviewAt = LocalDateTime.now();

    @Column(name = "is_resolved", nullable = false)
    @Builder.Default
    private Boolean isResolved = false;

    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
