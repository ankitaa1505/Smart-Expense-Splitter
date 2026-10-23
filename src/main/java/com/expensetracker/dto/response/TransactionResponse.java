package com.expensetracker.dto.response;

import com.expensetracker.entity.enums.PaymentMethod;
import com.expensetracker.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        TransactionType type,
        BigDecimal amount,
        String title,
        String description,
        LocalDate transactionDate,
        PaymentMethod paymentMethod,
        String notes,
        CategoryResponse category,
        Instant createdAt,
        Instant updatedAt
) {}
