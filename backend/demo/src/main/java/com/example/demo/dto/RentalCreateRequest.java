package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RentalCreateRequest(

        @NotNull(message = "propertyId is required")
        Long propertyId,

        @NotNull(message = "startDate is required")
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "startDate must be in YYYY-MM-DD format")
        String startDate,

        @NotNull(message = "endDate is required")
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "endDate must be in YYYY-MM-DD format")
        String endDate,

        Long userId
) {}
