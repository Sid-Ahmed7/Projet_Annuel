CREATE TABLE IF NOT EXISTS challenge_interactives (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    challenge_id UUID NOT NULL REFERENCES challenges(id) ON DELETE CASCADE,
    question_text VARCHAR(500),
    system_type VARCHAR(50) NOT NULL,
    correct_option_index INTEGER,
    correct_word VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS challenge_interactive_options (
    interactive_id UUID NOT NULL REFERENCES challenge_interactives(id) ON DELETE CASCADE,
    option_text VARCHAR(255) NOT NULL,
    PRIMARY KEY (interactive_id, option_text)
);

CREATE TABLE IF NOT EXISTS challenge_interactive_images (
    interactive_id UUID NOT NULL REFERENCES challenge_interactives(id) ON DELETE CASCADE,
    image_path VARCHAR(255) NOT NULL,
    PRIMARY KEY (interactive_id, image_path)
);

CREATE TABLE IF NOT EXISTS challenge_interactive_audios (
    interactive_id UUID NOT NULL REFERENCES challenge_interactives(id) ON DELETE CASCADE,
    audio_path VARCHAR(255) NOT NULL,
    PRIMARY KEY (interactive_id, audio_path)
);
