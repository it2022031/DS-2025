package com.example.demo.dto;

import com.example.demo.Entities.Property;

import java.math.BigDecimal;

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
        Long ownerId,
        String ownerFirstName,   // νέο
        String ownerLastName,    // νέο
        String ownerUsername,    // νέο
        BigDecimal price
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
                p.getOwner() != null ? p.getOwner().getId() : null,
                p.getOwner() != null ? p.getOwner().getFirstName() : null,
                p.getOwner() != null ? p.getOwner().getLastName() : null,
                p.getOwner() != null ? p.getOwner().getUsername() : null,
                p.getPrice()
        );
    }
}