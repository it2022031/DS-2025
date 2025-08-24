package com.example.demo.dto;

import com.example.demo.Entities.User;
import com.example.demo.Security.Role;

import com.example.demo.Entities.User;
import com.example.demo.Security.Role;

public record UserResponseDto(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        String role,
        String passportNumber,
        String afm,
        String renterRequestStatus   // νέο
) {
    public static UserResponseDto fromEntity(User u) {
        return new UserResponseDto(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getFirstName(),
                u.getLastName(),
                u.getRoles().stream().findFirst().map(Role::name).orElse(null),
                u.getPassportNumber(),
                u.getAfm(),
                u.getRenterRequestStatus() != null ? u.getRenterRequestStatus().name() : null
        );
    }
}
