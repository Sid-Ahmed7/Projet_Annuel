package com.glotrush.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_progress", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"account_id", "topic_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Accounts account;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(name = "total_xp", nullable = false)
    private Long totalXP = 0L;

    @Column(name = "completed_lessons", nullable = false)
    private Integer completedLessons = 0;

    @Column(name = "completion_percentage", nullable = false)
    private Double completionPercentage = 0.0;

    @Column(name = "correct_answers", nullable = false)
    private Integer correctAnswers = 0;

    @Column(name = "total_answers", nullable = false)
    private Integer totalAnswers = 0;

    @Column(name = "accuracy", nullable = false)
    private Double accuracy = 0.0; 

    @Column(name = "study_streak", nullable = false)
    private Integer studyStreak = 0; 

    @Column(name = "last_studied_at")
    private LocalDateTime lastStudiedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void calculateAccuracy() {
        if(this.totalAnswers > 0) {
            this.accuracy = this.correctAnswers.doubleValue() / this.totalAnswers.doubleValue();
        } else {
            this.accuracy = 0.0;
        }
    }

    public void calculateCompletionPercentage(Integer totalLesson) {
        if(totalLesson > 0) {
            this.completionPercentage = this.completedLessons.doubleValue() / totalLesson.doubleValue() * 100.0;
        } else {
            this.completionPercentage = 0.0;
        }
    }
}
