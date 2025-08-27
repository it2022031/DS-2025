-- Βάση διαδρομής για τις εικόνες ΜΕΣΑ στο CONTAINER
\set BASE '/photos'

-------------------------------
-- AVATARS για USERS 1,2,3
-------------------------------

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

-------------------------------
-- PHOTOS για PROPERTIES 1,2,3
-------------------------------
-- Σημείωση: Αν ο πίνακας έχει στήλη 'contenttype' αντί για 'content_type',
-- άλλαξε το όνομα στη παρακάτω INSERT.

-- Property #1 (jpeg)
SELECT lo_import(:'BASE' || '/propertie_photoes/ego_otan_o_fotografos_mou_pirnei_to_fei_gia_na_koitakso_tin_kamera.jpg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename)
VALUES (1, lo_get(:oid), 'image/jpeg', 'ego_otan_o_fotografos_mou_pirnei_to_fei_gia_na_koitakso_tin_kamera.jpg');
SELECT lo_unlink(:oid);

-- Property #2 (png)
SELECT lo_import(:'BASE' || '/propertie_photoes/image.png') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename)
VALUES (2, lo_get(:oid), 'image/png', 'image.png');
SELECT lo_unlink(:oid);

-- Property #3 (jpeg)
SELECT lo_import(:'BASE' || '/propertie_photoes/Screenshot_20240624_225002_Instagram.jpg') AS oid \gset
INSERT INTO property_photos (property_id, image, content_type, filename)
VALUES (3, lo_get(:oid), 'image/jpeg', 'Screenshot_20240624_225002_Instagram.jpg');
SELECT lo_unlink(:oid);
