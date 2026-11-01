package com.expensetracker.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record BudgetResponse(
        Long id,
        Long categoryId,
        String categoryName, // "Overall Monthly Budget" if categoryId is null
        int budgetMonth,
        int budgetYear,
        BigDecimal targetAmount,
        BigDecimal actualSpent,
        BigDecimal remainingAmount,
        double utilizationPercentage,
        String status, // "ON_TRACK", "WARNING", "EXCEEDED"
        Instant createdAt
) {}
