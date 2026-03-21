package com.glotrush.entities.lesson;

import com.glotrush.entities.Lesson;
import com.glotrush.entities.exercice.FlashcardEntity;
import com.glotrush.entities.exercice.QcmQuestionEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("QCM")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class QcmLesson extends Lesson {
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true, fetch = jakarta.persistence.FetchType.EAGER)
    private List<QcmQuestionEntity> questions = new ArrayList<>();

    public void addQuestion(QcmQuestionEntity question) {
        questions.add(question);
        question.setLesson(this);
    }

    public void removeQuestion(QcmQuestionEntity question) {
        questions.remove(question);
        question.setLesson(null);
    }
}
