package com.example.demo.Services;

import com.example.demo.Entities.Rental;
import com.example.demo.Repositories.RentalRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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

    // Προαιρετικά μπορείς να βάλεις κι άλλες μεθόδους, πχ update, delete κτλ
}
