package com.example.demo.dto;

import com.example.demo.Entities.Review;

public record ReviewDto(
        Long id,
        String content,
        int rating,
        String createdAt,   // ως ISO-8601 string
        Long propertyId,
        Long renterId ,
        Long rentalId
) {
    public static ReviewDto fromEntity(Review r) {
        return new ReviewDto(
                r.getId(),
                r.getContent(),
                r.getRating(),
                r.getCreatedAt() != null ? r.getCreatedAt().toString() : null,
                r.getRental()!=null ? r.getRental().getId() : null,
                (r.getProperty() != null ? r.getProperty().getId() : null),
                (r.getUser() != null ? r.getUser().getId() : null)
        );
    }
}
