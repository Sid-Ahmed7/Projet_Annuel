package com.glotrush.entities.challenge;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.glotrush.enumerations.InteractiveSystemType;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "challenge_interactives")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeInteractive {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Column(name = "question_text", length = 500)
    private String questionText;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "challenge_interactive_images", joinColumns = @JoinColumn(name = "interactive_id"))
    @Column(name = "image_path")
    @Builder.Default
    private List<String> imagePaths = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "challenge_interactive_audios", joinColumns = @JoinColumn(name = "interactive_id"))
    @Column(name = "audio_path")
    @Builder.Default
    private List<String> audioPaths = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "system_type", nullable = false)
    private InteractiveSystemType systemType;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "challenge_interactive_options", joinColumns = @JoinColumn(name = "interactive_id"))
    @Column(name = "option_text")
    @Builder.Default
    private List<String> options = new ArrayList<>();

    @Column(name = "correct_option_index")
    private Integer correctOptionIndex;

    @Column(name = "correct_word")
    private String correctWord;
}
