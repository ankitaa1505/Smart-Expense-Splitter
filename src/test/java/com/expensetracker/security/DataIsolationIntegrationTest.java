package com.expensetracker.security;

import com.expensetracker.entity.Category;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.User;
import com.expensetracker.entity.enums.PaymentMethod;
import com.expensetracker.entity.enums.Role;
import com.expensetracker.entity.enums.TransactionType;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DataIsolationIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionService transactionService;

    private User userA;
    private User userB;
    private Transaction transactionB;

    @BeforeEach
    void setUp() {
        userA = userRepository.save(new User("usera@example.com", "pass", "User", "A", Role.ROLE_USER));
        userB = userRepository.save(new User("userb@example.com", "pass", "User", "B", Role.ROLE_USER));

        Category catB = categoryRepository.save(new Category(userB, "Books", TransactionType.EXPENSE, "book", "#333", false));

        transactionB = transactionRepository.save(new Transaction(
                userB, catB, TransactionType.EXPENSE, new BigDecimal("35.00"),
                "Java Concurrency", "Book purchase", LocalDate.now(), PaymentMethod.CREDIT_CARD, null
        ));
    }

    @Test
    @DisplayName("IDOR Prevention: User A querying User B's transaction throws ResourceNotFoundException")
    void testUserCannotAccessOtherUsersTransaction() {
        // User B owns transactionB. User A tries to access it:
        assertThatThrownBy(() -> transactionService.getTransactionById(transactionB.getId(), userA.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transaction not found with id");
    }

    @Test
    @DisplayName("IDOR Prevention: User A attempting to delete User B's transaction throws ResourceNotFoundException")
    void testUserCannotDeleteOtherUsersTransaction() {
        assertThatThrownBy(() -> transactionService.deleteTransaction(transactionB.getId(), userA.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transaction not found with id");

        // Verify transaction was NOT deleted
        assertThat(transactionRepository.existsById(transactionB.getId())).isTrue();
    }
}
