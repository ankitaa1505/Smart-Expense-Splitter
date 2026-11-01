package com.expensetracker.service;

import com.expensetracker.dto.response.FinancialSummaryResponse;
import com.expensetracker.dto.response.MonthOverMonthResponse;
import com.expensetracker.entity.enums.TransactionType;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.projection.CategorySpendingProjection;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    @DisplayName("getFinancialSummary: Accurately calculates balance, savings rate, and daily average")
    void testGetFinancialSummary() {
        LocalDate start = LocalDate.of(2026, 10, 1);
        LocalDate end = LocalDate.of(2026, 10, 31); // 31 days

        when(transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(1L, TransactionType.INCOME, start, end))
                .thenReturn(new BigDecimal("5000.00"));
        when(transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(1L, TransactionType.EXPENSE, start, end))
                .thenReturn(new BigDecimal("2000.00"));

        CategorySpendingProjection projection = mock(CategorySpendingProjection.class);
        when(projection.getCategoryName()).thenReturn("Rent & Housing");
        when(transactionRepository.getCategoryBreakdown(1L, TransactionType.EXPENSE, start, end))
                .thenReturn(List.of(projection));

        when(transactionRepository.countByUserIdAndTransactionDateBetween(1L, start, end))
                .thenReturn(25L);

        FinancialSummaryResponse summary = analyticsService.getFinancialSummary(1L, start, end);

        assertThat(summary).isNotNull();
        assertThat(summary.totalIncome()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(summary.totalExpense()).isEqualByComparingTo(new BigDecimal("2000.00"));
        assertThat(summary.netBalance()).isEqualByComparingTo(new BigDecimal("3000.00"));
        // Savings rate = ((5000 - 2000) / 5000) * 100 = 60.0%
        assertThat(summary.savingsRatePercentage()).isEqualTo(60.0);
        // Average daily spending = 2000 / 31 = 64.52
        assertThat(summary.averageDailySpending()).isEqualByComparingTo(new BigDecimal("64.52"));
        assertThat(summary.highestSpendingCategory()).isEqualTo("Rent & Housing");
        assertThat(summary.totalTransactionsCount()).isEqualTo(25L);
    }

    @Test
    @DisplayName("getMonthOverMonthChange: Correctly computes percentage difference between two months")
    void testGetMonthOverMonthChange() {
        // Current: Oct 2026, Prior: Sep 2026
        when(transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                eq(1L), eq(TransactionType.EXPENSE), eq(LocalDate.of(2026, 10, 1)), eq(LocalDate.of(2026, 10, 31))))
                .thenReturn(new BigDecimal("1500.00"));

        when(transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                eq(1L), eq(TransactionType.EXPENSE), eq(LocalDate.of(2026, 9, 1)), eq(LocalDate.of(2026, 9, 30))))
                .thenReturn(new BigDecimal("1200.00"));

        MonthOverMonthResponse response = analyticsService.getMonthOverMonthChange(1L, 2026, 10);

        assertThat(response).isNotNull();
        assertThat(response.currentMonthSpending()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(response.priorMonthSpending()).isEqualByComparingTo(new BigDecimal("1200.00"));
        assertThat(response.spendingDifference()).isEqualByComparingTo(new BigDecimal("300.00"));
        // ((1500 - 1200) / 1200) * 100 = 25.0%
        assertThat(response.percentageChange()).isEqualTo(25.0);
    }
}
