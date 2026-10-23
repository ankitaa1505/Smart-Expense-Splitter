package com.expensetracker.dto.request;

import com.expensetracker.entity.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Category name is required")
        @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters")
        String name,

        @NotNull(message = "Category type (INCOME or EXPENSE) is required")
        TransactionType type,

        @Size(max = 50, message = "Icon name cannot exceed 50 characters")
        String icon,

        @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Color code must be a valid 6-character hex string (e.g. #4A90E2)")
        String colorCode
) {}
