package com.example.demo.dto;

import com.example.demo.Entities.User;

public record RenterRequestDto(
        Long           id,
        String         username,
        String         email,
        String         status   // PENDING / APPROVED / REJECTED
) {
    public static RenterRequestDto fromEntity(User u) {
        return new RenterRequestDto(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getRenterRequestStatus().name()
        );
    }
}