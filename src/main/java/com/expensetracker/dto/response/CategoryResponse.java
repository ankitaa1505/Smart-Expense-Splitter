package com.expensetracker.dto.response;

import com.expensetracker.entity.enums.TransactionType;

import java.time.Instant;

public record CategoryResponse(
        Long id,
        String name,
        TransactionType type,
        String icon,
        String colorCode,
        boolean isSystemDefault,
        Instant createdAt
) {}
