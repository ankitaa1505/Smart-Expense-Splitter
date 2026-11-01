package com.expensetracker.service;

import com.expensetracker.dto.request.TransactionFilterRequest;
import com.expensetracker.dto.request.TransactionRequest;
import com.expensetracker.dto.response.CategoryResponse;
import com.expensetracker.dto.response.TransactionResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.User;
import com.expensetracker.entity.enums.PaymentMethod;
import com.expensetracker.entity.enums.Role;
import com.expensetracker.entity.enums.TransactionType;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.mapper.TransactionMapper;
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
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private BudgetService budgetService;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private Category expenseCategory;
    private Category incomeCategory;
    private Transaction transaction;
    private TransactionRequest validRequest;
    private TransactionResponse expectedResponse;

    @BeforeEach
    void setUp() {
        user = new User("user@example.com", "hash", "Jane", "Doe", Role.ROLE_USER);
        user.setId(1L);

        expenseCategory = new Category(user, "Groceries", TransactionType.EXPENSE, "cart", "#2ECC71", false);
        expenseCategory.setId(10L);

        incomeCategory = new Category(user, "Salary", TransactionType.INCOME, "briefcase", "#27AE60", false);
        incomeCategory.setId(20L);

        validRequest = new TransactionRequest(
                10L,
                TransactionType.EXPENSE,
                new BigDecimal("85.50"),
                "Trader Joe's groceries",
                "Weekly shopping",
                LocalDate.of(2026, 10, 20),
                PaymentMethod.CREDIT_CARD,
                "Used grocery card"
        );

        transaction = new Transaction(
                user, expenseCategory, TransactionType.EXPENSE,
                new BigDecimal("85.50"), "Trader Joe's groceries", "Weekly shopping",
                LocalDate.of(2026, 10, 20), PaymentMethod.CREDIT_CARD, "Used grocery card"
        );
        transaction.setId(100L);

        CategoryResponse categoryResponse = new CategoryResponse(
                10L, "Groceries", TransactionType.EXPENSE, "cart", "#2ECC71", false, Instant.now()
        );

        expectedResponse = new TransactionResponse(
                100L, TransactionType.EXPENSE, new BigDecimal("85.50"),
                "Trader Joe's groceries", "Weekly shopping", LocalDate.of(2026, 10, 20),
                PaymentMethod.CREDIT_CARD, "Used grocery card", categoryResponse,
                Instant.now(), Instant.now()
        );
    }

    @Test
    @DisplayName("createTransaction: Successfully creates and returns transaction")
    void testCreateTransactionSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findByIdAndUserIdOrSystemDefault(10L, 1L)).thenReturn(Optional.of(expenseCategory));
        when(transactionMapper.toEntity(validRequest, user, expenseCategory)).thenReturn(transaction);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.toDto(transaction)).thenReturn(expectedResponse);

        TransactionResponse result = transactionService.createTransaction(validRequest, 1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("85.50"));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("createTransaction: Throws BadRequestException when category type does not match transaction type")
    void testCreateTransactionTypeMismatch() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findByIdAndUserIdOrSystemDefault(10L, 1L)).thenReturn(Optional.of(incomeCategory));

        assertThatThrownBy(() -> transactionService.createTransaction(validRequest, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot be assigned to an EXPENSE transaction");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("createTransaction: Throws ResourceNotFoundException when category not found")
    void testCreateTransactionCategoryNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findByIdAndUserIdOrSystemDefault(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.createTransaction(validRequest, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found with id: '10'");
    }

    @Test
    @DisplayName("getTransactionById: Throws ResourceNotFoundException when ID does not exist or belongs to another user")
    void testGetTransactionNotFound() {
        when(transactionRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransactionById(999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transaction not found with id: '999'");
    }

    @Test
    @DisplayName("getTransactions: Throws BadRequestException when startDate is after endDate")
    void testGetTransactionsInvalidDateRange() {
        TransactionFilterRequest filter = new TransactionFilterRequest(
                TransactionType.EXPENSE, null,
                LocalDate.of(2026, 10, 30),
                LocalDate.of(2026, 10, 1),
                null, null, 0, 10, "transactionDate", "DESC"
        );

        assertThatThrownBy(() -> transactionService.getTransactions(filter, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Start date cannot be after end date");
    }
}
