-- Mise à jour idempotente des langues pour Topic
DO $$
BEGIN
    -- Changement de nom de la colonne language_id vers target_language_id si elle existe
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'topic' AND column_name = 'language_id') THEN
        ALTER TABLE topic RENAME COLUMN language_id TO target_language_id;
    END IF;

    -- Ajout de la colonne source_language_id si elle n'existe pas encore
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'topic' AND column_name = 'source_language_id') THEN
        ALTER TABLE topic ADD COLUMN source_language_id UUID;
    END IF;
END $$;

-- Mise à jour des topics existants si la langue source est vide
UPDATE topic 
SET source_language_id = COALESCE(
    (SELECT id FROM languages WHERE code = 'fr' LIMIT 1),
    (SELECT id FROM languages LIMIT 1)
)
WHERE source_language_id IS NULL;

-- Application de la contrainte NOT NULL
ALTER TABLE topic ALTER COLUMN source_language_id SET NOT NULL;

-- Ajout de la clé étrangère de manière idempotente
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                   WHERE table_name = 'topic' AND constraint_name = 'fk_topic_source_language') THEN
        ALTER TABLE topic ADD CONSTRAINT fk_topic_source_language FOREIGN KEY (source_language_id) REFERENCES languages(id);
    END IF;
END $$;
