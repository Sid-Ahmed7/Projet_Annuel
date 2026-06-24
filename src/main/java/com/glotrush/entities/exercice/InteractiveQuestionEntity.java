package com.glotrush.entities.exercice;

import com.glotrush.entities.lesson.InteractiveLesson;
import com.glotrush.enumerations.InteractiveSystemType;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "interactive_questions")
@Data
public class InteractiveQuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private InteractiveLesson lesson;

    @Column(name = "question_text", length = 500)
    private String questionText;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "interactive_question_images", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "image_path")
    private List<String> imagePaths = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "interactive_question_audios", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "audio_path")
    private List<String> audioPaths = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "system_type", nullable = false)
    private InteractiveSystemType systemType;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "interactive_question_options", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "option_text")
    private List<String> options = new ArrayList<>();

    @Column(name = "correct_option_index")
    private Integer correctOptionIndex;

    @Column(name = "correct_word")
    private String correctWord;
}
