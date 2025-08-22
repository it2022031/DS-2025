package com.example.demo.dto;

import com.example.demo.Entities.Rental;
import java.math.BigDecimal;

public record RentalSummaryDto(
        Long rentalId,        // <-- ΝΕΟ
        Long propertyId,
        String propertyName,
        String ownerName,
        Long ownerId,
        String startDate,
        String endDate,
        String status,
        BigDecimal totalPrice
) {
    public static RentalSummaryDto fromEntity(Rental r) {
        return new RentalSummaryDto(
                r.getId(),                                // <-- ΝΕΟ
                r.getProperty().getId(),
                r.getProperty().getName(),
                r.getProperty().getOwner().getFirstName()
                        + " " + r.getProperty().getOwner().getLastName(),
                r.getProperty().getOwner().getId(),
                r.getStartDate().toString(),
                r.getEndDate().toString(),
                r.getApprovalStatus() != null ? r.getApprovalStatus().name() : null,
                r.getPaymentAmount()
        );
    }
}
