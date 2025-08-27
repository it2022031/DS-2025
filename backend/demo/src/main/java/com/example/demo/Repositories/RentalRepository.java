package com.example.demo.Repositories;

import com.example.demo.Entities.ApprovalStatus;
import com.example.demo.Entities.Rental;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {

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

    @Override
    List<Rental> findAll();

    @Override
    Optional<Rental> findById(Long id);

    List<Rental> findRentalsByUserId(Long userId);

    @Query("""
        SELECT r FROM Rental r
        WHERE r.property.id = :propertyId
          AND r.approvalStatus IN ('APPROVED','PENDING')
          AND r.endDate  > :start
          AND r.startDate < :end
""")
    List<Rental> findOverlappingActiveRentals(
            @Param("propertyId") Long propertyId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    List<Rental> findByPropertyOwnerId(Long ownerId);

    long deleteByIdAndUser_Id(Long rentalId, Long userId);

    List<Rental> findByApprovalStatus(ApprovalStatus status);

    void deleteByApprovalStatus(ApprovalStatus status);

    List<Rental> findByPropertyIdAndUserId(Long propertyId, Long userId);

    List<Rental> findByPropertyIdAndApprovalStatusAndEndDateGreaterThanEqual(
            Long propertyId, ApprovalStatus status, LocalDate date);

    // Βρίσκω rentals με βάση propertyId, approvalStatus, dates
    List<Rental> findByPropertyIdAndApprovalStatusInAndEndDateGreaterThanEqual(
            Long propertyId, Collection<ApprovalStatus> statuses, LocalDate date);

    @Query("""
       SELECT r
       FROM Rental r
       JOIN FETCH r.property p
       JOIN FETCH p.owner o
       WHERE r.user.id = :renterId
       ORDER BY r.startDate DESC
       """)
    List<Rental> findAllByRenterIdWithPropertyOwner(@Param("renterId") Long renterId);

    List<Rental> findTop1ByUserIdAndPropertyIdAndApprovalStatusAndEndDateBeforeOrderByEndDateDesc(
            Long userId, Long propertyId, ApprovalStatus status, LocalDate before);

    @Modifying
    @Transactional
    @Query("""
       update Rental r
          set r.approvalStatus = 'REJECTED'
        where r.approvalStatus = 'PENDING'
          and r.startDate < :today
       """)
    int rejectExpiredPending(@Param("today") LocalDate today);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Rental r
           set r.approvalStatus = 'REJECTED'
         where r.approvalStatus = 'PENDING'
           and r.startDate < :today
    """)
    int bulkRejectExpiredPending(@Param("today") LocalDate today);
}
