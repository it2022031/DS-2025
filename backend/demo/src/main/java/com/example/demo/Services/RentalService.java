package com.example.demo.Services;

import com.example.demo.Entities.ApprovalStatus;
import com.example.demo.Entities.Property;
import com.example.demo.Entities.Rental;
import com.example.demo.Entities.User;
import com.example.demo.Repositories.PropertyRepository;
import com.example.demo.Repositories.RentalRepository;
import com.example.demo.Repositories.UserRepository;
import com.example.demo.dto.RentalSummaryDto;
//import jakarta.transaction.Transactional;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RentalService {

    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;

    public RentalService(RentalRepository rentalRepository,
                         UserRepository userRepository,
                         PropertyRepository propertyRepository) {
        this.rentalRepository = rentalRepository;
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
    }

    public List<Rental> findAll() {
        return rentalRepository.findAll();
    }

    public Rental addRental(Rental rental) {
        return rentalRepository.save(rental);
    }
    public Rental getRentalWithRelations(Long rentalId) {
        return rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found"));
    }

    public List<Map<String, Object>> getAllRentalData() {
        List<Object[]> rentalsData = rentalRepository.findAllRentalData();

        List<Map<String, Object>> rentals = new ArrayList<>();

        for (Object[] row : rentalsData) {
            Map<String, Object> rentalMap = new HashMap<>();
            rentalMap.put("id", row[0]);
            rentalMap.put("startDate", row[1]);
            rentalMap.put("endDate", row[2]);
            rentalMap.put("paymentAmount", row[3]);
            rentalMap.put("status", row[4]);
            rentalMap.put("userId", row[5]);
            rentalMap.put("propertyId", row[6]);
            rentals.add(rentalMap);
        }

        return rentals;

    }



//    @Transactional
//    public Rental createRental(Long propertyId, Long userId,
//                               LocalDate startDate, LocalDate endDate,
//                               Double paymentAmount) {
//        if (startDate == null || endDate == null) {
//            throw new IllegalArgumentException("Start and end dates are required");
//        }
//        if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
//            throw new IllegalArgumentException("End date must be after start date");
//        }
//        if (paymentAmount == null || paymentAmount < 0) {
//            throw new IllegalArgumentException("Payment amount must be non-negative");
//        }
//
//        Property property = propertyRepository.findById(propertyId)
//                .orElseThrow(() -> new IllegalArgumentException("Property not found with id " + propertyId));
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found with id " + userId));
//
//        // Έλεγχος για overlap
//        List<Rental> overlapping = rentalRepository.findActiveOverlapping(propertyId, startDate, endDate);
//        if (!overlapping.isEmpty()) {
//            throw new IllegalStateException("Property is already rented in the given period");
//        }
//
//        Rental rental = new Rental();
//        rental.setProperty(property);
//        rental.setUser(user);
//        rental.setStartDate(startDate);
//        rental.setEndDate(endDate);
//        rental.setPaymentAmount(paymentAmount);
//        rental.setStatus(true); // ενεργό
//
//        return rentalRepository.save(rental);
//    }


    @Transactional
    public Rental createRental(Long propertyId,
                               Long userId,
                               LocalDate startDate,
                               LocalDate endDate) {

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("startDate and endDate are required");
        }
        if (!endDate.isAfter(startDate)) { // exclusive end (διανυκτερεύσεις)
            throw new IllegalArgumentException("endDate must be after startDate");
        }

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        if (property.getPrice() == null) {
            throw new IllegalStateException("Property price is not set");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Έλεγχος επικάλυψης
        List<Rental> overlapping = rentalRepository.findOverlappingActiveRentals(
                propertyId, startDate, endDate);
        if (!overlapping.isEmpty()) {
            throw new IllegalStateException("Property already has an active overlapping rental");
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate); // 20–25 => 5 μέρες
        if (days <= 0) {
            throw new IllegalArgumentException("Rental must be at least 1 day");
        }

        BigDecimal amount = property.getPrice()                       // BigDecimal
                .multiply(BigDecimal.valueOf(days))
                .setScale(2, RoundingMode.HALF_UP);

        Rental rental = new Rental();
        rental.setStartDate(startDate);
        rental.setEndDate(endDate);
        rental.setPaymentAmount(amount);                              // BigDecimal στο entity
        rental.setApprovalStatus(ApprovalStatus.PENDING);
        rental.setProperty(property);
        rental.setUser(user);

        return rentalRepository.save(rental);
    }

    @Transactional
    public Rental setApprovalStatus(Long rentalId, ApprovalStatus newStatus) {
        Rental r = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found with id " + rentalId));
        r.setApprovalStatus(newStatus);
        return rentalRepository.save(r);
    }

    public Rental getById(Long rentalId) {
        return rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found with id " + rentalId));
    }

    public List<Rental> getRentalsForOwner(Long ownerId) {
        return rentalRepository.findByPropertyOwnerId(ownerId);
    }

    @Transactional
    public void adminDeleteRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Rental not found: " + rentalId));

        // Αν χρειάζεσαι validations (π.χ. Nα μην είναι ήδη πληρωμένο), βάλε εδώ
        rentalRepository.delete(rental);
    }


    public List<Rental> getRejectedRentals() {
        return rentalRepository.findByApprovalStatus(ApprovalStatus.REJECTED);
    }

    @Transactional
    public void deleteAllRejectedRentals() {
        rentalRepository.deleteByApprovalStatus(ApprovalStatus.REJECTED);
    }

    @Transactional(readOnly = true)
    public List<RentalSummaryDto> getRentalsByRenter(Long renterId) {
        return rentalRepository.findAllByRenterIdWithPropertyOwner(renterId)
                .stream()
                .map(RentalSummaryDto::fromEntity)
                .toList();
    }


}
