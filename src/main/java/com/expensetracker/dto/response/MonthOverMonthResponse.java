package com.expensetracker.dto.response;

import java.math.BigDecimal;

public record MonthOverMonthResponse(
        int currentYear,
        int currentMonth,
        BigDecimal currentMonthSpending,
        int priorYear,
        int priorMonth,
        BigDecimal priorMonthSpending,
        BigDecimal spendingDifference,
        double percentageChange
) {}
