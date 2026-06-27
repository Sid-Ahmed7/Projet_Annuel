DO $$
DECLARE v_c TEXT;
BEGIN

    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'lesson_session') THEN
        SELECT kcu.constraint_name INTO v_c
        FROM information_schema.key_column_usage kcu
        JOIN information_schema.table_constraints tc ON tc.constraint_name = kcu.constraint_name
        WHERE kcu.table_name = 'lesson_session' AND kcu.column_name = 'account_id' AND tc.constraint_type = 'FOREIGN KEY';
        IF v_c IS NOT NULL THEN EXECUTE 'ALTER TABLE lesson_session DROP CONSTRAINT "' || v_c || '"'; END IF;
        ALTER TABLE lesson_session ADD CONSTRAINT fk_lesson_session_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE;
    END IF;

    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'user_mistakes') THEN
        SELECT kcu.constraint_name INTO v_c
        FROM information_schema.key_column_usage kcu
        JOIN information_schema.table_constraints tc ON tc.constraint_name = kcu.constraint_name
        WHERE kcu.table_name = 'user_mistakes' AND kcu.column_name = 'account_id' AND tc.constraint_type = 'FOREIGN KEY';
        IF v_c IS NOT NULL THEN EXECUTE 'ALTER TABLE user_mistakes DROP CONSTRAINT "' || v_c || '"'; END IF;
        ALTER TABLE user_mistakes ADD CONSTRAINT fk_user_mistakes_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE;
    END IF;

    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'user_lesson_progress') THEN
        SELECT kcu.constraint_name INTO v_c
        FROM information_schema.key_column_usage kcu
        JOIN information_schema.table_constraints tc ON tc.constraint_name = kcu.constraint_name
        WHERE kcu.table_name = 'user_lesson_progress' AND kcu.column_name = 'account_id' AND tc.constraint_type = 'FOREIGN KEY';
        IF v_c IS NOT NULL THEN EXECUTE 'ALTER TABLE user_lesson_progress DROP CONSTRAINT "' || v_c || '"'; END IF;
        ALTER TABLE user_lesson_progress ADD CONSTRAINT fk_user_lesson_progress_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE;
    END IF;

    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'push_notification_subscription') THEN
        SELECT kcu.constraint_name INTO v_c
        FROM information_schema.key_column_usage kcu
        JOIN information_schema.table_constraints tc ON tc.constraint_name = kcu.constraint_name
        WHERE kcu.table_name = 'push_notification_subscription' AND kcu.column_name = 'account_id' AND tc.constraint_type = 'FOREIGN KEY';
        IF v_c IS NOT NULL THEN EXECUTE 'ALTER TABLE push_notification_subscription DROP CONSTRAINT "' || v_c || '"'; END IF;
        ALTER TABLE push_notification_subscription ADD CONSTRAINT fk_push_notif_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE;
    END IF;

    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'password_reset_tokens') THEN
        SELECT kcu.constraint_name INTO v_c
        FROM information_schema.key_column_usage kcu
        JOIN information_schema.table_constraints tc ON tc.constraint_name = kcu.constraint_name
        WHERE kcu.table_name = 'password_reset_tokens' AND kcu.column_name = 'account_id' AND tc.constraint_type = 'FOREIGN KEY';
        IF v_c IS NOT NULL THEN EXECUTE 'ALTER TABLE password_reset_tokens DROP CONSTRAINT "' || v_c || '"'; END IF;
        ALTER TABLE password_reset_tokens ADD CONSTRAINT fk_password_reset_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE;
    END IF;

    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'payment_history') THEN
        SELECT kcu.constraint_name INTO v_c
        FROM information_schema.key_column_usage kcu
        JOIN information_schema.table_constraints tc ON tc.constraint_name = kcu.constraint_name
        WHERE kcu.table_name = 'payment_history' AND kcu.column_name = 'account_id' AND tc.constraint_type = 'FOREIGN KEY';
        IF v_c IS NOT NULL THEN EXECUTE 'ALTER TABLE payment_history DROP CONSTRAINT "' || v_c || '"'; END IF;
        ALTER TABLE payment_history ADD CONSTRAINT fk_payment_history_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE;
    END IF;

    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'subscription') THEN
        SELECT kcu.constraint_name INTO v_c
        FROM information_schema.key_column_usage kcu
        JOIN information_schema.table_constraints tc ON tc.constraint_name = kcu.constraint_name
        WHERE kcu.table_name = 'subscription' AND kcu.column_name = 'account_id' AND tc.constraint_type = 'FOREIGN KEY';
        IF v_c IS NOT NULL THEN EXECUTE 'ALTER TABLE subscription DROP CONSTRAINT "' || v_c || '"'; END IF;
        ALTER TABLE subscription ADD CONSTRAINT fk_subscription_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE;
    END IF;

    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'friends') THEN
        SELECT kcu.constraint_name INTO v_c
        FROM information_schema.key_column_usage kcu
        JOIN information_schema.table_constraints tc ON tc.constraint_name = kcu.constraint_name
        WHERE kcu.table_name = 'friends' AND kcu.column_name = 'sender_id' AND tc.constraint_type = 'FOREIGN KEY';
        IF v_c IS NOT NULL THEN EXECUTE 'ALTER TABLE friends DROP CONSTRAINT "' || v_c || '"'; END IF;
        ALTER TABLE friends ADD CONSTRAINT fk_friends_sender FOREIGN KEY (sender_id) REFERENCES accounts(id) ON DELETE CASCADE;

        SELECT kcu.constraint_name INTO v_c
        FROM information_schema.key_column_usage kcu
        JOIN information_schema.table_constraints tc ON tc.constraint_name = kcu.constraint_name
        WHERE kcu.table_name = 'friends' AND kcu.column_name = 'receiver_id' AND tc.constraint_type = 'FOREIGN KEY';
        IF v_c IS NOT NULL THEN EXECUTE 'ALTER TABLE friends DROP CONSTRAINT "' || v_c || '"'; END IF;
        ALTER TABLE friends ADD CONSTRAINT fk_friends_receiver FOREIGN KEY (receiver_id) REFERENCES accounts(id) ON DELETE CASCADE;
    END IF;

    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'challenges') THEN
        SELECT kcu.constraint_name INTO v_c
        FROM information_schema.key_column_usage kcu
        JOIN information_schema.table_constraints tc ON tc.constraint_name = kcu.constraint_name
        WHERE kcu.table_name = 'challenges' AND kcu.column_name = 'challenger_id' AND tc.constraint_type = 'FOREIGN KEY';
        IF v_c IS NOT NULL THEN EXECUTE 'ALTER TABLE challenges DROP CONSTRAINT "' || v_c || '"'; END IF;
        ALTER TABLE challenges ADD CONSTRAINT fk_challenges_challenger FOREIGN KEY (challenger_id) REFERENCES accounts(id) ON DELETE CASCADE;

        SELECT kcu.constraint_name INTO v_c
        FROM information_schema.key_column_usage kcu
        JOIN information_schema.table_constraints tc ON tc.constraint_name = kcu.constraint_name
        WHERE kcu.table_name = 'challenges' AND kcu.column_name = 'challenged_id' AND tc.constraint_type = 'FOREIGN KEY';
        IF v_c IS NOT NULL THEN EXECUTE 'ALTER TABLE challenges DROP CONSTRAINT "' || v_c || '"'; END IF;
        ALTER TABLE challenges ADD CONSTRAINT fk_challenges_challenged FOREIGN KEY (challenged_id) REFERENCES accounts(id) ON DELETE CASCADE;
    END IF;

    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'challenge_participants') THEN
        SELECT kcu.constraint_name INTO v_c
        FROM information_schema.key_column_usage kcu
        JOIN information_schema.table_constraints tc ON tc.constraint_name = kcu.constraint_name
        WHERE kcu.table_name = 'challenge_participants' AND kcu.column_name = 'account_id' AND tc.constraint_type = 'FOREIGN KEY';
        IF v_c IS NOT NULL THEN EXECUTE 'ALTER TABLE challenge_participants DROP CONSTRAINT "' || v_c || '"'; END IF;
        ALTER TABLE challenge_participants ADD CONSTRAINT fk_challenge_participants_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE;
    END IF;

END $$;