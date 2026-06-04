DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'user_lesson_progress' AND column_name = 'failed_attempts'
    ) THEN
        ALTER TABLE user_lesson_progress ALTER COLUMN failed_attempts SET DEFAULT 0;
        UPDATE user_lesson_progress SET failed_attempts = 0 WHERE failed_attempts IS NULL;
    END IF;
END $$;
