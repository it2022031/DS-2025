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
    (8, 3, '2024-02-10', '2024-02-15', 500.00, 'APPROVED');

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

--1
SELECT schemaname, tablename
FROM pg_catalog.pg_tables
ORDER BY schemaname, tablename;
-- 2
SELECT table_schema, table_name
FROM information_schema.tables
WHERE table_type = 'BASE TABLE'
ORDER BY table_schema, table_name;

--3
SELECT table_schema, table_name
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_type = 'BASE TABLE'
ORDER BY table_name;

-- 4
SELECT table_name, column_name, data_type
FROM information_schema.columns
WHERE table_schema = 'public'
ORDER BY table_name, ordinal_position;

-- 5
SELECT tc.table_name,
       tc.constraint_name,
       tc.constraint_type,
       kcu.column_name,
       ccu.table_name AS foreign_table,
       ccu.column_name AS foreign_column
FROM information_schema.table_constraints AS tc
         LEFT JOIN information_schema.key_column_usage AS kcu
                   ON tc.constraint_name = kcu.constraint_name
                       AND tc.table_schema = kcu.table_schema
         LEFT JOIN information_schema.constraint_column_usage AS ccu
                   ON ccu.constraint_name = tc.constraint_name
                       AND ccu.table_schema = tc.table_schema
WHERE tc.table_schema = 'public'
ORDER BY tc.table_name, tc.constraint_type;

-- 6
SELECT c.table_name, c.column_name, c.data_type,
       tc.constraint_type, tc.constraint_name
FROM information_schema.columns c
         LEFT JOIN information_schema.key_column_usage kcu
                   ON c.table_name = kcu.table_name
                       AND c.column_name = kcu.column_name
                       AND c.table_schema = kcu.table_schema
         LEFT JOIN information_schema.table_constraints tc
                   ON tc.constraint_name = kcu.constraint_name
                       AND tc.table_schema = c.table_schema
WHERE c.table_schema = 'public'
ORDER BY c.table_name, c.ordinal_position;

--
SELECT *
FROM bookings;