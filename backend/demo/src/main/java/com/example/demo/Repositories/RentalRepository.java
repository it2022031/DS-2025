package com.example.demo.Repositories;

import com.example.demo.Entities.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    // Αν θες μπορείς να βάλεις custom queries
    @Query("SELECT r.id, r.startDate, r.endDate, r.paymentAmount, r.status, r.user.id, r.property.id FROM Rental r")
    List<Object[]> findAllRentalData();
    List<Rental> findAll();
    Optional<Rental> findById(Long id);

    List<Rental> findRentalsByUserId(Long userId);

    // Βρίσκει ενεργά rentals που επικαλύπτονται με το ζητούμενο διάστημα για το ίδιο property
    @Query("""
        SELECT r FROM Rental r
        WHERE r.property.id = :propertyId
          AND r.status = true
          AND NOT (r.endDate < :startDate OR r.startDate > :endDate)
        """)
    List<Rental> findActiveOverlapping(
            @Param("propertyId") Long propertyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT r FROM Rental r
        WHERE r.property.id = :propertyId
          AND r.status = true
          AND NOT (r.endDate < :start OR r.startDate > :end)
    """)
    List<Rental> findOverlappingActiveRentals(
            @Param("propertyId") Long propertyId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}