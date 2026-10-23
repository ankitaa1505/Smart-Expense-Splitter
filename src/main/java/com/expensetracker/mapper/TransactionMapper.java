package com.expensetracker.mapper;

import com.expensetracker.dto.request.TransactionRequest;
import com.expensetracker.dto.response.TransactionResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    private final CategoryMapper categoryMapper;

    public TransactionMapper(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public TransactionResponse toDto(Transaction entity) {
        if (entity == null) return null;
        return new TransactionResponse(
                entity.getId(),
                entity.getType(),
                entity.getAmount(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getTransactionDate(),
                entity.getPaymentMethod(),
                entity.getNotes(),
                categoryMapper.toDto(entity.getCategory()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public Transaction toEntity(TransactionRequest request, User user, Category category) {
        if (request == null) return null;
        return new Transaction(
                user,
                category,
                request.type(),
                request.amount(),
                request.title().trim(),
                request.description() != null ? request.description().trim() : null,
                request.transactionDate(),
                request.paymentMethod(),
                request.notes() != null ? request.notes().trim() : null
        );
    }

    public void updateEntityFromRequest(Transaction entity, TransactionRequest request, Category category) {
        entity.setCategory(category);
        entity.setType(request.type());
        entity.setAmount(request.amount());
        entity.setTitle(request.title().trim());
        entity.setDescription(request.description() != null ? request.description().trim() : null);
        entity.setTransactionDate(request.transactionDate());
        entity.setPaymentMethod(request.paymentMethod());
        entity.setNotes(request.notes() != null ? request.notes().trim() : null);
    }
}
