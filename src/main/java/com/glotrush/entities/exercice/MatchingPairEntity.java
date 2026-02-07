package com.glotrush.entities.exercice;

import com.glotrush.entities.lesson.MatchingPairLesson;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
public class MatchingPairEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String item1; // Mot en langue 1
    private String item2; // Mot en langue 2
    @ManyToOne
    private MatchingPairLesson lesson;
}
