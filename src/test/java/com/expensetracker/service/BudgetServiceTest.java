package com.expensetracker.service;

import com.expensetracker.dto.request.BudgetRequest;
import com.expensetracker.dto.response.BudgetResponse;
import com.expensetracker.entity.Budget;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.User;
import com.expensetracker.entity.enums.NotificationType;
import com.expensetracker.entity.enums.Role;
import com.expensetracker.entity.enums.TransactionType;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.DuplicateResourceException;
import com.expensetracker.mapper.BudgetMapper;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private BudgetMapper budgetMapper;

    @InjectMocks
    private BudgetService budgetService;

    private User user;
    private Category expenseCategory;
    private Category incomeCategory;
    private Budget budget;
    private BudgetRequest validRequest;

    @BeforeEach
    void setUp() {
        user = new User("user@example.com", "hash", "John", "Doe", Role.ROLE_USER);
        user.setId(1L);

        expenseCategory = new Category(user, "Dining", TransactionType.EXPENSE, "coffee", "#D35400", false);
        expenseCategory.setId(10L);

        incomeCategory = new Category(user, "Salary", TransactionType.INCOME, "briefcase", "#27AE60", false);
        incomeCategory.setId(20L);

        budget = new Budget(user, expenseCategory, 10, 2026, new BigDecimal("400.00"));
        budget.setId(100L);

        validRequest = new BudgetRequest(10L, 10, 2026, new BigDecimal("400.00"));
    }

    @Test
    @DisplayName("createBudget: Successfully creates category budget and calculates initial status")
    void testCreateBudgetSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findByIdAndUserIdOrSystemDefault(10L, 1L)).thenReturn(Optional.of(expenseCategory));
        when(budgetRepository.existsByUserIdAndCategoryIdAndBudgetMonthAndBudgetYear(1L, 10L, 10, 2026)).thenReturn(false);
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);
        when(transactionRepository.sumAmountByUserIdAndCategoryIdAndTypeAndDateBetween(
                eq(1L), eq(10L), eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("150.00"));

        BudgetResponse expectedResponse = new BudgetResponse(
                100L, 10L, "Dining", 10, 2026, new BigDecimal("400.00"),
                new BigDecimal("150.00"), new BigDecimal("250.00"), 37.5, "ON_TRACK", null
        );
        when(budgetMapper.toDto(eq(budget), eq(new BigDecimal("150.00")))).thenReturn(expectedResponse);

        BudgetResponse result = budgetService.createBudget(validRequest, 1L);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo("ON_TRACK");
        assertThat(result.utilizationPercentage()).isEqualTo(37.5);
        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    @DisplayName("createBudget: Throws BadRequestException when setting budget on INCOME category")
    void testCreateBudgetIncomeCategoryError() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findByIdAndUserIdOrSystemDefault(20L, 1L)).thenReturn(Optional.of(incomeCategory));

        BudgetRequest request = new BudgetRequest(20L, 10, 2026, new BigDecimal("500.00"));

        assertThatThrownBy(() -> budgetService.createBudget(request, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Budgets can only be set for EXPENSE categories");

        verify(budgetRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBudget: Throws DuplicateResourceException when budget already exists for period")
    void testCreateDuplicateBudget() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findByIdAndUserIdOrSystemDefault(10L, 1L)).thenReturn(Optional.of(expenseCategory));
        when(budgetRepository.existsByUserIdAndCategoryIdAndBudgetMonthAndBudgetYear(1L, 10L, 10, 2026)).thenReturn(true);

        assertThatThrownBy(() -> budgetService.createBudget(validRequest, 1L))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("An active budget already exists for category 'Dining'");
    }

    @Test
    @DisplayName("evaluateBudgetStatus: Triggers 80% warning notification when spending crosses 80%")
    void testEvaluateBudgetStatusWarningNotification() {
        when(transactionRepository.sumAmountByUserIdAndCategoryIdAndTypeAndDateBetween(
                eq(1L), eq(10L), eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("340.00")); // 340 / 400 = 85.0%

        budgetService.evaluateBudgetStatus(budget, user);

        verify(notificationService).createNotification(
                eq(user),
                eq(NotificationType.BUDGET_WARNING),
                eq("Budget Warning (80% Reached)"),
                contains("340.00 (85.0%)"),
                eq(100L)
        );
        assertThat(budget.isThresholdWarned()).isTrue();
    }

    @Test
    @DisplayName("evaluateBudgetStatus: Triggers exceeded notification when spending reaches 100%")
    void testEvaluateBudgetStatusExceededNotification() {
        when(transactionRepository.sumAmountByUserIdAndCategoryIdAndTypeAndDateBetween(
                eq(1L), eq(10L), eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("420.00")); // 420 / 400 = 105.0%

        budgetService.evaluateBudgetStatus(budget, user);

        verify(notificationService).createNotification(
                eq(user),
                eq(NotificationType.BUDGET_EXCEEDED),
                eq("Budget Exceeded Alert!"),
                contains("exceeded your Dining budget"),
                eq(100L)
        );
        assertThat(budget.isExceededNotified()).isTrue();
    }
}
