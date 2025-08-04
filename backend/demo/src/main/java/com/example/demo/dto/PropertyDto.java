package com.example.demo.dto;

import com.example.demo.Entities.Property;

public record PropertyDto(
        Long id,
        String name,
        String description,
        String country,
        String city,
        String street,
        String postalCode,
        Double squareMeters,
        String approvalStatus,
        Long ownerId
) {
    public static PropertyDto fromEntity(Property p) {
        return new PropertyDto(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getCountry(),
                p.getCity(),
                p.getStreet(),
                p.getPostalCode(),
                p.getSquareMeters(),
                p.getApprovalStatus() != null ? p.getApprovalStatus().name() : null,
                p.getOwner() != null ? p.getOwner().getId() : null
        );
    }
}