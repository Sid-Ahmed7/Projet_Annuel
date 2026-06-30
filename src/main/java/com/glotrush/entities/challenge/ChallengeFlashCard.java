package com.glotrush.entities.challenge;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "challenge_flashcards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeFlashCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Column(nullable = false)
    private String front;

    @Column(nullable = false)
    private String back;

    @Column(name = "front_language", length = 10)
    private String frontLanguage;

    @Column(name = "back_language", length = 10)
    private String backLanguage;

    @Column(name = "time_limit_seconds")
    @Builder.Default
    private Integer timeLimitSeconds = 30;
    
}
