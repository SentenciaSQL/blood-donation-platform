-- Align users.enabled (V1) with JPA field User.active.
-- Safe if a previous environment already renamed the column manually.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'users'
          AND column_name = 'enabled'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'users'
          AND column_name = 'active'
    ) THEN
        ALTER TABLE users RENAME COLUMN enabled TO active;
    END IF;
END $$;
