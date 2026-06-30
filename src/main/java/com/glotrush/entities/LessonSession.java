package com.glotrush.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import com.glotrush.enumerations.LessonSessionStatus;

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
@Table(name = "lesson_session")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonSession {
    

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Accounts account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false)
    private Integer correctAnswers;

    @Column(nullable = false)
    private Integer wrongAnswers;

    @Column(nullable = false)
    private Integer totalQuestions;

    @Column(nullable = false)
    private Integer totalTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LessonSessionStatus status;    

    @Column(name = "complete_at", nullable = false)
    private LocalDateTime completedAt;
}
