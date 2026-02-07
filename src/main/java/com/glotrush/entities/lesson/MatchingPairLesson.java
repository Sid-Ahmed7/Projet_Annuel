package com.glotrush.entities.lesson;

import com.glotrush.entities.Lesson;
import com.glotrush.entities.exercice.FlashcardEntity;
import com.glotrush.entities.exercice.MatchingPairEntity;
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
@DiscriminatorValue("MATCHING_PAIR")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MatchingPairLesson extends Lesson {
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MatchingPairEntity> matchingPairEntities = new ArrayList<>();

    public void addMatchingPair(MatchingPairEntity matchingPairEntity) {
        matchingPairEntities.add(matchingPairEntity);
        matchingPairEntity.setLesson(this);
    }

    public void removeMatchingPair(MatchingPairEntity matchingPairEntity) {
        matchingPairEntities.remove(matchingPairEntity);
        matchingPairEntity.setLesson(null);
    }
}
