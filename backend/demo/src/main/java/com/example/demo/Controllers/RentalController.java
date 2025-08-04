package com.example.demo.Controllers;

import com.example.demo.Entities.Rental;
import com.example.demo.Entities.User;
import com.example.demo.Repositories.UserRepository;
import com.example.demo.Services.RentalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;


import java.util.List;
@RestController
@RequestMapping("/api/rentals")
@CrossOrigin(origins = "*")
public class RentalController {

    private final RentalService rentalService;
    private final UserRepository userRepository;
    public RentalController(RentalService rentalService, UserRepository userRepository) {
        this.rentalService = rentalService;
        this.userRepository = userRepository;
    }

//    @GetMapping("/all")
//    public List<Rental> getAllRentals() {
//        return rentalService.findAll();
//    }
@GetMapping("/all")
public ResponseEntity<?> getAllRentals(Authentication authentication) {
    boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    if (!isAdmin) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Only admins can list all rentals"));
    }
    List<Map<String, Object>> rentals = rentalService.getAllRentalData();
    return ResponseEntity.ok(rentals);
}

//    @PostMapping("/add")
//    public ResponseEntity<Rental> addRental(@RequestBody Rental dto) {
//        Rental saved = rentalService.addRental(dto);
//        return ResponseEntity.ok(saved);
//    }
    @GetMapping("/{id}")
    public ResponseEntity<Rental> getRentalById(@PathVariable Long id) {
        Rental rental = rentalService.getRentalWithRelations(id);
        return ResponseEntity.ok(rental);
    }

//    @PostMapping("/add")
//    public ResponseEntity<?> createRental(@RequestBody Map<String, Object> body) {
//        try {
//            if (!body.containsKey("propertyId") || !body.containsKey("userId") ||
//                    !body.containsKey("startDate") || !body.containsKey("endDate") ||
//                    !body.containsKey("paymentAmount")) {
//                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
//            }
//
//            Long propertyId = Long.valueOf(body.get("propertyId").toString());
//            Long userId = Long.valueOf(body.get("userId").toString());
//            LocalDate startDate = LocalDate.parse(body.get("startDate").toString());
//            LocalDate endDate = LocalDate.parse(body.get("endDate").toString());
//            Double paymentAmount = Double.valueOf(body.get("paymentAmount").toString());
//
//            Rental created = rentalService.createRental(propertyId, userId, startDate, endDate, paymentAmount);
//            return ResponseEntity.ok(RentalDto.fromEntity(created));
//        } catch (DateTimeParseException e) {
//            return ResponseEntity.badRequest().body(Map.of("error", "Invalid date format. Use YYYY-MM-DD."));
//        } catch (IllegalArgumentException | IllegalStateException e) {
//            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body(Map.of("error", "Failed to create rental"));
//        }
//    }
//
//
//
//    // RentalDto.java (μπορείς και ως record αν θες)
//    public record RentalDto(
//            Long id,
//            String startDate,
//            String endDate,
//            Double paymentAmount,
//            Boolean status,
//            Long propertyId,
//            Long userId
//    ) {
//        public static RentalDto fromEntity(Rental r) {
//            return new RentalDto(
//                    r.getId(),
//                    r.getStartDate().toString(),
//                    r.getEndDate().toString(),
//                    r.getPaymentAmount(),
//                    r.getStatus(),
//                    r.getProperty().getId(),
//                    r.getUser().getId()
//            );
//        }
//    }
@PostMapping("/add")
public ResponseEntity<?> createRental(@Valid @RequestBody RentalCreateRequest req,
                                      Authentication authentication) {
    if (authentication == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthenticated"));
    }

    // Έλεγχος required πεδίων (εκτός userId)
    List<String> missing = new ArrayList<>();
    if (req.propertyId() == null) missing.add("propertyId");
    if (req.startDate() == null) missing.add("startDate");
    if (req.endDate() == null) missing.add("endDate");
    if (req.paymentAmount() == null) missing.add("paymentAmount");
    if (!missing.isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of("error", "Missing fields: " + String.join(", ", missing)));
    }

    String callerUsername = extractUsername(authentication);
    User caller = userRepository.findByUsername(callerUsername)
            .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

    Long finalUserId;
    if (req.userId() != null) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only admin can set userId explicitly"));
        }
        finalUserId = req.userId();
    } else {
        finalUserId = caller.getId();
    }

    try {
        LocalDate start = LocalDate.parse(req.startDate());
        LocalDate end = LocalDate.parse(req.endDate());

        Rental created = rentalService.createRental(
                req.propertyId(),
                finalUserId,
                start,
                end,
                req.paymentAmount()
        );
        return ResponseEntity.ok(RentalDto.fromEntity(created));
    } catch (DateTimeParseException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid date format. Use YYYY-MM-DD."));
    } catch (IllegalArgumentException | IllegalStateException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(500).body(Map.of("error", "Failed to create rental"));
    }
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
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
    public record RentalCreateRequest(
            Long propertyId,
            String startDate,    // "YYYY-MM-DD"
            String endDate,      // "YYYY-MM-DD"
            Double paymentAmount,
            Long userId          // προαιρετικό: μόνο admin μπορεί να το ορίσει
    ) {}
    public record RentalDto(
            Long id,
            String startDate,
            String endDate,
            Double paymentAmount,
            Boolean status,
            Long propertyId,
            Long userId
    ) {
        public static RentalDto fromEntity(Rental r) {
            return new RentalDto(
                    r.getId(),
                    r.getStartDate().toString(),
                    r.getEndDate().toString(),
                    r.getPaymentAmount(),
                    r.getStatus(),
                    r.getProperty().getId(),
                    r.getUser().getId()
            );
        }
    }
}

