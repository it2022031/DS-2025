package com.example.demo.Services;

import com.example.demo.Entities.Property;
import com.example.demo.Entities.Rental;
import com.example.demo.Entities.User;
import com.example.demo.Repositories.PropertyRepository;
import com.example.demo.Repositories.RentalRepository;
import com.example.demo.Repositories.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final RentalRepository rentalRepository;

    public UserService(UserRepository userRepository,
                       PropertyRepository propertyRepository,
                       RentalRepository rentalRepository) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.rentalRepository = rentalRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User addUser(User user) {
        return userRepository.save(user);
    }

    public Long getNextId() {
        return userRepository.count() + 1;
    }

    public User addUserAnalytical(User user) {
        return userRepository.save(user);
    }

    public List<Map<String, Object>> getUserRelations() {
        List<Object[]> rows = userRepository.fetchUserPropertyAndRentalIds();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : rows) {
            Long userId = ((Number) row[0]).longValue();

            // row[1] & row[2] μπορεί να είναι Long[] ή java.sql.Array
            Object propObj = row[1];
            Object rentObj = row[2];

            List<Long> propertyIds = new ArrayList<>();
            if (propObj != null) {
                if (propObj instanceof Long[]) {
                    for (Long id : (Long[]) propObj) {
                        if (id != null) propertyIds.add(id);
                    }
                } else if (propObj instanceof java.sql.Array) {
                    try {
                        Object[] raw = (Object[]) ((java.sql.Array) propObj).getArray();
                        for (Object o : raw) {
                            if (o != null) propertyIds.add(((Number) o).longValue());
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException("Error reading property IDs", e);
                    }
                }
            }

            List<Long> rentalIds = new ArrayList<>();
            if (rentObj != null) {
                if (rentObj instanceof Long[]) {
                    for (Long id : (Long[]) rentObj) {
                        if (id != null) rentalIds.add(id);
                    }
                } else if (rentObj instanceof java.sql.Array) {
                    try {
                        Object[] raw = (Object[]) ((java.sql.Array) rentObj).getArray();
                        for (Object o : raw) {
                            if (o != null) rentalIds.add(((Number) o).longValue());
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException("Error reading rental IDs", e);
                    }
                }
            }

            Map<String, Object> map = new HashMap<>();
            map.put("userId",      userId);
            map.put("propertyIds", propertyIds);
            map.put("rentalIds",   rentalIds);
            result.add(map);
        }

        return result;
    }

//    // Ξεχωριστά: rentals του χρήστη με βάση το userId
//    public List<Rental> getRentalsForUserId(Long userId) {
//        // αν θέλεις να εξακριβώσεις ότι υπάρχει user
//        userRepository.findById(userId)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found with id " + userId));
//        return userRepository.findRentalsByUserId(userId);
//    }

    // Ξεχωριστά: properties του χρήστη με βάση το userId
//    public List<Property> getPropertiesForUserId(Long userId) {
//        userRepository.findById(userId)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found with id " + userId));
//        return userRepository.findPropertiesByOwnerId(userId);
//    }
    public List<Property> getPropertiesForUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id " + userId));
        return propertyRepository.findByOwnerId(userId);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // Υπάρχει ήδη, και πρέπει να ελέγχει ότι ο user υπάρχει:
    public List<Rental> getRentalsForUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id " + userId));
        return rentalRepository.findRentalsByUserId(userId); // ή όποια μέθοδο έχεις για να φέρνεις τα rentals
    }
//    public Optional<User> findById(Long id) {
//        return userRepository.findById(id);
//    }
}
