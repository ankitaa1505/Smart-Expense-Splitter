package com.expensetracker.dto.response;

import java.math.BigDecimal;

public record MonthlyTrendResponse(
        int year,
        int month,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netSavings
) {}
