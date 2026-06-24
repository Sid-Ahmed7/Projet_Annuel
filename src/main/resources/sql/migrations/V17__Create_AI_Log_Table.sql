CREATE TABLE IF NOT EXISTS ai_generation_logs (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    user_prompt TEXT NOT NULL,
    generated_response TEXT,
    lesson_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
