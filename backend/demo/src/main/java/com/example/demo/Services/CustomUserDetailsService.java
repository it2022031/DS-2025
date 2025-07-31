package com.example.demo.Services;

import com.example.demo.Entities.User;
import com.example.demo.Repositories.UserRepository;
import com.example.demo.Security.Role;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public CustomUserDetailsService(UserRepository userRepository,
                                    BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Χρησιμοποιείται από το Spring Security για authentication
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(), // ήδη hashed
                user.isEnabled(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                user.isAccountNonLocked(),
                Collections.singletonList(authority)
        );
    }

    // Registration helper: κωδικοποιεί το password πριν αποθήκευση
    public User registerNewUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }
        return userRepository.save(user);
    }
}
