package com.expensetracker.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record SpendingInsightResponse(
        List<UnusualSpendingAlert> unusualSpendingAlerts,
        List<BudgetRecommendation> budgetRecommendations,
        LowBalanceAlert lowBalanceAlert,
        List<String> actionableAdvice
) {
    public record UnusualSpendingAlert(
            Long transactionId,
            String title,
            BigDecimal amount,
            String categoryName,
            BigDecimal categoryAverage,
            double multipleOfAverage,
            String reason
    ) {}

    public record BudgetRecommendation(
            Long categoryId,
            String categoryName,
            BigDecimal averageMonthlySpending,
            BigDecimal recommendedTarget,
            String rationale
    ) {}

    public record LowBalanceAlert(
            boolean isTriggered,
            BigDecimal currentBalance,
            BigDecimal threshold,
            String message
    ) {}
}
