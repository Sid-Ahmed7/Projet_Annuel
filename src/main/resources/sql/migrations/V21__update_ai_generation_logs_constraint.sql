ALTER TABLE ai_generation_logs DROP CONSTRAINT IF EXISTS ai_generation_logs_lesson_type_check;
ALTER TABLE ai_generation_logs ADD CONSTRAINT ai_generation_logs_lesson_type_check CHECK (lesson_type IN ('FLASHCARD', 'MATCHING_PAIR', 'SORTING_EXERCISE', 'QCM', 'INTERACTIVE'));
