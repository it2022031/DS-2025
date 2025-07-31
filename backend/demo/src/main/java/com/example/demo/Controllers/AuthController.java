package com.example.demo.Controllers;

import com.example.demo.Entities.User;
import com.example.demo.Repositories.UserRepository;
import com.example.demo.Security.JwtUtil;
import com.example.demo.Security.Role;
import com.example.demo.Services.CustomUserDetailsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.example.demo.Entities.User;
import com.example.demo.Repositories.UserRepository;
import com.example.demo.Security.JwtUtil;
import com.example.demo.Security.Role;
import com.example.demo.Services.CustomUserDetailsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin // ή @CrossOrigin(origins = "http://localhost:8081") για περιορισμό
public class AuthController {

    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    public AuthController(CustomUserDetailsService userDetailsService,
                          JwtUtil jwtUtil,
                          AuthenticationManager authenticationManager,
                          UserRepository userRepository) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    // DTOs
    public record RegistrationRequest(
            String username,
            String password,
            String email,
            String firstName,
            String lastName,
            String passportNumber,
            String afm
    ) {}

    public record AuthRequest(String username, String password) {}

    public record AuthResponse(String token) {}

    public record UserResponse(Long id, String username, String email,
                               String firstName, String lastName, String role) {
        public static UserResponse fromEntity(User u) {
            return new UserResponse(
                    u.getId(),
                    u.getUsername(),
                    u.getEmail(),
                    u.getFirstName(),
                    u.getLastName(),
                    u.getRole() != null ? u.getRole().name() : null
            );
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            return ResponseEntity.badRequest().body("Username already taken");
        }
        if (userRepository.existsByEmail(req.email())) {
            return ResponseEntity.badRequest().body("Email already in use");
        }

        User user = new User();
        user.setUsername(req.username());
        user.setPassword(req.password()); // θα κωδικοποιηθεί στο service
        user.setEmail(req.email());
        user.setFirstName(req.firstName());
        user.setLastName(req.lastName());
        user.setPassportNumber(req.passportNumber());
        user.setAfm(req.afm());
        user.setRole(Role.USER);

        User saved = userDetailsService.registerNewUser(user);
        return ResponseEntity.ok(UserResponse.fromEntity(saved));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password())
            );
        } catch (BadCredentialsException | DisabledException | LockedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials or account disabled/locked");
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
        }

        UserDetails ud = userDetailsService.loadUserByUsername(req.username());
        String token = jwtUtil.generateToken(ud);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    // Προαιρετικό: να πάρει ο χρήστης το προφίλ του
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails ud)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthenticated");
        }
        String username = ud.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }
}
