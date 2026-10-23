package com.expensetracker.service;

import com.expensetracker.dto.request.CategoryRequest;
import com.expensetracker.dto.response.CategoryResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.User;
import com.expensetracker.entity.enums.TransactionType;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.DuplicateResourceException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.mapper.CategoryMapper;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository,
                           UserRepository userRepository,
                           CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.categoryMapper = categoryMapper;
    }

    public List<CategoryResponse> getCategoriesForUser(Long userId) {
        log.debug("Fetching all available categories for user ID: {}", userId);
        return categoryRepository.findAllAvailableForUser(userId).stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    public List<CategoryResponse> getCategoriesForUserAndType(Long userId, TransactionType type) {
        log.debug("Fetching categories for user ID: {} and type: {}", userId, type);
        return categoryRepository.findAllAvailableForUserAndType(userId, type).stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    public Category getCategoryEntityForUser(Long categoryId, Long userId) {
        return categoryRepository.findByIdAndUserIdOrSystemDefault(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
    }

    @Transactional
    public CategoryResponse createCustomCategory(CategoryRequest request, Long userId) {
        log.info("Creating custom category '{}' of type '{}' for user ID: {}", request.name(), request.type(), userId);

        // 1. Check if category already exists as a system default
        if (categoryRepository.existsByNameIgnoreCaseAndIsSystemDefaultTrueAndType(request.name().trim(), request.type())) {
            throw new DuplicateResourceException(
                    String.format("A default system category named '%s' already exists for type %s", request.name().trim(), request.type()));
        }

        // 2. Check if user already created a custom category with this name and type
        if (categoryRepository.existsByNameIgnoreCaseAndUserIdAndType(request.name().trim(), userId, request.type())) {
            throw new DuplicateResourceException(
                    String.format("You already have a category named '%s' for type %s", request.name().trim(), request.type()));
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Category category = categoryMapper.toEntity(request, user);
        Category savedCategory = categoryRepository.save(category);
        log.info("Successfully created custom category with ID: {}", savedCategory.getId());
        return categoryMapper.toDto(savedCategory);
    }

    @Transactional
    public void deleteCustomCategory(Long categoryId, Long userId) {
        log.info("Attempting to delete custom category ID: {} for user ID: {}", categoryId, userId);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        if (category.isSystemDefault()) {
            throw new BadRequestException("System default categories cannot be deleted");
        }

        if (category.getUser() == null || !category.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Category", "id", categoryId);
        }

        categoryRepository.delete(category);
        log.info("Successfully deleted custom category ID: {}", categoryId);
    }
}
