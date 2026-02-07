package com.glotrush.entities.lesson;

import com.glotrush.entities.Lesson;
import com.glotrush.entities.exercice.FlashcardEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("FLASHCARD")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardLesson extends Lesson {
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FlashcardEntity> flashcards = new ArrayList<>();

    public void addFlashcard(FlashcardEntity flashcard) {
        flashcards.add(flashcard);
        flashcard.setLesson(this);
    }

    public void removeFlashcard(FlashcardEntity flashcard) {
        flashcards.remove(flashcard);
        flashcard.setLesson(null);
    }
}
