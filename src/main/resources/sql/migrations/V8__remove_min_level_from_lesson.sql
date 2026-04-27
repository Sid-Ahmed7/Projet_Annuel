-- Suppression de la colonne min_level_required avec condition d'existence (idempotence)
ALTER TABLE lesson DROP COLUMN IF EXISTS min_level_required;