package com.glotrush.entities.exercice;

import com.glotrush.entities.lesson.FlashcardLesson;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
public class FlashcardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String front; // Mot/image/URL audio
    private String back; // Traduction/réponse
    private String frontLanguage; // Code de la langue (ex: "fr")
    private String backLanguage; // Code de la langue (ex: "en")
    @ManyToOne
    private FlashcardLesson lesson;
}