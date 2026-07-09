-- Seed Data for Glotrush (Languages, Topics, Lessons, Exercises) using dynamic UUID generation
-- Ce script insère des données cohérentes pour le Français -> Anglais
-- Thématique : Voyage & Salutations

DO $$
DECLARE
    -- Langues
    lang_fr_id UUID;
    lang_en_id UUID;
    lang_es_id UUID;
    
    -- Topics
    topic_salutations_id UUID;
    topic_voyage_id UUID;
    
    -- Lessons
    lesson_mots_base_id UUID;
    lesson_quiz_salutations_id UUID;
    lesson_transport_id UUID;
    lesson_ordre_phrase_id UUID;
    
    -- Exercices
    qcm_q1_id UUID;
    qcm_q2_id UUID;
    sorting_entity_id UUID;
BEGIN
    -- 1. LANGUES
    INSERT INTO languages (id, code, name, is_active, created_at) 
    VALUES (gen_random_uuid(), 'fr', 'Français', true, NOW())
    RETURNING id INTO lang_fr_id;

    INSERT INTO languages (id, code, name, is_active, created_at) 
    VALUES (gen_random_uuid(), 'en', 'Anglais', true, NOW())
    RETURNING id INTO lang_en_id;

    INSERT INTO languages (id, code, name, is_active, created_at) 
    VALUES (gen_random_uuid(), 'es', 'Espagnol', true, NOW())
    RETURNING id INTO lang_es_id;

    -- 2. TOPICS (pour l'Anglais)
    INSERT INTO topic (id, target_language_id, source_language_id, name, description, difficulty, is_active, created_at, updated_at) 
    VALUES (gen_random_uuid(), lang_en_id, lang_fr_id, 'Salutations & Présentations', 'Apprenez les bases pour dire bonjour et vous présenter.', 'A1', true, NOW(), NOW())
    RETURNING id INTO topic_salutations_id;

    INSERT INTO topic (id, target_language_id, source_language_id, name, description, difficulty, is_active, created_at, updated_at) 
    VALUES (gen_random_uuid(), lang_en_id, lang_fr_id, 'Voyage & Transport', 'Vocabulaire essentiel pour vos déplacements à l''étranger.', 'B1', true, NOW(), NOW())
    RETURNING id INTO topic_voyage_id;

    -- 3. LESSONS (Type FLASHCARD)
    INSERT INTO lesson (id, topic_id, title, description, order_index, xp_reward, min_score_required, duration_minutes, is_active, is_included_in_exam, lesson_type, created_at, updated_at) 
    VALUES (gen_random_uuid(), topic_salutations_id, 'Les mots de base', 'Découvrez les salutations courantes.', 1, 50, 80, 5, true, true, 'FLASHCARD', NOW(), NOW())
    RETURNING id INTO lesson_mots_base_id;

    -- Exercices FLASHCARD
    INSERT INTO flashcard_entity (id, lesson_id, front, back, front_language, back_language) VALUES 
    (gen_random_uuid(), lesson_mots_base_id, 'Bonjour', 'Hello', 'fr', 'en'),
    (gen_random_uuid(), lesson_mots_base_id, 'Merci', 'Thank you', 'fr', 'en'),
    (gen_random_uuid(), lesson_mots_base_id, 'S''il vous plaît', 'Please', 'fr', 'en');

    -- 4. LESSONS (Type QCM)
    INSERT INTO lesson (id, topic_id, title, description, order_index, xp_reward, min_score_required, duration_minutes, is_active, is_included_in_exam, lesson_type, created_at, updated_at) 
    VALUES (gen_random_uuid(), topic_salutations_id, 'Quiz de salutations', 'Vérifiez vos connaissances sur les bases.', 2, 60, 75, 10, true, true, 'QCM', NOW(), NOW())
    RETURNING id INTO lesson_quiz_salutations_id;

    -- Exercices QCM
    INSERT INTO qcm_question_entity (id, lesson_id, question, correct_option_index, explanation) 
    VALUES (gen_random_uuid(), lesson_quiz_salutations_id, 'Comment dit-on "Au revoir" en anglais ?', 1, '"Goodbye" est la forme standard pour dire au revoir.')
    RETURNING id INTO qcm_q1_id;

    INSERT INTO qcm_question_entity (id, lesson_id, question, correct_option_index, explanation) 
    VALUES (gen_random_uuid(), lesson_quiz_salutations_id, 'Quelle est la réponse appropriée à "How are you?" ?', 0, '"I am fine" signifie "Je vais bien".')
    RETURNING id INTO qcm_q2_id;

    -- Options QCM
    INSERT INTO qcm_question_entity_options (qcm_question_entity_id, options) VALUES 
    (qcm_q1_id, 'Hello'),
    (qcm_q1_id, 'Goodbye'),
    (qcm_q1_id, 'Welcome'),
    (qcm_q2_id, 'I am fine, thank you'),
    (qcm_q2_id, 'Nice to meet you'),
    (qcm_q2_id, 'My name is John');

    -- 5. LESSONS (Type MATCHING_PAIR)
    INSERT INTO lesson (id, topic_id, title, description, order_index, xp_reward, min_score_required, duration_minutes, is_active, is_included_in_exam, lesson_type, created_at, updated_at) 
    VALUES (gen_random_uuid(), topic_voyage_id, 'Correspondance Transport', 'Associez les moyens de transport.', 1, 70, 70, 8, true, true, 'MATCHING_PAIR', NOW(), NOW())
    RETURNING id INTO lesson_transport_id;

    -- Exercices MATCHING_PAIR
    INSERT INTO matching_pair_entity (id, lesson_id, item1, item2) VALUES 
    (gen_random_uuid(), lesson_transport_id, 'Voiture', 'Car'),
    (gen_random_uuid(), lesson_transport_id, 'Avion', 'Plane'),
    (gen_random_uuid(), lesson_transport_id, 'Train', 'Train'),
    (gen_random_uuid(), lesson_transport_id, 'Bateau', 'Boat');

    -- 6. LESSONS (Type SORTING_EXERCISE)
    INSERT INTO lesson (id, topic_id, title, description, order_index, xp_reward, min_score_required, duration_minutes, is_active, is_included_in_exam, lesson_type, created_at, updated_at) 
    VALUES (gen_random_uuid(), topic_voyage_id, 'Ordre d''une phrase de voyage', 'Remettez les mots dans l''ordre pour former une phrase.', 2, 80, 80, 12, true, true, 'SORTING_EXERCISE', NOW(), NOW())
    RETURNING id INTO lesson_ordre_phrase_id;

    -- Exercices SORTING_EXERCISE
    INSERT INTO sorting_exercise_entity (id, lesson_id) 
    VALUES (gen_random_uuid(), lesson_ordre_phrase_id)
    RETURNING id INTO sorting_entity_id;

    -- Items pour la phrase "Where is the train station ?"
    INSERT INTO sorting_exercise_entity_items (sorting_exercise_entity_id, items) VALUES 
    (sorting_entity_id, 'the'),
    (sorting_entity_id, 'Where'),
    (sorting_entity_id, 'station'),
    (sorting_entity_id, 'is'),
    (sorting_entity_id, 'train');

    -- Ordre correct
    INSERT INTO sorting_exercise_entity_correct_order (sorting_exercise_entity_id, correct_order) VALUES 
    (sorting_entity_id, 1),
    (sorting_entity_id, 3),
    (sorting_entity_id, 0),
    (sorting_entity_id, 4),
    (sorting_entity_id, 2);

    RAISE NOTICE 'Seed data inserted successfully with dynamic UUIDs.';
END $$;
