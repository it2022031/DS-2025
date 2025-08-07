package com.example.demo.dto;


public record PropertyPatchRequestDto(
        String name,
        String description,
        String country,
        String city,
        String street,
        String postalCode,
        Double squareMeters
        // δεν περιλαμβάνουμε approvalStatus εδώ για user
) {}