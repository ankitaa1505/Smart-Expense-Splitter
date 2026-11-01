package com.expensetracker.dto.request;

import com.expensetracker.entity.enums.Frequency;
import com.expensetracker.entity.enums.PaymentMethod;
import com.expensetracker.entity.enums.TransactionType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecurringTransactionRequest(
        @NotNull(message = "Category ID is required")
        Long categoryId,

        @NotNull(message = "Transaction type is required")
        TransactionType type,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be strictly greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Amount can have at most 2 decimal places")
        BigDecimal amount,

        @NotBlank(message = "Title is required")
        @Size(min = 2, max = 120, message = "Title must be between 2 and 120 characters")
        String title,

        @NotNull(message = "Frequency is required (DAILY, WEEKLY, MONTHLY, YEARLY)")
        Frequency frequency,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        LocalDate endDate,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod
) {}
