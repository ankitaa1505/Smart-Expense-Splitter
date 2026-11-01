package com.expensetracker.service;

import com.expensetracker.entity.Category;
import com.expensetracker.entity.Notification;
import com.expensetracker.entity.RecurringTransaction;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.User;
import com.expensetracker.entity.enums.Frequency;
import com.expensetracker.entity.enums.PaymentMethod;
import com.expensetracker.entity.enums.Role;
import com.expensetracker.entity.enums.TransactionType;
import com.expensetracker.mapper.RecurringTransactionMapper;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.NotificationRepository;
import com.expensetracker.repository.RecurringTransactionRepository;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecurringTransactionServiceTest {

    @Mock
    private RecurringTransactionRepository recurringTransactionRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private BudgetService budgetService;

    @Mock
    private RecurringTransactionMapper recurringTransactionMapper;

    @InjectMocks
    private RecurringTransactionService recurringTransactionService;

    @Test
    @DisplayName("computeNextRunDate: Computes correct next execution date for all frequencies")
    void testComputeNextRunDate() {
        LocalDate baseDate = LocalDate.of(2026, 1, 15);

        assertThat(recurringTransactionService.computeNextRunDate(baseDate, Frequency.DAILY))
                .isEqualTo(LocalDate.of(2026, 1, 16));
        assertThat(recurringTransactionService.computeNextRunDate(baseDate, Frequency.WEEKLY))
                .isEqualTo(LocalDate.of(2026, 1, 22));
        assertThat(recurringTransactionService.computeNextRunDate(baseDate, Frequency.MONTHLY))
                .isEqualTo(LocalDate.of(2026, 2, 15));
        assertThat(recurringTransactionService.computeNextRunDate(baseDate, Frequency.YEARLY))
                .isEqualTo(LocalDate.of(2027, 1, 15));
    }

    @Test
    @DisplayName("processDueRecurringTransactions: Processes due schedule, creates transaction and notification")
    void testProcessDueRecurringTransactions() {
        User user = new User("user@example.com", "hash", "Bob", "Builder", Role.ROLE_USER);
        user.setId(2L);

        Category category = new Category(user, "Utilities", TransactionType.EXPENSE, "zap", "#F39C12", true);
        category.setId(3L);

        LocalDate today = LocalDate.now();
        RecurringTransaction recurring = new RecurringTransaction(
                user, category, TransactionType.EXPENSE, new BigDecimal("120.00"),
                "Internet Bill", Frequency.MONTHLY, today.minusMonths(1), null, today,
                PaymentMethod.BANK_TRANSFER, true
        );
        recurring.setId(50L);

        when(recurringTransactionRepository.findByIsActiveTrueAndNextRunDateLessThanEqual(today))
                .thenReturn(List.of(recurring));

        int processed = recurringTransactionService.processDueRecurringTransactions();

        assertThat(processed).isEqualTo(1);
        verify(transactionRepository).save(any(Transaction.class));
        verify(budgetService).checkBudgetThresholds(eq(2L), eq(today), eq(3L));
        verify(notificationRepository).save(any(Notification.class));
        verify(recurringTransactionRepository).save(recurring);
        assertThat(recurring.getNextRunDate()).isEqualTo(today.plusMonths(1));
    }
}
