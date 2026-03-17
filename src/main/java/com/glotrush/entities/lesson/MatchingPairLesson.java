package com.glotrush.entities.lesson;

import com.glotrush.entities.Lesson;
import com.glotrush.entities.exercice.FlashcardEntity;
import com.glotrush.entities.exercice.MatchingPairEntity;
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
@DiscriminatorValue("MATCHING_PAIR")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MatchingPairLesson extends Lesson {
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true, fetch = jakarta.persistence.FetchType.EAGER)
    private List<MatchingPairEntity> matchingPair = new ArrayList<>();

    public void addMatchingPair(MatchingPairEntity matchingPairEntity) {
        matchingPair.add(matchingPairEntity);
        matchingPairEntity.setLesson(this);
    }

    public void removeMatchingPair(MatchingPairEntity matchingPairEntity) {
        matchingPair.remove(matchingPairEntity);
        matchingPairEntity.setLesson(null);
    }
}
