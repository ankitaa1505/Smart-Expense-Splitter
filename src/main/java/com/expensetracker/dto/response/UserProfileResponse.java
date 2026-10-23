package com.expensetracker.dto.response;

import com.expensetracker.entity.enums.Role;

import java.time.Instant;

public record UserProfileResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        Role role,
        Instant createdAt
) {}
