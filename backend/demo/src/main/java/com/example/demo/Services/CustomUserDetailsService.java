package com.example.demo.Services;

import com.example.demo.Entities.AccountActivationToken;
import com.example.demo.Entities.User;
import com.example.demo.Repositories.AccountActivationTokenRepository;
import com.example.demo.Repositories.UserRepository;
import com.example.demo.Security.Role;
import com.example.demo.Services.email.EmailService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AccountActivationTokenRepository activationTokenRepository;
    private final EmailService emailService;

    public CustomUserDetailsService(UserRepository userRepository,
                                    BCryptPasswordEncoder passwordEncoder,
                                    AccountActivationTokenRepository activationTokenRepository,
                                    EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.activationTokenRepository = activationTokenRepository;
        this.emailService = emailService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
                .toList();

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),          // ✅ αν false, login αποτυγχάνει
                true,
                true,
                user.isAccountNonLocked(),
                authorities
        );
    }

    public User registerNewUser(User user) {
        // 1) encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 2) default role
        if (user.getRoles().isEmpty()) {
            user.addRole(Role.USER);
        }

        // 3) disable until activation
        user.setEnabled(false);

        // 4) save user
        User savedUser = userRepository.save(user);

        // 5) remove old tokens (optional αλλά καλό)
        activationTokenRepository.deleteByUserId(savedUser.getId());

        // 6) create token
        String token = UUID.randomUUID().toString();
        AccountActivationToken at = new AccountActivationToken();
        at.setToken(token);
        at.setUser(savedUser);
        at.setExpiresAt(LocalDateTime.now().plusHours(24));

        activationTokenRepository.save(at);

        // 7) send activation email
        emailService.sendActivationEmail(savedUser.getEmail(), savedUser.getUsername(), token);

        return savedUser;
    }
}
