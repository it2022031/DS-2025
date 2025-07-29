package com.example.demo.Repositories;

import com.example.demo.Entities.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    // Αν θες μπορείς να βάλεις custom queries
    @Query("SELECT r.id, r.startDate, r.endDate, r.paymentAmount, r.status, r.user.id, r.property.id FROM Rental r")
    List<Object[]> findAllRentalData();
    List<Rental> findAll();
    Optional<Rental> findById(Long id);
}