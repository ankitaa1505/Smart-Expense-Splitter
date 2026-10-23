package com.expensetracker.mapper;

import com.expensetracker.dto.request.CategoryRequest;
import com.expensetracker.dto.response.CategoryResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.User;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toDto(Category entity) {
        if (entity == null) return null;
        return new CategoryResponse(
                entity.getId(),
                entity.getName(),
                entity.getType(),
                entity.getIcon(),
                entity.getColorCode(),
                entity.isSystemDefault(),
                entity.getCreatedAt()
        );
    }

    public Category toEntity(CategoryRequest request, User user) {
        if (request == null) return null;
        return new Category(
                user,
                request.name().trim(),
                request.type(),
                request.icon() != null && !request.icon().isBlank() ? request.icon() : "default-icon",
                request.colorCode() != null && !request.colorCode().isBlank() ? request.colorCode() : "#4A90E2",
                false
        );
    }
}
