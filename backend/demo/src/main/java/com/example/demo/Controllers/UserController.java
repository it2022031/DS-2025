package com.example.demo.Controllers;

import com.example.demo.Entities.Property;
import com.example.demo.Entities.Rental;
import com.example.demo.Entities.User;
import com.example.demo.Repositories.UserRepository;
import com.example.demo.Services.UserService;
import com.example.demo.Controllers.AuthController.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
    @GetMapping
    public ResponseEntity<?> allUsers(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only admins can list all users"));
        }
        List<UserResponse> users = userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}/properties")
    public ResponseEntity<?> getPropertiesForUserId(@PathVariable Long id, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthenticated"));
        }

        String callerUsername = extractUsername(authentication);
        User caller = userRepository.findByUsername(callerUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        boolean admin = isAdmin(authentication);
        if (!admin && !caller.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Forbidden"));
        }

        List<Property> props = userService.getPropertiesForUserId(id);
        return ResponseEntity.ok(props);
    }

    @GetMapping("/{id}/rentals")
    public ResponseEntity<?> getRentalsForUserId(@PathVariable Long id, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthenticated"));
        }

        String callerUsername = extractUsername(authentication);
        User caller = userRepository.findByUsername(callerUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        boolean admin = isAdmin(authentication);
        if (!admin && !caller.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Forbidden"));
        }

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

    @PatchMapping("/me")
    public ResponseEntity<?> patchMe(Authentication authentication,
                                     @RequestBody Map<String, Object> updates) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails ud)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthenticated"));
        }
        String username = ud.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            User updated = userService.updateUserPartial(user.getId(), updates);
            return ResponseEntity.ok(AuthController.UserResponse.fromEntity(updated)); // ή αν έχεις ξεχωριστό DTO
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> patchUserById(@PathVariable Long id,
                                           @RequestBody Map<String, Object> updates) {
        try {
            User updated = userService.updateUserPartial(id, updates);
            return ResponseEntity.ok(AuthController.UserResponse.fromEntity(updated));
        } catch (IllegalArgumentException e) {
            // validation / conflict, π.χ. username/email already taken
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            // π.χ. user not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

}
