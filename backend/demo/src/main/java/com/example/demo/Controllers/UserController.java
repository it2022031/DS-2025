package com.example.demo.Controllers;

import com.example.demo.Entities.Property;
import com.example.demo.Entities.Rental;
import com.example.demo.Entities.User;
import com.example.demo.Repositories.UserRepository;
import com.example.demo.Services.UserService;
import com.example.demo.Controllers.AuthController.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService; // για rentals / properties

    public UserController(UserRepository userRepository,
                          UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

//    // helper για να ελέγχει αν ο authenticated έχει ρόλο ADMIN
//    private boolean isAdmin(Authentication authentication) {
//        return authentication.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .anyMatch(auth -> auth.equals("ROLE_ADMIN") || auth.equals("ADMIN"));
//    }
//
//    // helper για να βγάζουμε username από principal
//    private String extractUsername(Authentication authentication) {
//        Object principal = authentication.getPrincipal();
//        if (principal instanceof UserDetails ud) {
//            return ud.getUsername();
//        } else {
//            return principal.toString();
//        }
//    }

    // 1. Το προφίλ του τρέχοντα χρήστη
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthenticated");
        }
        String username = extractUsername(authentication);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    // 2. Όλοι οι χρήστες — μόνο admin
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserResponse> allUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

//    // 3. Rentals ενός χρήστη με ID: είτε ο ίδιος είτε ADMIN
//    @GetMapping("/{id}/rentals")
//    public ResponseEntity<?> getRentalsForUserId(@PathVariable Long id, Authentication authentication) {
//        User target = userRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        String callerUsername = extractUsername(authentication);
//        boolean ok = isAdmin(authentication) || callerUsername.equals(target.getUsername());
//        if (!ok) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
//        }
//
//        List<Rental> rentals = userService.getRentalsForUserId(id);
//        List<RentalDto> dto = rentals.stream()
//                .map(RentalDto::fromEntity)
//                .collect(Collectors.toList());
//        return ResponseEntity.ok(dto);
//    }

    // 4. Properties ενός χρήστη με ID: είτε ο ίδιος είτε ADMIN
//    @GetMapping("/{id}/properties")
//    public ResponseEntity<?> getPropertiesForUserId(@PathVariable Long id, Authentication authentication) {
//        User target = userRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        String callerUsername = extractUsername(authentication);
//        boolean ok = isAdmin(authentication) || callerUsername.equals(target.getUsername());
//        if (!ok) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
//        }
//
//        List<Property> props = userService.getPropertiesForUserId(id);
//        List<PropertyDto> dto = props.stream()
//                .map(PropertyDto::fromEntity)
//                .collect(Collectors.toList());
//        return ResponseEntity.ok(dto);
//    }

    // --- DTOs για output --- //

//    public static record RentalDto(Long id,
//                                   String startDate,
//                                   String endDate,
//                                   Boolean status,
//                                   Long propertyId,
//                                   Double paymentAmount) {
//        public static RentalDto fromEntity(Rental r) {
//            return new RentalDto(
//                    r.getId(),
//                    r.getStartDate() != null ? r.getStartDate().toString() : null,
//                    r.getEndDate() != null ? r.getEndDate().toString() : null,
//                    r.isStatus(),
//                    r.getProperty() != null ? r.getProperty().getId() : null,
//                    r.getPaymentAmount()
//            );
//        }
//    }

//    public static record PropertyDto(Long id,
//                                     String name,
//                                     String city,
//                                     String country,
//                                     Boolean status,
//                                     Long ownerId) {
//        public static PropertyDto fromEntity(Property p) {
//            return new PropertyDto(
//                    p.getId(),
//                    p.getName(),
//                    p.getCity(),
//                    p.getCountry(),
//                    p.getStatus(),
//                    p.getOwner() != null ? p.getOwner().getId() : null
//            );
//        }
//    }
    @GetMapping("/{id}/properties")
    public ResponseEntity<?> getPropertiesForUserId(@PathVariable Long id, Authentication authentication) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String callerUsername = extractUsername(authentication);
        boolean ok = isAdmin(authentication) || callerUsername.equals(target.getUsername());
        if (!ok) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }

        List<Property> props = userService.getPropertiesForUserId(id);
        return ResponseEntity.ok(props); // χωρίς DTO, επιστρέφεις απευθείας entities
    }

    @GetMapping("/{id}/rentals")
    public ResponseEntity<?> getRentalsForUserId(@PathVariable Long id, Authentication authentication) {
        // Πάρε τον target user για να ελέγξεις δικαιώματα
        User target = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String callerUsername = extractUsername(authentication);
        boolean allowed = isAdmin(authentication) || callerUsername.equals(target.getUsername());
        if (!allowed) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Forbidden"));
        }

        // Φέρε τα rentals
        List<Rental> rentals = userService.getRentalsForUserId(id);
        return ResponseEntity.ok(rentals);
    }

    private String extractUsername(Authentication authentication) {
        if (authentication == null) return null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails ud) {
            return ud.getUsername();
        }
        return principal.toString();
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")); // ή "ADMIN" αν έχεις διαμορφώσει διαφορετικά
    }
}
