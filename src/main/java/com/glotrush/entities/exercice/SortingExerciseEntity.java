package com.glotrush.entities.exercice;

import com.glotrush.entities.lesson.SortingExerciseLesson;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Entity
@Data
public class SortingExerciseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ElementCollection
    private List<String> items; // Mots à trier
    @ElementCollection
    private List<Integer> correctOrder; // Ordre correct
    @ManyToOne
    private SortingExerciseLesson lesson;
}