package com.glotrush.entities.challenge;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.glotrush.entities.Accounts;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "challenge_participants", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"challenge_id", "account_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeParticipant {
    
    @Id
    @GeneratedValue(strategy =  GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Accounts account;

    @Column(name= "score")
    private Double score;

    @Column(name = "time_passed")
    private Long timePassed; 

    @Column(name = "xp_gained")
    @Builder.Default
    private Long xpGained = 0L;

    @Column(name = "final_rank")
    private Integer finalRank;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;


    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;


    @Column(name = "score_recorded")
    @Builder.Default
    private Boolean scoreRecorded = false;
}
