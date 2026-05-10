
CREATE TABLE IF NOT EXISTS challenges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    challenger_id UUID NOT NULL REFERENCES accounts(id),
    challenged_id UUID REFERENCES accounts(id),
    language_id UUID NOT NULL REFERENCES languages(id),
    challenge_type VARCHAR(20) NOT NULL,
    lesson_type VARCHAR(20) NOT NULL,
    challenge_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    title VARCHAR(200) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS challenges_qcm (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    challenge_id UUID NOT NULL REFERENCES challenges(id) ON DELETE CASCADE,
    question TEXT NOT NULL,
    correct_option_index INTEGER NOT NULL,
    explanation VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS challenge_qcm_options (
    question_id UUID NOT NULL REFERENCES challenges_qcm(id) ON DELETE CASCADE,
    option_value TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS challenge_flashcards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    challenge_id UUID NOT NULL REFERENCES challenges(id) ON DELETE CASCADE,
    front TEXT NOT NULL,
    back TEXT NOT NULL,
    front_language VARCHAR(10),
    back_language VARCHAR(10),
    time_limit_seconds INTEGER NOT NULL DEFAULT 30
);

CREATE TABLE IF NOT EXISTS challenge_participants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    challenge_id UUID NOT NULL REFERENCES challenges(id) ON DELETE CASCADE,
    account_id UUID NOT NULL REFERENCES accounts(id),
    score DOUBLE PRECISION,
    time_passed BIGINT,
    xp_gained INTEGER NOT NULL DEFAULT 0,
    final_rank INTEGER,
    completed_at TIMESTAMP,
    joined_at TIMESTAMP NOT NULL DEFAULT now(),
    score_recorded BOOLEAN NOT NULL DEFAULT false,
    UNIQUE(challenge_id, account_id)
);

CREATE TABLE IF NOT EXISTS challenge_matching_pairs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    challenge_id UUID NOT NULL REFERENCES challenges(id) ON DELETE CASCADE,
    item1 TEXT NOT NULL,
    item2 TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS challenge_sorting_exercises (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    challenge_id UUID NOT NULL REFERENCES challenges(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS challenge_sorting_items (
    exercise_id UUID NOT NULL REFERENCES challenge_sorting_exercises(id) ON DELETE CASCADE,
    item_value TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS challenge_sorting_order (
    exercise_id UUID NOT NULL REFERENCES challenge_sorting_exercises(id) ON DELETE CASCADE,
    order_index INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_challenges_challenger ON challenges(challenger_id);
CREATE INDEX IF NOT EXISTS idx_challenges_challenged ON challenges(challenged_id);
CREATE INDEX IF NOT EXISTS idx_challenges_status ON challenges(challenge_status);
CREATE INDEX IF NOT EXISTS idx_challenges_expires_at ON challenges(expires_at);
CREATE INDEX IF NOT EXISTS idx_challenge_participants_challenge ON challenge_participants(challenge_id);
CREATE INDEX IF NOT EXISTS idx_challenge_participants_account  ON challenge_participants(account_id);