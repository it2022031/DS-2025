-- =========================================
-- Load sample user avatars & property photos
-- Προϋπόθεση: Τα αρχεία υπάρχουν στο /photos στο CONTAINER
-- =========================================

-- Ρύθμιση βάσης διαδρομής
\set BASE '/photos'

-- =======================
-- USERS: avatars (ids 1..3)
-- =======================

-- User #1 (jpeg)
SELECT lo_import(:'BASE' || '/avatars_photoes/ego_otan_o_fotografos_mou_pirnei_to_fei_gia_na_koitakso_tin_kamera.jpg') AS oid \gset
UPDATE users
SET profile_picture = lo_get(:oid),
    profile_picture_content_type = 'image/jpeg',
    profile_picture_filename = 'ego_otan_o_fotografos_mou_pirnei_to_fei_gia_na_koitakso_tin_kamera.jpg'
WHERE id = 1;
SELECT lo_unlink(:oid);

-- User #2 (png)
SELECT lo_import(:'BASE' || '/avatars_photoes/image.png') AS oid \gset
UPDATE users
SET profile_picture = lo_get(:oid),
    profile_picture_content_type = 'image/png',
    profile_picture_filename = 'image.png'
WHERE id = 2;
SELECT lo_unlink(:oid);

-- User #3 (jpeg)
SELECT lo_import(:'BASE' || '/avatars_photoes/Screenshot_20240624_225002_Instagram.jpg') AS oid \gset
UPDATE users
SET profile_picture = lo_get(:oid),
    profile_picture_content_type = 'image/jpeg',
    profile_picture_filename = 'Screenshot_20240624_225002_Instagram.jpg'
WHERE id = 3;
SELECT lo_unlink(:oid);

-- ==========================
-- PROPERTIES: photos (ids 1..3)
-- ==========================
-- ΣΗΜ: Αν η στήλη στον πίνακα είναι "contenttype" αντί "content_type",
-- άλλαξε το όνομα παρακάτω στα INSERT.
\set BASE '/photos'

-- 1ο αρχείο -> property_id = 1
SELECT lo_import(:'BASE' || '/propertie_photoes/ego_otan_o_fotografos_mou_pirnei_to_fei_gia_na_koitakso_tin_kamera.jpg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename)
VALUES (1, lo_get(:oid), 'image/jpeg', 'ego_otan_o_fotografos_mou_pirnei_to_fei_gia_na_koitakso_tin_kamera.jpg');
SELECT lo_unlink(:oid);

-- 2ο αρχείο -> property_id = 2
SELECT lo_import(:'BASE' || '/propertie_photoes/image.png') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename)
VALUES (2, lo_get(:oid), 'image/png', 'image.png');
SELECT lo_unlink(:oid);

-- 3ο αρχείο -> property_id = 3
SELECT lo_import(:'BASE' || '/propertie_photoes/Screenshot_20240624_225002_Instagram.jpg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename)
VALUES (3, lo_get(:oid), 'image/jpeg', 'Screenshot_20240624_225002_Instagram.jpg');
SELECT lo_unlink(:oid);
;
