CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       profile_picture BYTEA,
                       profile_picture_filename VARCHAR(255),
                       profile_picture_content_type VARCHAR(255),
                       renter_request_status VARCHAR(50),
                       enabled BOOLEAN NOT NULL DEFAULT TRUE,
                       account_non_locked BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE properties (
                            id BIGSERIAL PRIMARY KEY,
                            name VARCHAR(255) NOT NULL,
                            description TEXT,
                            city VARCHAR(255),
                            street VARCHAR(255),
                            postal_code VARCHAR(20),
                            country VARCHAR(255),
                            square_meters INT,
                            approval_status VARCHAR(50),
                            user_id BIGINT REFERENCES users(id)
);

CREATE TABLE property_photos (
                                 id BIGSERIAL PRIMARY KEY,
                                 property_id BIGINT REFERENCES properties(id),
                                 image BYTEA
);
