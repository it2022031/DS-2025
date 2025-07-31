package com.example.demo.Repositories;

import com.example.demo.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface UserRepository extends JpaRepository<User, Long> {
    // custom methods if needed
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u")
    List<User> getAllUsers();

    @Query(
            value = """
      SELECT
        u.id                     AS user_id,
        array_remove(array_agg(DISTINCT p.id), NULL) AS property_ids,
        array_remove(array_agg(DISTINCT r.id), NULL) AS rental_ids
      FROM users u
      LEFT JOIN properties p ON p.user_id = u.id
      LEFT JOIN rentals   r ON r.user_id = u.id
      GROUP BY u.id
      ORDER BY u.id
      """,
            nativeQuery = true
    )
    List<Object[]> fetchUserPropertyAndRentalIds();
}
