package com.expensetracker.service;

import com.expensetracker.dto.request.BudgetRequest;
import com.expensetracker.dto.response.BudgetResponse;
import com.expensetracker.entity.Budget;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.User;
import com.expensetracker.entity.enums.NotificationType;
import com.expensetracker.entity.enums.TransactionType;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.DuplicateResourceException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.mapper.BudgetMapper;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BudgetService {

    private static final Logger log = LoggerFactory.getLogger(BudgetService.class);

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final BudgetMapper budgetMapper;

    public BudgetService(BudgetRepository budgetRepository,
                         CategoryRepository categoryRepository,
                         TransactionRepository transactionRepository,
                         UserRepository userRepository,
                         NotificationService notificationService,
                         BudgetMapper budgetMapper) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.budgetMapper = budgetMapper;
    }

    @Transactional
    @CacheEvict(value = "analytics:summary", allEntries = true)
    public BudgetResponse createBudget(BudgetRequest request, Long userId) {
        log.info("Creating budget for user ID: {}, month: {}/{}, categoryId: {}",
                userId, request.budgetMonth(), request.budgetYear(), request.categoryId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findByIdAndUserIdOrSystemDefault(request.categoryId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.categoryId()));

            if (category.getType() != TransactionType.EXPENSE) {
                throw new BadRequestException("Budgets can only be set for EXPENSE categories");
            }

            if (budgetRepository.existsByUserIdAndCategoryIdAndBudgetMonthAndBudgetYear(
                    userId, request.categoryId(), request.budgetMonth(), request.budgetYear())) {
                throw new DuplicateResourceException(String.format(
                        "An active budget already exists for category '%s' in %02d/%d",
                        category.getName(), request.budgetMonth(), request.budgetYear()));
            }
        } else {
            if (budgetRepository.existsByUserIdAndCategoryIsNullAndBudgetMonthAndBudgetYear(
                    userId, request.budgetMonth(), request.budgetYear())) {
                throw new DuplicateResourceException(String.format(
                        "An overall monthly budget already exists for %02d/%d",
                        request.budgetMonth(), request.budgetYear()));
            }
        }

        Budget budget = new Budget(user, category, request.budgetMonth(), request.budgetYear(), request.targetAmount());
        Budget savedBudget = budgetRepository.save(budget);

        // Check spending and trigger immediate threshold warnings if already exceeded
        return evaluateBudgetStatus(savedBudget, user);
    }

    public List<BudgetResponse> getBudgetsForMonth(int month, int year, Long userId) {
        log.debug("Fetching budgets for user ID: {} for period {}/{}", userId, month, year);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<Budget> budgets = budgetRepository.findByUserIdAndBudgetMonthAndBudgetYear(userId, month, year);
        return budgets.stream()
                .map(b -> evaluateBudgetStatus(b, user))
                .toList();
    }

    public BudgetResponse getBudgetById(Long budgetId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", budgetId));
        return evaluateBudgetStatus(budget, user);
    }

    @Transactional
    @CacheEvict(value = "analytics:summary", allEntries = true)
    public BudgetResponse updateBudget(Long budgetId, BudgetRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", budgetId));

        budget.setTargetAmount(request.targetAmount());
        budget.setThresholdWarned(false);
        budget.setExceededNotified(false);
        Budget updated = budgetRepository.save(budget);

        return evaluateBudgetStatus(updated, user);
    }

    @Transactional
    @CacheEvict(value = "analytics:summary", allEntries = true)
    public void deleteBudget(Long budgetId, Long userId) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", budgetId));
        budgetRepository.delete(budget);
    }

    @Transactional
    public void checkBudgetThresholds(Long userId, LocalDate transactionDate, Long categoryId) {
        int month = transactionDate.getMonthValue();
        int year = transactionDate.getYear();

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        // 1. Check category budget if applicable
        if (categoryId != null) {
            budgetRepository.findByUserIdAndCategoryIdAndBudgetMonthAndBudgetYear(userId, categoryId, month, year)
                    .ifPresent(b -> evaluateBudgetStatus(b, user));
        }

        // 2. Check overall monthly budget
        budgetRepository.findByUserIdAndCategoryIsNullAndBudgetMonthAndBudgetYear(userId, month, year)
                .ifPresent(b -> evaluateBudgetStatus(b, user));
    }

    @Transactional
    public BudgetResponse evaluateBudgetStatus(Budget budget, User user) {
        LocalDate startDate = LocalDate.of(budget.getBudgetYear(), budget.getBudgetMonth(), 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        BigDecimal actualSpent;
        String categoryName;
        if (budget.getCategory() != null) {
            actualSpent = transactionRepository.sumAmountByUserIdAndCategoryIdAndTypeAndDateBetween(
                    user.getId(), budget.getCategory().getId(), TransactionType.EXPENSE, startDate, endDate);
            categoryName = budget.getCategory().getName();
        } else {
            actualSpent = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                    user.getId(), TransactionType.EXPENSE, startDate, endDate);
            categoryName = "Overall Monthly Budget";
        }

        if (actualSpent == null) actualSpent = BigDecimal.ZERO;

        double utilizationPercentage = 0.0;
        if (budget.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            utilizationPercentage = actualSpent.divide(budget.getTargetAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        // 80% threshold warning alert
        if (utilizationPercentage >= 80.0 && utilizationPercentage < 100.0 && !budget.isThresholdWarned()) {
            notificationService.createNotification(
                    user,
                    NotificationType.BUDGET_WARNING,
                    "Budget Warning (80% Reached)",
                    String.format("You have spent %s (%.1f%%) of your %s target of %s for %02d/%d",
                            actualSpent, utilizationPercentage, categoryName, budget.getTargetAmount(),
                            budget.getBudgetMonth(), budget.getBudgetYear()),
                    budget.getId()
            );
            budget.setThresholdWarned(true);
            budgetRepository.save(budget);
        }

        // 100% exceeded alert
        if (utilizationPercentage >= 100.0 && !budget.isExceededNotified()) {
            notificationService.createNotification(
                    user,
                    NotificationType.BUDGET_EXCEEDED,
                    "Budget Exceeded Alert!",
                    String.format("Alert: You have exceeded your %s budget of %s! Total spent: %s (%.1f%%)",
                            categoryName, budget.getTargetAmount(), actualSpent, utilizationPercentage),
                    budget.getId()
            );
            budget.setExceededNotified(true);
            budgetRepository.save(budget);
        }

        return budgetMapper.toDto(budget, actualSpent);
    }
}
