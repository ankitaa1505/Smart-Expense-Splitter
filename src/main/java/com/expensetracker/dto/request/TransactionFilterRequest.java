package com.expensetracker.dto.request;

import com.expensetracker.entity.enums.PaymentMethod;
import com.expensetracker.entity.enums.TransactionType;

import java.time.LocalDate;

public record TransactionFilterRequest(
        TransactionType type,
        Long categoryId,
        LocalDate startDate,
        LocalDate endDate,
        PaymentMethod paymentMethod,
        String searchTerm,
        Integer page,
        Integer size,
        String sortBy,
        String direction
) {
    public TransactionFilterRequest {
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 20;
        if (size > 100) size = 100; // Enforce maximum page size limit
        if (sortBy == null || sortBy.isBlank()) sortBy = "transactionDate";
        if (direction == null || direction.isBlank()) direction = "DESC";
    }
}
