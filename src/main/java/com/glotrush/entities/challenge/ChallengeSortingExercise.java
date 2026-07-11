package com.glotrush.entities.challenge;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "challenge_sorting_exercises")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeSortingExercise {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @ElementCollection
    @CollectionTable(name = "challenge_sorting_items", joinColumns = @JoinColumn(name = "exercise_id"))
    @Column(name = "item_value")
    @OrderColumn(name = "item_position")
    private List<String> items;

    @ElementCollection
    @CollectionTable(name = "challenge_sorting_order", joinColumns = @JoinColumn(name = "exercise_id"))
    @Column(name = "order_index")
    @OrderColumn(name = "order_position")
    private List<Integer> correctOrder;
}
