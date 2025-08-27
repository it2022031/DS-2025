package com.example.demo.dto;

import com.example.demo.Entities.User;

public record RenterRequestDto(
        // Όνομα πεδίων
        Long           id,
        String         username,
        String         email,
        String         status   // PENDING / APPROVED / REJECTED
) {
    public static RenterRequestDto fromEntity(User u) {

        // Τιμές πεδίων από entity
        return new RenterRequestDto(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getRenterRequestStatus().name()
        );
    }
}