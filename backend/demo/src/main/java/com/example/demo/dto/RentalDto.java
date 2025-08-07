    package com.example.demo.dto;
    import com.example.demo.Entities.Rental;
    import com.example.demo.Entities.ApprovalStatus;

    /**
     * DTO for transferring Rental data to clients
     */
    public record RentalDto(
            Long id,
            String startDate,
            String endDate,
            Double paymentAmount,
            String approvalStatus,
            Long propertyId,
            Long userId
    ) {
        public static RentalDto fromEntity(Rental r) {
            return new RentalDto(
                    r.getId(),
                    r.getStartDate().toString(),
                    r.getEndDate().toString(),
                    r.getPaymentAmount(),
                    r.getApprovalStatus() != null ? r.getApprovalStatus().name() : null,
                    r.getProperty().getId(),
                    r.getUser().getId()
            );
        }
    }
