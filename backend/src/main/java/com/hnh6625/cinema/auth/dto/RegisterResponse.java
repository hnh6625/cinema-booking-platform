package com.hnh6625.cinema.auth.dto;

import com.hnh6625.cinema.auth.entity.Role;
import com.hnh6625.cinema.auth.entity.User;

import java.time.Instant;

public record RegisterResponse(
        Long id,
        String email,
        String phone,
        Role role,
        Instant createdAt
) {
    public static RegisterResponse from(User user) {
        return new RegisterResponse(
                user.getId(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getCreatedAt());
    }
}
