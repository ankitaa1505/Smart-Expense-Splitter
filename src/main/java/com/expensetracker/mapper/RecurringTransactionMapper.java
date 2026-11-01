package com.expensetracker.mapper;

import com.expensetracker.dto.request.RecurringTransactionRequest;
import com.expensetracker.dto.response.RecurringTransactionResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.RecurringTransaction;
import com.expensetracker.entity.User;
import org.springframework.stereotype.Component;

@Component
public class RecurringTransactionMapper {

    public RecurringTransactionResponse toDto(RecurringTransaction entity) {
        if (entity == null) return null;
        return new RecurringTransactionResponse(
                entity.getId(),
                entity.getCategory().getId(),
                entity.getCategory().getName(),
                entity.getType(),
                entity.getAmount(),
                entity.getTitle(),
                entity.getFrequency(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getNextRunDate(),
                entity.getPaymentMethod(),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }

    public RecurringTransaction toEntity(RecurringTransactionRequest request, User user, Category category) {
        if (request == null) return null;
        return new RecurringTransaction(
                user,
                category,
                request.type(),
                request.amount(),
                request.title().trim(),
                request.frequency(),
                request.startDate(),
                request.endDate(),
                request.startDate(), // initially, nextRunDate is startDate
                request.paymentMethod(),
                true
        );
    }
}
