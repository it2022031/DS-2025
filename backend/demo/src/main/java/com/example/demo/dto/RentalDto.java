package com.example.demo.dto;

import com.example.demo.Entities.Rental;
import com.example.demo.Entities.Property;
import com.example.demo.Entities.User;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record RentalDto(
        // Όνομα πεδίων
        Long id,
        String startDate,
        String endDate,
        BigDecimal paymentAmount,
        @JsonProperty("TotalPrice")
        BigDecimal totalPrice,
        String approvalStatus,
        Long propertyId,
        String propertyName,
        Long userId,
        String renterFirstName,
        String renterLastName,
        String renterUserName
) {
    public static RentalDto fromEntity(Rental r) {
        Property p = r.getProperty();
        User u = r.getUser();

        BigDecimal amount = r.getPaymentAmount();

        // Τιμές πεδίων από entity
        return new RentalDto(
                r.getId(),
                r.getStartDate() != null ? r.getStartDate().toString() : null,
                r.getEndDate()   != null ? r.getEndDate().toString()   : null,
                amount,                         // paymentAmount
                amount,                         // totalPrice = paymentAmount
                r.getApprovalStatus() != null ? r.getApprovalStatus().name() : null,
                p != null ? p.getId() : null,   // propertyId
                p != null ? p.getName() : null, // propertyName
                u != null ? u.getId() : null,   // userId (renter)
                u != null ? u.getFirstName() : null,
                u != null ? u.getLastName() : null,
                u != null ? u.getUsername() : null
        );
    }
}
