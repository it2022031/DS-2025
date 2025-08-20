package com.example.demo.dto;


import java.math.BigDecimal;

public record PropertyPatchRequestDto(
        String name,
        String description,
        String country,
        String city,
        String street,
        String postalCode,
        Double squareMeters ,
        BigDecimal price
        // δεν περιλαμβάνουμε approvalStatus εδώ για user
) {}