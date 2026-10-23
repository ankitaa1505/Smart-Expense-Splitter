package com.expensetracker.repository;

import com.expensetracker.entity.*;
import com.expensetracker.entity.enums.*;
import com.expensetracker.repository.projection.CategorySpendingProjection;
import com.expensetracker.repository.projection.MonthlySpendingProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testUser = new User("tester@example.com", "encodedPassword", "Alex", "Smith", Role.ROLE_USER);
        userRepository.save(testUser);

        testCategory = new Category(testUser, "Dining Out", TransactionType.EXPENSE, "coffee", "#E67E22", false);
        categoryRepository.save(testCategory);
    }

    @Test
    @DisplayName("UserRepository: Should find user by email and verify uniqueness check")
    void testUserRepository() {
        Optional<User> found = userRepository.findByEmail("tester@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Alex");

        boolean exists = userRepository.existsByEmail("tester@example.com");
        assertThat(exists).isTrue();

        boolean notExists = userRepository.existsByEmail("unknown@example.com");
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("CategoryRepository: Should fetch custom and system categories for user")
    void testCategoryRepository() {
        Category systemCategory = new Category(null, "Salary", TransactionType.INCOME, "briefcase", "#27AE60", true);
        categoryRepository.save(systemCategory);

        List<Category> available = categoryRepository.findAllAvailableForUser(testUser.getId());
        assertThat(available).hasSize(2);
        assertThat(available).extracting(Category::getName).containsExactlyInAnyOrder("Dining Out", "Salary");
    }

    @Test
    @DisplayName("TransactionRepository: Aggregations and category breakdown calculations")
    void testTransactionRepositoryAggregations() {
        LocalDate date = LocalDate.of(2026, 10, 15);

        Transaction tx1 = new Transaction(testUser, testCategory, TransactionType.EXPENSE,
                new BigDecimal("45.50"), "Lunch with team", null, date, PaymentMethod.CREDIT_CARD, null);
        Transaction tx2 = new Transaction(testUser, testCategory, TransactionType.EXPENSE,
                new BigDecimal("54.50"), "Dinner with clients", null, date, PaymentMethod.DEBIT_CARD, null);

        transactionRepository.saveAll(List.of(tx1, tx2));

        // 1. Test SUM aggregation
        BigDecimal totalExpense = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                testUser.getId(),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
        );
        assertThat(totalExpense).isEqualByComparingTo(new BigDecimal("100.00"));

        // 2. Test Category Spending Breakdown Projection
        List<CategorySpendingProjection> breakdown = transactionRepository.getCategoryBreakdown(
                testUser.getId(),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
        );
        assertThat(breakdown).hasSize(1);
        assertThat(breakdown.getFirst().getCategoryName()).isEqualTo("Dining Out");
        assertThat(breakdown.getFirst().getTotalAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(breakdown.getFirst().getTransactionCount()).isEqualTo(2L);

        // 3. Test Monthly Spending Trend Projection
        List<MonthlySpendingProjection> trend = transactionRepository.getMonthlySpendingTrend(
                testUser.getId(),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );
        assertThat(trend).hasSize(1);
        assertThat(trend.getFirst().getYear()).isEqualTo(2026);
        assertThat(trend.getFirst().getMonth()).isEqualTo(10);
        assertThat(trend.getFirst().getTotalAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("BudgetRepository: Should save budget and verify period lookup")
    void testBudgetRepository() {
        Budget budget = new Budget(testUser, testCategory, 10, 2026, new BigDecimal("500.00"));
        budgetRepository.save(budget);

        Optional<Budget> found = budgetRepository.findByUserIdAndCategoryIdAndBudgetMonthAndBudgetYear(
                testUser.getId(), testCategory.getId(), 10, 2026);
        assertThat(found).isPresent();
        assertThat(found.get().getTargetAmount()).isEqualByComparingTo(new BigDecimal("500.00"));

        boolean exists = budgetRepository.existsByUserIdAndCategoryIdAndBudgetMonthAndBudgetYear(
                testUser.getId(), testCategory.getId(), 10, 2026);
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("NotificationRepository: Should count unread alerts correctly")
    void testNotificationRepository() {
        Notification notif1 = new Notification(testUser, NotificationType.BUDGET_WARNING, "80% threshold reached", "Dining Out budget reached 80%", 1L);
        Notification notif2 = new Notification(testUser, NotificationType.LOW_BALANCE, "Low balance", "Balance below limit", null);
        notif2.setRead(true);

        notificationRepository.saveAll(List.of(notif1, notif2));

        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(testUser.getId());
        assertThat(unreadCount).isEqualTo(1L);
    }
}
