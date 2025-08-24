package com.example.demo.dto;

import com.example.demo.Entities.User;
import com.example.demo.Security.Role;

import java.util.List;

public record UserResponseDto(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        List<String> roles,         // λίστα με όλους τους ρόλους
        String passportNumber,
        String afm,
        String renterRequestStatus
) {
    public static UserResponseDto fromEntity(User u) {
        List<String> roleNames = u.getRoles().stream()
                .map(Role::name)        // μετατρέπουμε τα enums σε String
                .toList();

        return new UserResponseDto(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getFirstName(),
                u.getLastName(),
                roleNames,
                u.getPassportNumber(),
                u.getAfm(),
                u.getRenterRequestStatus() != null ? u.getRenterRequestStatus().name() : null
        );
    }
}
