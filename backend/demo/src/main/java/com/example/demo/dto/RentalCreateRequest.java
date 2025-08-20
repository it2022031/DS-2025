package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for creating a new Rental.
 * userId is optional and should only be set by admin requests.
 */
public record RentalCreateRequest(
        @NotNull(message = "propertyId is required")
        Long propertyId,

        @NotNull(message = "startDate is required")
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "startDate must be in YYYY-MM-DD format")
        String startDate,

        @NotNull(message = "endDate is required")
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "endDate must be in YYYY-MM-DD format")
        String endDate,

        /**
         * Optional: only admin should provide this to create a rental for another user.
         */
        Long userId
) {}
