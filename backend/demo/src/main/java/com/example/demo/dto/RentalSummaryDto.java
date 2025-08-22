package com.example.demo.dto;

import com.example.demo.Entities.Rental;
import java.math.BigDecimal;

public record RentalSummaryDto(
        Long propertyId,
        String propertyName,
        Long ownerId,
        String ownerName,
        String startDate,
        String endDate,
        String status,
        BigDecimal totalPrice
) {
    public static RentalSummaryDto fromEntity(Rental r) {
        var p = r.getProperty();
        var o = p.getOwner();
        String ownerFullName = ((o.getFirstName() == null ? "" : o.getFirstName()) + " " +
                (o.getLastName()  == null ? "" : o.getLastName())).trim();
        if (ownerFullName.isEmpty()) ownerFullName = o.getUsername();

        return new RentalSummaryDto(
                p.getId(),
                p.getName(),
                o.getId(),
                ownerFullName,
                r.getStartDate().toString(),
                r.getEndDate().toString(),
                r.getApprovalStatus() != null ? r.getApprovalStatus().name() : null,
                r.getPaymentAmount()
        );
    }
}
