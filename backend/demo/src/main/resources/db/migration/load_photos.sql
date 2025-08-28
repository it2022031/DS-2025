\set BASE '/photos'

-- ======================
-- AVATAR PHOTOS (USERS)
-- ======================
-- map: 1..10 στους αντίστοιχους φακέλους/αρχεία

\set BASE '/photos'

-- 1 Alice (F)
SELECT lo_import(:'BASE' || '/avatar_photos/f1.jpg') AS oid \gset
UPDATE users SET profile_picture = lo_get(:oid), profile_picture_content_type='image/jpeg', profile_picture_filename='f1.jpg' WHERE id=1;
SELECT lo_unlink(:oid);

-- 2 Bob (M)
SELECT lo_import(:'BASE' || '/avatar_photos/m1.jpeg') AS oid \gset
UPDATE users SET profile_picture = lo_get(:oid), profile_picture_content_type='image/jpeg', profile_picture_filename='m1.jpeg' WHERE id=2;
SELECT lo_unlink(:oid);

-- 3 Carol (F)
SELECT lo_import(:'BASE' || '/avatar_photos/f2.webp') AS oid \gset
UPDATE users SET profile_picture = lo_get(:oid), profile_picture_content_type='image/webp', profile_picture_filename='f2.webp' WHERE id=3;
SELECT lo_unlink(:oid);

-- 4 Dave (M)
SELECT lo_import(:'BASE' || '/avatar_photos/m2.jpeg') AS oid \gset
UPDATE users SET profile_picture = lo_get(:oid), profile_picture_content_type='image/jpeg', profile_picture_filename='m2.jpeg' WHERE id=4;
SELECT lo_unlink(:oid);

-- 5 Eve (F)
SELECT lo_import(:'BASE' || '/avatar_photos/f3.jpeg') AS oid \gset
UPDATE users SET profile_picture = lo_get(:oid), profile_picture_content_type='image/jpeg', profile_picture_filename='f3.jpeg' WHERE id=5;
SELECT lo_unlink(:oid);

-- 6 Frank (M)
SELECT lo_import(:'BASE' || '/avatar_photos/m3.jpeg') AS oid \gset
UPDATE users SET profile_picture = lo_get(:oid), profile_picture_content_type='image/jpeg', profile_picture_filename='m3.jpeg' WHERE id=6;
SELECT lo_unlink(:oid);

-- 7 Grace (F)
SELECT lo_import(:'BASE' || '/avatar_photos/f4.jpg') AS oid \gset
UPDATE users SET profile_picture = lo_get(:oid), profile_picture_content_type='image/jpeg', profile_picture_filename='f4.jpg' WHERE id=7;
SELECT lo_unlink(:oid);

-- 8 Hank (M)
SELECT lo_import(:'BASE' || '/avatar_photos/m4.jpeg') AS oid \gset
UPDATE users SET profile_picture = lo_get(:oid), profile_picture_content_type='image/jpeg', profile_picture_filename='m4.jpeg' WHERE id=8;
SELECT lo_unlink(:oid);

-- 9 Ivy (F)
SELECT lo_import(:'BASE' || '/avatar_photos/f5.jpg') AS oid \gset
UPDATE users SET profile_picture = lo_get(:oid), profile_picture_content_type='image/jpeg', profile_picture_filename='f5.jpg' WHERE id=9;
SELECT lo_unlink(:oid);

-- 10 John (M)
SELECT lo_import(:'BASE' || '/avatar_photos/m5.jpeg') AS oid \gset
UPDATE users SET profile_picture = lo_get(:oid), profile_picture_content_type='image/jpeg', profile_picture_filename='m5.jpeg' WHERE id=10;
SELECT lo_unlink(:oid);

-- =======================
-- PROPERTY PHOTOS (3/prop)
-- =======================
-- Προσαρμόζεις κατά βούληση· εδώ δίνω 3 φωτό για properties 1..10

-- p1: "City Loft"
SELECT lo_import(:'BASE' || '/property_photos/City Loft 1.jpg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (1, lo_get(:oid), 'image/jpeg', 'City Loft 1.jpg');
SELECT lo_unlink(:oid);
SELECT lo_import(:'BASE' || '/property_photos/City Loft 2.jpg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (1, lo_get(:oid), 'image/jpeg', 'City Loft 2.jpg');
SELECT lo_unlink(:oid);
SELECT lo_import(:'BASE' || '/property_photos/City Loft 3.jpg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (1, lo_get(:oid), 'image/jpeg', 'City Loft 3.jpg');
SELECT lo_unlink(:oid);

-- p2: "City View"
SELECT lo_import(:'BASE' || '/property_photos/City View 1.jpeg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (2, lo_get(:oid), 'image/jpeg', 'City View 1.jpeg');
SELECT lo_unlink(:oid);
SELECT lo_import(:'BASE' || '/property_photos/City View 2.jpg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (2, lo_get(:oid), 'image/jpeg', 'City View 2.jpg');
SELECT lo_unlink(:oid);
SELECT lo_import(:'BASE' || '/property_photos/City View 3.jpg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (2, lo_get(:oid), 'image/jpeg', 'City View 3.jpg');
SELECT lo_unlink(:oid);

-- p3: "Garden Home"
SELECT lo_import(:'BASE' || '/property_photos/Garden Home 1.jpg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (3, lo_get(:oid), 'image/jpeg', 'Garden Home 1.jpg');
SELECT lo_unlink(:oid);
SELECT lo_import(:'BASE' || '/property_photos/Garden Home 2.jpg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (3, lo_get(:oid), 'image/jpeg', 'Garden Home 2.jpg');
SELECT lo_unlink(:oid);
SELECT lo_import(:'BASE' || '/property_photos/Suburb House 1.jpg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (3, lo_get(:oid), 'image/jpeg', 'Suburb House 1.jpg');
SELECT lo_unlink(:oid);

-- p4: "Island Studio"
SELECT lo_import(:'BASE' || '/property_photos/Island Studio 1.jpg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (4, lo_get(:oid), 'image/jpeg', 'Island Studio 1.jpg');
SELECT lo_unlink(:oid);
SELECT lo_import(:'BASE' || '/property_photos/Island Studio 2.jpg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (4, lo_get(:oid), 'image/jpeg', 'Island Studio 2.jpg');
SELECT lo_unlink(:oid);
SELECT lo_import(:'BASE' || '/property_photos/Island Studio 3.jpg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (4, lo_get(:oid), 'image/jpeg', 'Island Studio 3.jpg');
SELECT lo_unlink(:oid);

-- p5: "Lake Villa"
SELECT lo_import(:'BASE' || '/property_photos/Lake Villa 1.jpg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (5, lo_get(:oid), 'image/jpeg', 'Lake Villa 1.jpg');
SELECT lo_unlink(:oid);
SELECT lo_import(:'BASE' || '/property_photos/Lake Villa 2.jpg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (5, lo_get(:oid), 'image/jpeg', 'Lake Villa 2.jpg');
SELECT lo_unlink(:oid);
SELECT lo_import(:'BASE' || '/property_photos/Riverside 1.jpg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (5, lo_get(:oid), 'image/jpeg', 'Riverside 1.jpg');
SELECT lo_unlink(:oid);

-- p6: "Mountain Cabin"
SELECT lo_import(:'BASE' || '/property_photos/Mountain Cabin 1.jpg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (6, lo_get(:oid), 'image/jpeg', 'Mountain Cabin 1.jpg');
SELECT lo_unlink(:oid);
SELECT lo_import(:'BASE' || '/property_photos/Mountain Cabin 2.jpg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (6, lo_get(:oid), 'image/jpeg', 'Mountain Cabin 2.jpg');
SELECT lo_unlink(:oid);
SELECT lo_import(:'BASE' || '/property_photos/Mountain Cabin 3.jpg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (6, lo_get(:oid), 'image/jpeg', 'Mountain Cabin 3.jpg');
SELECT lo_unlink(:oid);

-- p7: "Old Town Flat"
SELECT lo_import(:'BASE' || '/property_photos/Old Twon Flat 1.JPEG') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (7, lo_get(:oid), 'image/jpeg', 'Old Twon Flat 1.JPEG');
SELECT lo_unlink(:oid);
SELECT lo_import(:'BASE' || '/property_photos/Old Twon Flat 2.JPEG') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (7, lo_get(:oid), 'image/jpeg', 'Old Twon Flat 2.JPEG');
SELECT lo_unlink(:oid);
SELECT lo_import(:'BASE' || '/property_photos/Old Twon Flat 3.JPEG') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (7, lo_get(:oid), 'image/jpeg', 'Old Twon Flat 3.JPEG');
SELECT lo_unlink(:oid);

-- p8: "Sea House"
SELECT lo_import(:'BASE' || '/property_photos/Sea House 1.jpg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (8, lo_get(:oid), 'image/jpeg', 'Sea House 1.jpg');
SELECT lo_unlink(:oid);
SELECT lo_import(:'BASE' || '/property_photos/Sea House 2.jpeg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (8, lo_get(:oid), 'image/jpeg', 'Sea House 2.jpeg');
SELECT lo_unlink(:oid);
SELECT lo_import(:'BASE' || '/property_photos/Sea House 3.jpg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (8, lo_get(:oid), 'image/jpeg', 'Sea House 3.jpg');
SELECT lo_unlink(:oid);

-- p9: "Stone House"
SELECT lo_import(:'BASE' || '/property_photos/Stone House 1.jpeg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (9, lo_get(:oid), 'image/jpeg', 'Stone House 1.jpeg');
SELECT lo_unlink(:oid);
SELECT lo_import(:'BASE' || '/property_photos/Stone House 2.jpg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (9, lo_get(:oid), 'image/jpeg', 'Stone House 2.jpg');
SELECT lo_unlink(:oid);
SELECT lo_import(:'BASE' || '/property_photos/Stone House 3.jpg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (9, lo_get(:oid), 'image/jpeg', 'Stone House 3.jpg');
SELECT lo_unlink(:oid);

-- p10: "Sunset Suite"
SELECT lo_import(:'BASE' || '/property_photos/Sunset Suite 1.jpg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (10, lo_get(:oid), 'image/jpeg', 'Sunset Suite 1.jpg');
SELECT lo_unlink(:oid);
SELECT lo_import(:'BASE' || '/property_photos/Sunset Suite 2.jpg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (10, lo_get(:oid), 'image/jpeg', 'Sunset Suite 2.jpg');
SELECT lo_unlink(:oid);
SELECT lo_import(:'BASE' || '/property_photos/Sunset Suite 3.jpg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename) VALUES (10, lo_get(:oid), 'image/jpeg', 'Sunset Suite 3.jpg');
SELECT lo_unlink(:oid);
