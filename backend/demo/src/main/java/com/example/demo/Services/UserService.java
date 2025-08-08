package com.example.demo.Services;

import com.example.demo.Entities.ApprovalStatus;
import com.example.demo.Entities.Property;
import com.example.demo.Entities.Rental;
import com.example.demo.Entities.User;
import com.example.demo.Repositories.PropertyRepository;
import com.example.demo.Repositories.RentalRepository;
import com.example.demo.Repositories.UserRepository;
import com.example.demo.Security.Role;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final RentalRepository rentalRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PropertyRepository propertyRepository,
                       RentalRepository rentalRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.rentalRepository = rentalRepository;
        this.passwordEncoder = passwordEncoder;
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


    public User updateUserPartial(Long userId, Map<String, Object> updates) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updates.containsKey("username")) {
            String newUsername = updates.get("username").toString().trim();
            if (!newUsername.isEmpty() && !newUsername.equals(user.getUsername())) {
                if (userRepository.existsByUsernameAndIdNot(newUsername, userId)) {
                    throw new IllegalArgumentException("Username already taken");
                }
                user.setUsername(newUsername);
            }
        }

        if (updates.containsKey("email")) {
            String newEmail = updates.get("email").toString().trim();
            if (!newEmail.isEmpty() && !newEmail.equals(user.getEmail())) {
                if (userRepository.existsByEmailAndIdNot(newEmail, userId)) {
                    throw new IllegalArgumentException("Email already in use");
                }
                // απλός έλεγχος μορφής (μπορείς να βάλεις πιο αυστηρό ή validator)
                if (!newEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                    throw new IllegalArgumentException("Invalid email format");
                }
                user.setEmail(newEmail);
            }
        }

        if (updates.containsKey("firstName")) {
            String fn = updates.get("firstName").toString().trim();
            user.setFirstName(fn);
        }

        if (updates.containsKey("lastName")) {
            String ln = updates.get("lastName").toString().trim();
            user.setLastName(ln);
        }

        if (updates.containsKey("passportNumber")) {
            String pn = updates.get("passportNumber").toString().trim();
            user.setPassportNumber(pn);
        }

        if (updates.containsKey("afm")) {
            String afm = updates.get("afm").toString().trim();
            user.setAfm(afm);
        }

        if (updates.containsKey("password")) {
            String pw = updates.get("password").toString();
            if (pw.length() < 6) {
                throw new IllegalArgumentException("Password must be at least 6 characters");
            }
            user.setPassword(passwordEncoder.encode(pw));
        }

        return userRepository.save(user);
    }
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /** Ορίζει την αίτηση του χρήστη σε PENDING. */
    @Transactional
    public User requestRenter(String username) {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        u.setRenterRequestStatus(ApprovalStatus.PENDING);
        return userRepository.save(u);
    }

    /**
     * Ο admin καλεί αυτό για να εγκρίνει ή να απορρίψει την αίτηση.
     * Αν status==APPROVED, αλλάζει και τον ρόλο σε RENTER.
     */
    @Transactional
    public User handleRenterRequest(Long userId, ApprovalStatus newStatus) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
        u.setRenterRequestStatus(newStatus);
        if (newStatus == ApprovalStatus.APPROVED) {
            u.addRole(Role.RENTER);
        }
        return userRepository.save(u);
    }

    /** (Προαιρετικό) Γενική μέθοδος αλλαγής ρόλου. */
    @Transactional
    public User updateUserRole(Long userId, Role newRole) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
        u.addRole(newRole);
        return userRepository.save(u);
    }

    /**
     * Επιστρέφει όλους τους χρήστες που έχουν renterRequestStatus == PENDING
     */
    public List<User> findAllRenterRequests() {
        return userRepository.findByRenterRequestStatus(ApprovalStatus.PENDING);
    }

    /**
     * Επίσης, μέθοδος για να εγκρίνει ή να απορρίψει το αίτημα
     */
    public User processRenterRequest(Long userId, boolean approve) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (approve) {
            u.addRole(Role.RENTER);
            u.setRenterRequestStatus(ApprovalStatus.APPROVED);
        } else {
            u.setRenterRequestStatus(ApprovalStatus.REJECTED);
        }
        return userRepository.save(u);
    }

    @Transactional
    public void adminDeleteUser(Long targetUserId, Long callerUserId) {
        if (targetUserId.equals(callerUserId)) {
            throw new IllegalArgumentException("Admins cannot delete themselves.");
        }

        User toDelete = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + targetUserId));

        // (προαιρετικό) αν δεν θέλεις να σβήνονται άλλοι admins, βάλε εδώ έλεγχο ρόλου

        userRepository.delete(toDelete); // ενεργοποιεί cascade/orphanRemoval προς properties/rentals
    }


}
