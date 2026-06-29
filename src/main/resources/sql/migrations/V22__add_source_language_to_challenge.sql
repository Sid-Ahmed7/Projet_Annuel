DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'challenges' AND column_name = 'source_language_id') THEN
        ALTER TABLE challenges ADD COLUMN source_language_id UUID;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_name = 'challenges' AND constraint_name = 'fk_challenges_source_language') THEN
        ALTER TABLE challenges ADD CONSTRAINT fk_challenges_source_language FOREIGN KEY (source_language_id) REFERENCES languages(id);
    END IF;
END $$;
