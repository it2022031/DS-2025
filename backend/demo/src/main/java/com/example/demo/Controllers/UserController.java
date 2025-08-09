package com.example.demo.Controllers;

import com.example.demo.Entities.ApprovalStatus;
import com.example.demo.Entities.Property;
import com.example.demo.Entities.Rental;
import com.example.demo.Entities.User;
import com.example.demo.Repositories.UserRepository;
import com.example.demo.Security.Role;
import com.example.demo.Services.UserService;
import com.example.demo.dto.RenterRequestDto;
import com.example.demo.dto.UserResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    public UserController(UserRepository userRepository,
                          UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    // 1. Το προφίλ του τρέχοντα χρήστη
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthenticated"));
        }
        String username = extractUsername(authentication);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(UserResponseDto.fromEntity(user));
    }

    // 2. Όλοι οι χρήστες — μόνο admin
    @GetMapping
    public ResponseEntity<?> allUsers(Authentication authentication) {
        if (authentication == null || !isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only admins can list all users"));
        }
        List<UserResponseDto> dtos = userRepository.findAll().stream()
                .map(UserResponseDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // 3. Get properties for user
    @GetMapping("/{id}/properties")
    public ResponseEntity<?> getPropertiesForUserId(@PathVariable Long id, Authentication authentication) {
        requireAuthenticated(authentication);
        User caller = getCaller(authentication);
        if (!isAdmin(authentication) && !caller.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Forbidden"));
        }
        List<Property> props = userService.getPropertiesForUserId(id);
        return ResponseEntity.ok(props);
    }

    // 4. Get rentals for user
    @GetMapping("/{id}/rentals")
    public ResponseEntity<?> getRentalsForUserId(@PathVariable Long id, Authentication authentication) {
        requireAuthenticated(authentication);
        User caller = getCaller(authentication);
        if (!isAdmin(authentication) && !caller.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Forbidden"));
        }
        List<Rental> rentals = userService.getRentalsForUserId(id);
        return ResponseEntity.ok(rentals);
    }

    // 5. Partial update του ιδίου χρήστη
    @PatchMapping("/me")
    public ResponseEntity<?> patchMe(Authentication authentication,
                                     @RequestBody Map<String, Object> updates) {
        requireAuthenticated(authentication);
        String username = ((UserDetails)authentication.getPrincipal()).getUsername();
        User me = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        try {
            User updated = userService.updateUserPartial(me.getId(), updates);
            return ResponseEntity.ok(UserResponseDto.fromEntity(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 6. Partial update άλλου χρήστη (admin only)
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> patchUserById(@PathVariable Long id,
                                           @RequestBody Map<String, Object> updates) {
        try {
            User updated = userService.updateUserPartial(id, updates);
            return ResponseEntity.ok(UserResponseDto.fromEntity(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // 7. Ο απλός user υποβάλλει αίτημα για να γίνει RENTER
    @PostMapping("/become-renter")
    public ResponseEntity<?> requestRenter(Authentication authentication) {
        requireAuthenticated(authentication);
        User u = getCaller(authentication);
        u.setRenterRequestStatus(ApprovalStatus.PENDING);
        userRepository.save(u);
        return ResponseEntity.ok(Map.of("message", "Renter request submitted"));
    }

    // 8. Ο admin εγκρίνει/απορρίπτει το αίτημα
    @PostMapping("/{id}/approve-renter")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveRenter(@PathVariable Long id) {
        User updated = userService.processRenterRequest(id, true);
        return ResponseEntity.ok(Map.of(
                "id",          updated.getId(),
                "newStatus",   updated.getRenterRequestStatus().name(),
                "roles",       updated.getRoles().stream().map(Role::name).collect(Collectors.toList())
        ));
    }

    @PostMapping("/{id}/reject-renter")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectRenter(@PathVariable Long id) {
        User updated = userService.processRenterRequest(id, false);
        return ResponseEntity.ok(Map.of(
                "id",        updated.getId(),
                "newStatus", updated.getRenterRequestStatus().name()
        ));
    }

    // 9. Get all outstanding renter requests (admin only)
    @GetMapping("/renter-requests")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RenterRequestDto>> listRenterRequests() {
        var dtos = userService.findAllRenterRequests().stream()
                .map(RenterRequestDto::fromEntity)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    // βοηθητικά
    private void requireAuthenticated(Authentication auth) {
        if (auth == null) throw new RuntimeException("Unauthenticated");
    }
    private String extractUsername(Authentication auth) {
        Object p = auth.getPrincipal();
        return p instanceof UserDetails ud ? ud.getUsername() : p.toString();
    }
    private User getCaller(Authentication auth) {
        String uname = extractUsername(auth);
        return userRepository.findByUsername(uname)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @GetMapping("/whoami")
    public ResponseEntity<?> whoami(Authentication auth) {
        return ResponseEntity.ok(
                auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList())
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUserAsAdmin(@PathVariable Long id, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // ποιος καλεί (για τον έλεγχο "μην διαγράψεις τον εαυτό σου")
        String callerUsername = extractUsername(authentication);
        Long callerId = userRepository.findByUsername(callerUsername)
                .orElseThrow(() -> new RuntimeException("Caller not found"))
                .getId();

        try {
            userService.adminDeleteUser(id, callerId);
            return ResponseEntity.noContent().build(); // 204
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/upload-photo")
    public ResponseEntity<?> uploadPhoto(@PathVariable Long id,
                                         @RequestParam("file") MultipartFile file,
                                         Authentication authentication) throws IOException {
        if (authentication == null) return ResponseEntity.status(401).body(Map.of("error","Unauthorized"));

        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        User caller = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isOwner = caller.getId().equals(id);
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(403).body(Map.of("error","Forbidden"));
        }

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error","Empty file"));
        }

        int updated = userRepository.updateProfilePicture(
                id, file.getBytes(), file.getContentType(), file.getOriginalFilename());

        if (updated == 0) return ResponseEntity.status(404).body(Map.of("error","User not found"));
        return ResponseEntity.ok(Map.of("message","Photo uploaded successfully"));
    }


    @GetMapping("/{id}/photo")
    public ResponseEntity<?> getPhoto(@PathVariable Long id, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        User caller = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isOwner = caller.getId().equals(id);
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN")); // <-- έλεγχος από authorities

        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        byte[] img = targetUser.getProfilePicture();
        if (img == null || img.length == 0) {
            return ResponseEntity.status(404).body(Map.of("error", "No photo found"));
        }

        return ResponseEntity.ok()
                .header("Content-Type", "image/jpeg") // ή χρησιμοποίησε αποθηκευμένο contentType
                .body(img);
    }
}
