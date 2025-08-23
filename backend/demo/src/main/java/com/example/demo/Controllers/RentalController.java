package com.example.demo.Controllers;

import com.example.demo.Entities.ApprovalStatus;
import com.example.demo.Entities.Property;
import com.example.demo.Entities.Rental;
import com.example.demo.Entities.User;
import com.example.demo.Repositories.UserRepository;
import com.example.demo.Services.PropertyService;
import com.example.demo.Services.RentalService;
import com.example.demo.dto.RentalCreateRequest;
import com.example.demo.dto.RentalDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rentals")
@CrossOrigin(origins = "*")
public class RentalController {

    private final RentalService rentalService;
    private final UserRepository userRepository;
    private final PropertyService propertyService;

    public RentalController(RentalService rentalService,
                            UserRepository userRepository, PropertyService propertyService) {
        this.rentalService = rentalService;
        this.userRepository = userRepository;
        this.propertyService = propertyService;
    }

    /** 1) List all rentals — admin only **/
    @GetMapping("/all")
    public ResponseEntity<?> getAllRentals(Authentication authentication) {
        boolean isAdmin = authentication != null &&
                authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only admins can list all rentals"));
        }
        List<Map<String, Object>> rentals = rentalService.getAllRentalData();
        return ResponseEntity.ok(rentals);
    }

    @GetMapping("/by-renter/{renterId}")
    public ResponseEntity<?> getRentalsByRenter(
            @PathVariable Long renterId,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        var caller = userRepository.findByUsername(username).orElse(null);

        boolean sameRenter = caller != null && caller.getId().equals(renterId);

        if (!isAdmin && !sameRenter) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only ADMIN or the same renter can view these rentals"));
        }

        var result = rentalService.getRentalsByRenter(renterId);
        return ResponseEntity.ok(result);
    }

    /** 2) Get a single rental by ID (no auth) **/
    @GetMapping("/{id}")
    public ResponseEntity<RentalDto> getRentalById(@PathVariable Long id) {
        Rental rental = rentalService.getRentalWithRelations(id);
        return ResponseEntity.ok(RentalDto.fromEntity(rental));
    }

    /** 3) Create a new rental **/
    @PostMapping("/add")
    public ResponseEntity<?> createRental(
            @Valid @RequestBody RentalCreateRequest req,
            Authentication authentication
    ) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthenticated"));
        }

        boolean isAdmin  = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isRenter = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RENTER"));
        if (!isAdmin && !isRenter) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only RENTERS or ADMIN can create rentals"));
        }

        List<String> missing = new ArrayList<>();
        if (req.propertyId() == null) missing.add("propertyId");
        if (req.startDate()  == null) missing.add("startDate");
        if (req.endDate()    == null) missing.add("endDate");
        if (!missing.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Missing fields: " + String.join(", ", missing)));
        }

        Property prop = propertyService.findByIdOptional(req.propertyId())
                .orElseThrow(() -> new RuntimeException("Property not found"));
        if (prop.getApprovalStatus() != ApprovalStatus.APPROVED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Cannot rent a property unless it is APPROVED"));
        }

        String callerUsername = ((UserDetails) authentication.getPrincipal()).getUsername();
        User caller = userRepository.findByUsername(callerUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        Long finalUserId;
        if (req.userId() != null) {
            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only ADMIN can set userId explicitly"));
            }
            finalUserId = req.userId();
        } else {
            finalUserId = caller.getId();
        }

        try {
            LocalDate start = LocalDate.parse(req.startDate());
            LocalDate end   = LocalDate.parse(req.endDate());

            // ✅ ΝΕΟΣ ΕΛΕΓΧΟΣ: startDate δεν μπορεί να είναι στο παρελθόν
            LocalDate today = LocalDate.now();
            if (start.isBefore(today)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Start date cannot be in the past"));
            }

            // (Το service ήδη ελέγχει ότι end > start κτλ.)
            Rental created = rentalService.createRental(
                    req.propertyId(),
                    finalUserId,
                    start,
                    end
            );

            return ResponseEntity.ok(RentalDto.fromEntity(created));

        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid date format. Use YYYY-MM-DD."));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create rental"));
        }
    }





    // helper to grab username
    private String extractUsername(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails ud) {
            return ud.getUsername();
        }
        return principal.toString();
    }

    // helper to check admin role
    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }


    @GetMapping("/owner")
    public ResponseEntity<?> getMyPropertyRentals(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthenticated"));
        }
        String username = auth.getName();
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // μόνο αν είναι πραγματικά owner (ή admin) τουλάχιστον ενός property
        List<Rental> rentals = rentalService.getRentalsForOwner(owner.getId());
        return ResponseEntity.ok(
                rentals.stream()
                        .map(RentalDto::fromEntity)    // χρησιμοποιείς το ήδη υπάρχον DTO
                        .collect(Collectors.toList())
        );
    }

    @PostMapping("/{rentalId}/approve")
    public ResponseEntity<?> approveRental(@PathVariable Long rentalId,
                                           Authentication authentication) {
        // must be authenticated
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthenticated"));
        }

        Rental rental = rentalService.getById(rentalId);
        Long callerId = getCallerId(authentication);
        Long ownerId  = rental.getProperty().getOwner().getId();

        if (!callerId.equals(ownerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only the property owner can approve rentals"));
        }

        Rental updated = rentalService.setApprovalStatus(rentalId, ApprovalStatus.APPROVED);
        return ResponseEntity.ok(RentalDto.fromEntity(updated));
    }

    /**  Reject **/
    @PostMapping("/{rentalId}/reject")
    public ResponseEntity<?> rejectRental(@PathVariable Long rentalId,
                                          Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthenticated"));
        }

        Rental rental = rentalService.getById(rentalId);
        Long callerId = getCallerId(authentication);
        Long ownerId  = rental.getProperty().getOwner().getId();

        if (!callerId.equals(ownerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only the property owner can reject rentals"));
        }

        Rental updated = rentalService.setApprovalStatus(rentalId, ApprovalStatus.REJECTED);
        return ResponseEntity.ok(RentalDto.fromEntity(updated));
    }


//    private String extractUsername(Authentication auth) {
//        if (auth == null) return null;
//        Object p = auth.getPrincipal();
//        return (p instanceof UserDetails ud) ? ud.getUsername() : p.toString();
//    }

    private Long getCallerId(Authentication auth) {
        String uname = extractUsername(auth);
        return userRepository.findByUsername(uname)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"))
                .getId();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteRentalAsAdmin(@PathVariable Long id) {
        rentalService.adminDeleteRental(id);
        return ResponseEntity.noContent().build(); // 204
    }

    @GetMapping("/status/rejected")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Rental>> getAllRejectedRentals() {
        return ResponseEntity.ok(rentalService.getRejectedRentals());
    }

    @DeleteMapping("/status/rejected")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteAllRejectedRentals() {
        rentalService.deleteAllRejectedRentals();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/maintenance/reject-expired-pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectExpiredPendingManually() {
        int changed = rentalService.rejectExpiredPendingRentalsToday();
        return ResponseEntity.ok(Map.of(
                "updated", changed,
                "message", "Rejected all pending rentals that start in the past."
        ));
    }
}
