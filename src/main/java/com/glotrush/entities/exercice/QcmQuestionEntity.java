package com.glotrush.entities.exercice;

import com.glotrush.entities.lesson.QcmLesson;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Entity
@Data
public class QcmQuestionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String question;
    @ElementCollection
    private List<String> options; // Réponses possibles
    private Integer correctOptionIndex; // Index de la bonne réponse
    private String explanation; // Explication de la réponse
    @ManyToOne
    private QcmLesson lesson;
}