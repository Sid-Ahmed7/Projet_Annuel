ALTER TABLE IF EXISTS user_lesson_progress ALTER COLUMN failed_attempts SET DEFAULT 0;
DO $$
BEGIN
    IF EXISTS (
        SELECT FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'user_lesson_progress'
    ) THEN
        UPDATE user_lesson_progress SET failed_attempts = 0 WHERE failed_attempts IS NULL;
    END IF;
END $$;
