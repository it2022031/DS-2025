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

    /**
     * Επιστρέφει κάθε σειριακή προβολή των rentals μαζί με τα IDs χρήστη και ακινήτου.
     * Χρήση για admin λίστα.
     */
    @Query("""
          SELECT r.id,
                 r.startDate,
                 r.endDate,
                 r.paymentAmount,
                 r.approvalStatus,
                 r.user.id,
                 r.property.id
          FROM Rental r
      """)
    List<Object[]> findAllRentalData();

    /**
     * Όλα τα rentals, π.χ. για απλή προεπιλεγμένη χρήση.
     */
    @Override
    List<Rental> findAll();

    /**
     * Βρίσκει ένα rental κατά ID (κληρονομείται, αλλά μπορείς να το ορίσεις αν θέλεις).
     */
    @Override
    Optional<Rental> findById(Long id);

    /**
     * Rentals για συγκεκριμένο χρήστη.
     */
    List<Rental> findRentalsByUserId(Long userId);

    /**
     * Ενεργά rentals που επικαλύπτονται με το ζητούμενο διάστημα για ένα ακίνητο.
     */
    @Query(
            "SELECT r FROM Rental r"
                    + " WHERE r.property.id = :propertyId"
                    + "   AND r.approvalStatus = 'APPROVED'"
                    + "   AND NOT (r.endDate < :start OR r.startDate > :end)"
    )
    List<Rental> findOverlappingActiveRentals(
            @Param("propertyId") Long propertyId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    List<Rental> findByPropertyOwnerId(Long ownerId);
}
