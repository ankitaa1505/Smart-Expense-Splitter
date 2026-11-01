package com.expensetracker.service;

import com.expensetracker.dto.request.RecurringTransactionRequest;
import com.expensetracker.dto.response.RecurringTransactionResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Notification;
import com.expensetracker.entity.RecurringTransaction;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.User;
import com.expensetracker.entity.enums.Frequency;
import com.expensetracker.entity.enums.NotificationType;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.mapper.RecurringTransactionMapper;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.NotificationRepository;
import com.expensetracker.repository.RecurringTransactionRepository;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RecurringTransactionService {

    private static final Logger log = LoggerFactory.getLogger(RecurringTransactionService.class);

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final BudgetService budgetService;
    private final RecurringTransactionMapper recurringTransactionMapper;

    public RecurringTransactionService(
            RecurringTransactionRepository recurringTransactionRepository,
            TransactionRepository transactionRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            NotificationRepository notificationRepository,
            BudgetService budgetService,
            RecurringTransactionMapper recurringTransactionMapper) {
        this.recurringTransactionRepository = recurringTransactionRepository;
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.budgetService = budgetService;
        this.recurringTransactionMapper = recurringTransactionMapper;
    }

    @Transactional
    public RecurringTransactionResponse createRecurringTransaction(RecurringTransactionRequest request, Long userId) {
        log.info("Creating recurring transaction '{}' for user ID: {}", request.title(), userId);

        if (request.endDate() != null && request.startDate().isAfter(request.endDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Category category = categoryRepository.findByIdAndUserIdOrSystemDefault(request.categoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.categoryId()));

        if (category.getType() != request.type()) {
            throw new BadRequestException(String.format(
                    "Category '%s' is of type %s and cannot be assigned to %s recurring transaction",
                    category.getName(), category.getType(), request.type()));
        }

        RecurringTransaction entity = recurringTransactionMapper.toEntity(request, user, category);
        RecurringTransaction saved = recurringTransactionRepository.save(entity);
        return recurringTransactionMapper.toDto(saved);
    }

    public List<RecurringTransactionResponse> getUserRecurringTransactions(Long userId) {
        return recurringTransactionRepository.findByUserIdOrderByNextRunDateAsc(userId).stream()
                .map(recurringTransactionMapper::toDto)
                .toList();
    }

    public RecurringTransactionResponse getRecurringTransactionById(Long id, Long userId) {
        RecurringTransaction entity = recurringTransactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("RecurringTransaction", "id", id));
        return recurringTransactionMapper.toDto(entity);
    }

    @Transactional
    public RecurringTransactionResponse toggleActive(Long id, Long userId) {
        RecurringTransaction entity = recurringTransactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("RecurringTransaction", "id", id));
        entity.setActive(!entity.isActive());
        RecurringTransaction updated = recurringTransactionRepository.save(entity);
        return recurringTransactionMapper.toDto(updated);
    }

    @Transactional
    public void deleteRecurringTransaction(Long id, Long userId) {
        RecurringTransaction entity = recurringTransactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("RecurringTransaction", "id", id));
        recurringTransactionRepository.delete(entity);
    }

    @Transactional
    @CacheEvict(value = "analytics:summary", allEntries = true)
    public int processDueRecurringTransactions() {
        LocalDate today = LocalDate.now();
        List<RecurringTransaction> dueTransactions = recurringTransactionRepository
                .findByIsActiveTrueAndNextRunDateLessThanEqual(today);

        log.info("Processing {} due recurring transactions on {}", dueTransactions.size(), today);
        int processedCount = 0;

        for (RecurringTransaction rec : dueTransactions) {
            try {
                // 1. Generate new Transaction
                Transaction tx = new Transaction(
                        rec.getUser(),
                        rec.getCategory(),
                        rec.getType(),
                        rec.getAmount(),
                        rec.getTitle(),
                        "Automatically generated from recurring schedule #" + rec.getId(),
                        rec.getNextRunDate(),
                        rec.getPaymentMethod(),
                        "Recurring transaction processed on " + today
                );
                transactionRepository.save(tx);

                // 2. Trigger budget check if expense
                if (rec.getType() == com.expensetracker.entity.enums.TransactionType.EXPENSE) {
                    budgetService.checkBudgetThresholds(rec.getUser().getId(), tx.getTransactionDate(), rec.getCategory().getId());
                }

                // 3. Compute next run date
                LocalDate nextDate = computeNextRunDate(rec.getNextRunDate(), rec.getFrequency());
                rec.setNextRunDate(nextDate);

                // 4. Check if past end date
                if (rec.getEndDate() != null && nextDate.isAfter(rec.getEndDate())) {
                    rec.setActive(false);
                }

                recurringTransactionRepository.save(rec);

                // 5. Create notification for user
                Notification notif = new Notification(
                        rec.getUser(),
                        NotificationType.SYSTEM,
                        "Recurring Transaction Executed",
                        String.format("Scheduled %s '%s' of %s was automatically recorded.",
                                rec.getType(), rec.getTitle(), rec.getAmount()),
                        tx.getId()
                );
                notificationRepository.save(notif);

                processedCount++;
            } catch (Exception ex) {
                log.error("Error processing recurring transaction ID: {}: {}", rec.getId(), ex.getMessage(), ex);
            }
        }

        return processedCount;
    }

    public LocalDate computeNextRunDate(LocalDate current, Frequency frequency) {
        return switch (frequency) {
            case DAILY -> current.plusDays(1);
            case WEEKLY -> current.plusWeeks(1);
            case MONTHLY -> current.plusMonths(1);
            case YEARLY -> current.plusYears(1);
        };
    }
}
