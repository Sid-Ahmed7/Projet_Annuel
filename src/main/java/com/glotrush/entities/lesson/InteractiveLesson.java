package com.glotrush.entities.lesson;

import com.glotrush.entities.Lesson;
import com.glotrush.entities.exercice.InteractiveQuestionEntity;
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
@DiscriminatorValue("INTERACTIVE")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class InteractiveLesson extends Lesson {

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true, fetch = jakarta.persistence.FetchType.EAGER)
    private List<InteractiveQuestionEntity> questions = new ArrayList<>();

    public void addQuestion(InteractiveQuestionEntity question) {
        questions.add(question);
        question.setLesson(this);
    }

    public void removeQuestion(InteractiveQuestionEntity question) {
        questions.remove(question);
        question.setLesson(null);
    }
}
