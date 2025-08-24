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

-- test gia review pseftiko parenlthodikou rental

SELECT id, name FROM properties;
SELECT id, username FROM users;

INSERT INTO rentals
(property_id, user_id, start_date, end_date, payment_amount, approval_status)
VALUES
    (2, 3, '2024-02-10', '2024-02-15', 500.00, 'PENDING');

-- allagi idi iparxon rental
UPDATE rentals
SET start_date = '2024-01-10',
    end_date   = '2024-01-15',
    approval_status = 'APPROVED'
WHERE id = 1;

-- allagi rolon enos iparxoun user

-- Όλοι οι ρόλοι ανά χρήστη
SELECT u.id, u.username, array_remove(array_agg(r.role), NULL) AS roles
FROM users u
         LEFT JOIN user_roles r ON r.user_id = u.id
GROUP BY u.id, u.username
ORDER BY u.id;

-- Οι ρόλοι ενός συγκεκριμένου χρήστη (π.χ. user_id = 5)
SELECT role FROM user_roles WHERE user_id = 5;

-- (Μία φορά) φτιάξε unique index αν δεν υπάρχει:
CREATE UNIQUE INDEX IF NOT EXISTS ux_user_roles_user_role
    ON user_roles(user_id, role);

-- Πρόσθεσε ρόλο (π.χ. ADMIN) στον user 5
INSERT INTO user_roles(user_id, role)
VALUES (5, 'ADMIN')
ON CONFLICT (user_id, role) DO NOTHING;  -- δεν θα διπλο-εισάγει

-- Αφαίρεσε τον ρόλο RENTER από τον user 5
DELETE FROM user_roles
WHERE user_id = 5 AND role = 'RENTER';

BEGIN;

-- 1) Καθάρισε τους τωρινούς ρόλους
DELETE FROM user_roles WHERE user_id = 5;

-- 2) Βάλε τους νέους (π.χ. μόνο ADMIN και RENTER)
INSERT INTO user_roles(user_id, role) VALUES
                                          (2, 'ADMIN');

COMMIT;

-- kanton admin
INSERT INTO user_roles(user_id, role) VALUES (2, 'ADMIN')
ON CONFLICT (user_id, role) DO NOTHING;
-- Κάν’ τον σκέτο USER
BEGIN;
DELETE FROM user_roles WHERE user_id = 5;
INSERT INTO user_roles(user_id, role) VALUES (5, 'USER');
COMMIT;

-- Δες τι έχει τώρα
SELECT role FROM user_roles WHERE user_id = 5;

SELECT ur.user_id, ur.role
FROM user_roles ur
WHERE ur.user_id = 2;
