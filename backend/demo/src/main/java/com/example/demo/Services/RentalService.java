package com.example.demo.Services;

import com.example.demo.Entities.ApprovalStatus;
import com.example.demo.Entities.Property;
import com.example.demo.Entities.Rental;
import com.example.demo.Entities.User;
import com.example.demo.Repositories.PropertyRepository;
import com.example.demo.Repositories.RentalRepository;
import com.example.demo.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
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


    public Rental createRental(Long propertyId,
                               Long userId,
                               LocalDate startDate,
                               LocalDate endDate,
                               Double paymentAmount) {

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must be on or before endDate");
        }

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Έλεγχος επικάλυψης
        List<Rental> overlapping = rentalRepository.findOverlappingActiveRentals(
                propertyId, startDate, endDate);
        if (!overlapping.isEmpty()) {
            throw new IllegalStateException("Property already has an active overlapping rental");
        }

        Rental rental = new Rental();
        rental.setStartDate(startDate);
        rental.setEndDate(endDate);
        rental.setPaymentAmount(paymentAmount);
        rental.setApprovalStatus(ApprovalStatus.PENDING); // default approved, μπορείς να αλλάξεις αν θέλεις workflow
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



}
