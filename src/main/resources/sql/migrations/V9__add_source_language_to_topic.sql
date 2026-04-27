DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'topic' AND column_name = 'language_id') THEN
        ALTER TABLE topic RENAME COLUMN language_id TO target_language_id;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'topic' AND column_name = 'source_language_id') THEN
        ALTER TABLE topic ADD COLUMN source_language_id UUID;
    END IF;
END $$;

UPDATE topic 
SET source_language_id = COALESCE(
    (SELECT id FROM languages WHERE code = 'fr' LIMIT 1),
    (SELECT id FROM languages LIMIT 1)
)
WHERE source_language_id IS NULL;

ALTER TABLE topic ALTER COLUMN source_language_id SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                   WHERE table_name = 'topic' AND constraint_name = 'fk_topic_source_language') THEN
        ALTER TABLE topic ADD CONSTRAINT fk_topic_source_language FOREIGN KEY (source_language_id) REFERENCES languages(id);
    END IF;
END $$;
