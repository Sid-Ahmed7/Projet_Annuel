package com.glotrush.entities.lesson;

import com.glotrush.entities.Lesson;
import com.glotrush.entities.exercice.QcmQuestionEntity;
import com.glotrush.entities.exercice.SortingExerciseEntity;
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
@DiscriminatorValue("SORTING_EXERCISE")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SortingExerciseLesson extends Lesson {
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SortingExerciseEntity> sortingExerciseEntities = new ArrayList<>();

    public void addSortingExercise(SortingExerciseEntity sortingExerciseEntity) {
        sortingExerciseEntities.add(sortingExerciseEntity);
        sortingExerciseEntity.setLesson(this);
    }

    public void removeSoringExercise(SortingExerciseEntity sortingExerciseEntity) {
        sortingExerciseEntities.remove(sortingExerciseEntity);
        sortingExerciseEntity.setLesson(this);
    }
}