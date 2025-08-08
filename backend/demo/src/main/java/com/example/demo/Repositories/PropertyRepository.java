package com.example.demo.Repositories;

import com.example.demo.Entities.ApprovalStatus;
import com.example.demo.Entities.Property;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByOwnerId(Long ownerId);
    @Query("SELECT p FROM Property p WHERE p.owner.id = :userId")
    List<Property> findPropertiesByOwnerId(@Param("userId") Long userId);

    // (προαιρετικό) να φορτώνεις και owner για εξηγήσιμα errors/logs
    @EntityGraph(attributePaths = {"owner"})
    Optional<Property> findWithOwnerById(Long id);

    // (προαιρετικό) χρήσιμο αν θες να επιτρέψεις διαγραφή μόνο από τον owner
    long deleteByIdAndOwner_Id(Long propertyId, Long ownerId);

    List<Property> findByApprovalStatus(ApprovalStatus status);
    void deleteByApprovalStatus(ApprovalStatus status);
}
