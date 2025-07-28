package com.example.demo.Repositories;

import com.example.demo.Entities.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    // Αν θες μπορείς να βάλεις custom queries
}