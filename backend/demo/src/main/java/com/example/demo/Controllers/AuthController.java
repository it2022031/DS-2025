package com.example.demo.Controllers;

import com.example.demo.Entities.User;
import com.example.demo.Repositories.AccountActivationTokenRepository;
import com.example.demo.Repositories.UserRepository;
import com.example.demo.Security.JwtUtil;
import com.example.demo.Security.Role;
import com.example.demo.Services.CustomUserDetailsService;
import com.example.demo.Services.email.EmailService;
import com.example.demo.dto.UserResponseDto;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
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

    // ✅ URL ΤΟΥ FRONTEND (ΟΧΙ backend)
    @Value("${APP_FRONTEND_URL:http://localhost:8081}")
    private String frontendUrl;

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

    // DTOs
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

    // Register
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
        user.setPassword(req.password());
        user.setEmail(req.email());
        user.setFirstName(req.firstName());
        user.setLastName(req.lastName());
        user.setPassportNumber(req.passportNumber());
        user.setAfm(req.afm());
        user.addRole(Role.USER);

        User saved = userDetailsService.registerNewUser(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Registration successful. Please check your email to activate your account.",
                "user", UserResponseDto.fromEntity(saved)
        ));
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest req) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password())
            );
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials or account disabled"));
        }

        UserDetails ud = userDetailsService.loadUserByUsername(req.username());
        String token = jwtUtil.generateToken(ud);

        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return ResponseEntity.ok(new AuthResponse(token, UserResponseDto.fromEntity(user)));
    }

    // Me
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails ud)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findByUsername(ud.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return ResponseEntity.ok(UserResponseDto.fromEntity(user));
    }

    // ✅ ACTIVATE → REDIRECT ΣΤΟ VUE
    @GetMapping("/activate")
    public void activate(@RequestParam String token,
                         HttpServletResponse response) throws IOException {

        String front = normalize(frontendUrl); // π.χ. http://localhost:8081

        var opt = activationTokenRepository.findByToken(token);
        if (opt.isEmpty()) {
            response.sendRedirect(front + "/#/login?activated=false&reason=invalid");
            return;
        }

        var at = opt.get();

        if (at.getExpiresAt().isBefore(LocalDateTime.now())) {
            response.sendRedirect(front + "/#/login?activated=false&reason=expired");
            return;
        }

        User user = at.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        activationTokenRepository.delete(at);

        response.sendRedirect(front + "/#/login?activated=true");
    }

    private String normalize(String url) {
        if (url == null || url.isBlank()) return "http://localhost:8081";
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
