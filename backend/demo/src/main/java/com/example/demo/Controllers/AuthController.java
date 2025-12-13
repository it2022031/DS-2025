package com.example.demo.Controllers;

import com.example.demo.Entities.User;
import com.example.demo.Repositories.AccountActivationTokenRepository;
import com.example.demo.Repositories.UserRepository;
import com.example.demo.Security.JwtUtil;
import com.example.demo.Security.Role;
import com.example.demo.Services.CustomUserDetailsService;
import com.example.demo.dto.UserResponseDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import com.example.demo.Services.email.EmailService;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final AccountActivationTokenRepository activationTokenRepository;
    public AuthController(CustomUserDetailsService userDetailsService,
                          JwtUtil jwtUtil,
                          AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          EmailService emailService,
                          AccountActivationTokenRepository activationTokenRepository) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.activationTokenRepository = activationTokenRepository;
    }

    // DTOs για Register/Login
    public record RegistrationRequest(
            @NotBlank String username,
            @NotBlank @Size(min = 6) String password,
            @Email @NotBlank String email,
            String firstName,
            String lastName,
            String passportNumber,
            String afm
    ) {}

    public record AuthRequest(String username, String password) {}

    public record AuthResponse(String token, UserResponseDto user) {}

    // Εγγραφή νέου χρήστη
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already taken"));
        }
        if (userRepository.existsByEmail(req.email())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already in use"));
        }

        User user = new User();
        user.setUsername(req.username());
        user.setPassword(req.password()); // θα κωδικοποιηθεί μέσα στο service
        user.setEmail(req.email());
        user.setFirstName(req.firstName());
        user.setLastName(req.lastName());
        user.setPassportNumber(req.passportNumber());
        user.setAfm(req.afm());
        user.addRole(Role.USER);

        User saved = userDetailsService.registerNewUser(user); // εδώ γίνεται enabled=false + token + email

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Registration successful. Please check your email to activate your account.",
                "user", UserResponseDto.fromEntity(saved)
        ));
    }


    // Login υπάρχοντος χρήστη
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password())
            );
        } catch (BadCredentialsException | DisabledException | LockedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials or account disabled/locked"));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication failed"));
        }

        UserDetails ud = userDetailsService.loadUserByUsername(req.username());
        String token = jwtUtil.generateToken(ud);

        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return ResponseEntity.ok(new AuthResponse(token, UserResponseDto.fromEntity(user)));
    }

    // Επιστροφή στοιχείων τρέχοντος χρήστη
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails ud)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthenticated"));
        }
        String username = ud.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return ResponseEntity.ok(UserResponseDto.fromEntity(user));
    }
    @GetMapping("/activate")
    public void activate(@RequestParam String token,
                         HttpServletResponse response) throws IOException {

        var at = activationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid activation token"));

        if (at.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            response.sendRedirect("/activation-expired.html");
            return; // 👈 απλό return; ΟΧΙ return κάτι
        }

        User user = at.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        activationTokenRepository.delete(at);

        response.sendRedirect("/account-activated.html");
    }

}
