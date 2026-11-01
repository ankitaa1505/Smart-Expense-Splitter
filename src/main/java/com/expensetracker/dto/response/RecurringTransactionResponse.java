package com.expensetracker.dto.response;

import com.expensetracker.entity.enums.Frequency;
import com.expensetracker.entity.enums.PaymentMethod;
import com.expensetracker.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record RecurringTransactionResponse(
        Long id,
        Long categoryId,
        String categoryName,
        TransactionType type,
        BigDecimal amount,
        String title,
        Frequency frequency,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate nextRunDate,
        PaymentMethod paymentMethod,
        boolean isActive,
        Instant createdAt
) {}
