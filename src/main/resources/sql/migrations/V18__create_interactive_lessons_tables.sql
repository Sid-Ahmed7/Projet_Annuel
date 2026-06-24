CREATE TABLE IF NOT EXISTS interactive_questions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id UUID NOT NULL REFERENCES lesson(id) ON DELETE CASCADE,
    question_text VARCHAR(500),
    system_type VARCHAR(50) NOT NULL,
    correct_option_index INTEGER,
    correct_word VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS interactive_question_options (
    question_id UUID NOT NULL REFERENCES interactive_questions(id) ON DELETE CASCADE,
    option_text VARCHAR(255) NOT NULL,
    PRIMARY KEY (question_id, option_text)
);

CREATE TABLE IF NOT EXISTS interactive_question_images (
    question_id UUID NOT NULL REFERENCES interactive_questions(id) ON DELETE CASCADE,
    image_path VARCHAR(255) NOT NULL,
    PRIMARY KEY (question_id, image_path)
);

CREATE TABLE IF NOT EXISTS interactive_question_audios (
    question_id UUID NOT NULL REFERENCES interactive_questions(id) ON DELETE CASCADE,
    audio_path VARCHAR(255) NOT NULL,
    PRIMARY KEY (question_id, audio_path)
);
