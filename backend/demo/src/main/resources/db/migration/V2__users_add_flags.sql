-- 1) Προσθήκη ως NULLABLE με default για να γεμίσουν οι υπάρχουσες γραμμές
ALTER TABLE users ADD COLUMN account_non_locked boolean DEFAULT true;
UPDATE users SET account_non_locked = true WHERE account_non_locked IS NULL;
ALTER TABLE users ALTER COLUMN account_non_locked SET NOT NULL;
ALTER TABLE users ALTER COLUMN account_non_locked DROP DEFAULT;

-- 2) Email: πρώτα nullable, μετά backfill, μετά not null
ALTER TABLE users ADD COLUMN email varchar(255);
-- Αν μπορείς να “βγάλεις” αξιόπιστο email από αλλού, κάνε UPDATE εδώ.
-- Διαφορετικά, για να μη σκάει προσωρινά στο dev:
UPDATE users SET email = CONCAT('user', id, '@example.com') WHERE email IS NULL;

ALTER TABLE users ALTER COLUMN email SET NOT NULL;

-- 3) enabled flag
ALTER TABLE users ADD COLUMN enabled boolean DEFAULT true;
UPDATE users SET enabled = true WHERE enabled IS NULL;
ALTER TABLE users ALTER COLUMN enabled SET NOT NULL;
ALTER TABLE users ALTER COLUMN enabled DROP DEFAULT;
