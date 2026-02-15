package com.glotrush.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "lesson")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // Toutes les sous-classes dans une seule table
@DiscriminatorColumn(name = "lesson_type", discriminatorType = DiscriminatorType.STRING) // Colonne pour distinguer les types
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 0;

    @Column(name = "xp_reward", nullable = false)
    private Integer xpReward = 10;

    @Column(name = "is_locked", nullable = false)
    private Boolean isLocked = false; // TODO SUPPRIMER IS LOCKED

    @Column(name = "min_level_required")
    private Integer minLevelRequired;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
