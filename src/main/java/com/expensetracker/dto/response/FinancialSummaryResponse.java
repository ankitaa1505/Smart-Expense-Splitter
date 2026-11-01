package com.expensetracker.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinancialSummaryResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netBalance,
        double savingsRatePercentage, // Formula: ((Income - Expense) / Income) * 100. Assumes 0% if Income <= 0.
        BigDecimal averageDailySpending,
        String highestSpendingCategory,
        long totalTransactionsCount,
        LocalDate startDate,
        LocalDate endDate
) {}
