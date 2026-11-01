package com.expensetracker.dto.response;

import java.math.BigDecimal;

public record CategoryBreakdownResponse(
        Long categoryId,
        String categoryName,
        String colorCode,
        String icon,
        BigDecimal totalAmount,
        long transactionCount,
        double percentageOfTotal
) {}
