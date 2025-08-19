DO $$
    BEGIN
        -- property_photos.image
        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = 'property_photos' AND column_name = 'image' AND data_type = 'oid'
        ) THEN
            ALTER TABLE property_photos
                ALTER COLUMN image TYPE bytea
                    USING lo_get(image);
        END IF;

        -- users.profile_picture
        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = 'users' AND column_name = 'profile_picture' AND data_type = 'oid'
        ) THEN
            ALTER TABLE users
                ALTER COLUMN profile_picture TYPE bytea
                    USING lo_get(profile_picture);
        END IF;
    END $$;
