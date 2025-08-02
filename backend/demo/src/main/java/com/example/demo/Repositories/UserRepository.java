package com.example.demo.Repositories;

import com.example.demo.Entities.Property;
import com.example.demo.Entities.Rental;
import com.example.demo.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query(
            value = """
    SELECT
      u.id AS user_id,
      array_remove(array_agg(DISTINCT r.id), NULL) AS rental_ids
    FROM users u
    LEFT JOIN rentals r ON r.user_id = u.id
    GROUP BY u.id
    ORDER BY u.id
    """,
            nativeQuery = true
    )
    List<Object[]> fetchUserAndRentalIds();


    @Query("""
      SELECT u FROM User u
      LEFT JOIN FETCH u.rentals r
      WHERE u.username = :username
      """)
    Optional<User> findByUsernameWithRentals(@Param("username") String username);



    // Rentals του χρήστη (φορτώνει και το property για κάθε rental)
    @Query("SELECT r FROM Rental r JOIN FETCH r.property WHERE r.user.id = :userId")
    List<Rental> findRentalsByUserId(@Param("userId") Long userId);

//    // Properties του χρήστη (owner)
//    @Query("SELECT p FROM Property p WHERE p.owner.id = :userId")
//    List<Property> findPropertiesByOwnerId(@Param("userId") Long userId)


}
