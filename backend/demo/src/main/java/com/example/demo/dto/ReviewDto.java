package com.example.demo.dto;

import com.example.demo.Entities.Review;

public record ReviewDto(
        Long id,
        String content,
        int rating,
        String createdAt,   // ISO-8601
        Long propertyId,
        Long renterId,
        Long rentalId
) {
    public static ReviewDto fromEntity(Review r) {
        Long propId = null;
        if (r.getProperty() != null) {
            propId = r.getProperty().getId();
        } else if (r.getRental() != null && r.getRental().getProperty() != null) {
            propId = r.getRental().getProperty().getId();
        }

        Long renterId = null;
        if (r.getUser() != null) {
            renterId = r.getUser().getId();
        } else if (r.getRental() != null && r.getRental().getUser() != null) {
            renterId = r.getRental().getUser().getId();
        }

        Long rentalId = (r.getRental() != null) ? r.getRental().getId() : null;

        return new ReviewDto(
                r.getId(),
                r.getContent(),
                r.getRating(),
                r.getCreatedAt() != null ? r.getCreatedAt().toString() : null,
                propId,
                renterId,
                rentalId
        );
    }
}
