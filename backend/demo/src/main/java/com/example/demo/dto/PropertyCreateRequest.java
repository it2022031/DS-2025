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
        // αν ο admin θέλει να το αναθέσει εκτός από query param
        Long ownerId ,
        BigDecimal price
) {}