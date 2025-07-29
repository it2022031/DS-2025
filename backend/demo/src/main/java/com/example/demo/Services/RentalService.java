package com.example.demo.Services;

import com.example.demo.Entities.Rental;
import com.example.demo.Repositories.RentalRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RentalService {

    private final RentalRepository rentalRepository;

    public RentalService(RentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
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

    // Προαιρετικά μπορείς να βάλεις κι άλλες μεθόδους, πχ update, delete κτλ
}
