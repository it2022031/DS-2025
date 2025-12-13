package com.example.demo.dto;

import com.example.demo.Entities.User;
import com.example.demo.Security.Role;

import java.util.List;

import static org.aspectj.weaver.tools.cache.SimpleCacheFactory.enabled;

public record UserResponseDto(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        List<String> roles,
        String passportNumber,
        String afm,
        String renterRequestStatus,
        boolean enabled
) {
    public static UserResponseDto fromEntity(User u) {
        List<String> roleNames = u.getRoles().stream()
                .map(Role::name) // μετατρέπουμε τα enums σε String
                .toList();

        // Τιμές πεδίων από entity
        return new UserResponseDto(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getFirstName(),
                u.getLastName(),
                roleNames,
                u.getPassportNumber(),
                u.getAfm(),
                u.getRenterRequestStatus() != null ? u.getRenterRequestStatus().name() : null,
                u.isEnabled()
        );
    }
}
