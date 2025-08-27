package com.example.demo.dto;


import java.math.BigDecimal;

public record PropertyCreateRequest(
        String name,
        String description,
        String country,
        String city,
        String street,
        String postalCode,
        Double squareMeters,
        Long ownerId ,
        BigDecimal price
) {}