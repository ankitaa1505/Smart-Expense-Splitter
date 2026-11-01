package com.expensetracker.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record BudgetRequest(
        Long categoryId, // Null represents an overall monthly budget

        @NotNull(message = "Budget month is required")
        @Min(value = 1, message = "Month must be between 1 and 12")
        @Max(value = 12, message = "Month must be between 1 and 12")
        Integer budgetMonth,

        @NotNull(message = "Budget year is required")
        @Min(value = 2000, message = "Year must be 2000 or later")
        @Max(value = 2100, message = "Year must be 2100 or earlier")
        Integer budgetYear,

        @NotNull(message = "Target budget amount is required")
        @Positive(message = "Target amount must be strictly greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Target amount can have at most 2 decimal places")
        BigDecimal targetAmount
) {}
