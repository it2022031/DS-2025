package com.example.demo.migrations;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseImageColumnFix {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseImageColumnFix(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void convertImageColumnIfNeeded() {
        String checkTypeSql = """
            SELECT data_type 
            FROM information_schema.columns
            WHERE table_name = 'property_photos' AND column_name = 'image'
        """;

        String type = jdbcTemplate.queryForObject(checkTypeSql, String.class);

        if ("oid".equalsIgnoreCase(type)) {
            System.out.println("🔄 Converting property_photos.image from OID to bytea...");
            jdbcTemplate.execute("""
                ALTER TABLE property_photos
                ALTER COLUMN image TYPE bytea
                USING lo_get(image)
            """);
            System.out.println("✅ Conversion completed!");
        } else {
            System.out.println("ℹ️ property_photos.image is already bytea, no change needed.");
        }
    }
    @PostConstruct
    public void convertUserProfilePictureIfNeeded() {
        String checkTypeSql = """
        SELECT data_type 
        FROM information_schema.columns
        WHERE table_name = 'users' AND column_name = 'profile_picture'
    """;

        String type = jdbcTemplate.queryForObject(checkTypeSql, String.class);

        if ("oid".equalsIgnoreCase(type)) {
            System.out.println("🔄 Converting users.profile_picture from OID to bytea...");
            jdbcTemplate.execute("""
            ALTER TABLE users
            ALTER COLUMN profile_picture TYPE bytea
            USING lo_get(profile_picture)
        """);
            System.out.println("✅ Conversion completed!");
        } else {
            System.out.println("ℹ️ users.profile_picture is already bytea, no change needed.");
        }
    }


}
