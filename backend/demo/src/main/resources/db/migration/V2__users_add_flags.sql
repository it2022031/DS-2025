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

-- afto trexe giani xaxax
ALTER TABLE properties ADD COLUMN price numeric(12,2);

ALTER TABLE rentals
    ALTER COLUMN payment_amount TYPE numeric(12,2)
        USING round((payment_amount)::numeric, 2);

ALTER TABLE rentals ADD COLUMN payment_amount numeric(12,2) NOT NULL DEFAULT 0;
ALTER TABLE rentals ALTER COLUMN payment_amount DROP DEFAULT;

-- afto trexe giani xxaxaxa 2
ALTER TABLE reviews ADD COLUMN rental_id BIGINT;
ALTER TABLE reviews
    ADD CONSTRAINT fk_reviews_rental
        FOREIGN KEY (rental_id) REFERENCES rentals(id) ON DELETE SET NULL;

CREATE INDEX idx_reviews_rental_id ON reviews(rental_id);
