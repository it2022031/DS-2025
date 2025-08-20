package com.example.demo.dto;

import com.example.demo.Entities.Rental;
import java.math.BigDecimal;

/**
 * DTO for transferring Rental data to clients
 */
public record RentalDto(
        Long id,
        String startDate,
        String endDate,
        BigDecimal paymentAmount,   // BigDecimal για ακρίβεια
        String approvalStatus,
        Long propertyId,
        Long userId
) {
    public static RentalDto fromEntity(Rental r) {
        return new RentalDto(
                r.getId(),
                r.getStartDate() != null ? r.getStartDate().toString() : null,
                r.getEndDate()   != null ? r.getEndDate().toString()   : null,
                r.getPaymentAmount(), // υποθέτουμε BigDecimal στο entity
                r.getApprovalStatus() != null ? r.getApprovalStatus().name() : null,
                r.getProperty() != null ? r.getProperty().getId() : null,
                r.getUser()     != null ? r.getUser().getId()     : null
        );
    }
}
