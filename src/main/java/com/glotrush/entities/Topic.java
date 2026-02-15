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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "topic")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(name = "name", nullable = false, length = 100)
    private String name; 

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "difficulty", nullable = false)
    private Integer difficulty; // TODO METTRE UNE ENUMERATION A1, A2

    @Column(name = "total_lessons", nullable = false)
    private Integer totalLessons = 0;// TODO SUPPRIMER

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
