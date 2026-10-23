package com.expensetracker.service;

import com.expensetracker.dto.common.PageResponse;
import com.expensetracker.dto.request.TransactionFilterRequest;
import com.expensetracker.dto.request.TransactionRequest;
import com.expensetracker.dto.response.TransactionResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.User;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.mapper.TransactionMapper;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.repository.specification.TransactionSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional(readOnly = true)
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "transactionDate", "amount", "title", "createdAt", "updatedAt"
    );

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionMapper transactionMapper;

    public TransactionService(TransactionRepository transactionRepository,
                              CategoryRepository categoryRepository,
                              UserRepository userRepository,
                              TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.transactionMapper = transactionMapper;
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request, Long userId) {
        log.info("Creating {} transaction for user ID: {}, amount: {}", request.type(), userId, request.amount());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Category category = categoryRepository.findByIdAndUserIdOrSystemDefault(request.categoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.categoryId()));

        // Enforce type consistency: Category type must match transaction type
        if (category.getType() != request.type()) {
            throw new BadRequestException(String.format(
                    "Category '%s' is of type %s and cannot be assigned to an %s transaction",
                    category.getName(), category.getType(), request.type()));
        }

        Transaction transaction = transactionMapper.toEntity(request, user, category);
        Transaction saved = transactionRepository.save(transaction);
        log.info("Successfully created transaction ID: {} for user ID: {}", saved.getId(), userId);
        return transactionMapper.toDto(saved);
    }

    public TransactionResponse getTransactionById(Long id, Long userId) {
        log.debug("Fetching transaction ID: {} for user ID: {}", id, userId);
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
        return transactionMapper.toDto(transaction);
    }

    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionRequest request, Long userId) {
        log.info("Updating transaction ID: {} for user ID: {}", id, userId);

        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));

        Category category = categoryRepository.findByIdAndUserIdOrSystemDefault(request.categoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.categoryId()));

        if (category.getType() != request.type()) {
            throw new BadRequestException(String.format(
                    "Category '%s' is of type %s and cannot be assigned to an %s transaction",
                    category.getName(), category.getType(), request.type()));
        }

        transactionMapper.updateEntityFromRequest(transaction, request, category);
        Transaction updated = transactionRepository.save(transaction);
        log.info("Successfully updated transaction ID: {}", updated.getId());
        return transactionMapper.toDto(updated);
    }

    @Transactional
    public void deleteTransaction(Long id, Long userId) {
        log.info("Deleting transaction ID: {} for user ID: {}", id, userId);
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
        transactionRepository.delete(transaction);
        log.info("Successfully deleted transaction ID: {}", id);
    }

    public PageResponse<TransactionResponse> getTransactions(TransactionFilterRequest filter, Long userId) {
        log.debug("Querying transactions for user ID: {} with filters: {}", userId, filter);

        if (filter.startDate() != null && filter.endDate() != null && filter.startDate().isAfter(filter.endDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        String sortBy = ALLOWED_SORT_PROPERTIES.contains(filter.sortBy()) ? filter.sortBy() : "transactionDate";
        Sort.Direction direction = "ASC".equalsIgnoreCase(filter.direction()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);

        Pageable pageable = PageRequest.of(filter.page(), filter.size(), sort);
        Specification<Transaction> spec = TransactionSpecification.withFilters(filter, userId);

        Page<TransactionResponse> page = transactionRepository.findAll(spec, pageable)
                .map(transactionMapper::toDto);

        return PageResponse.of(page);
    }
}
