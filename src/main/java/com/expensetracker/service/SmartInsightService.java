package com.expensetracker.service;

import com.expensetracker.dto.response.CategoryBreakdownResponse;
import com.expensetracker.dto.response.FinancialSummaryResponse;
import com.expensetracker.dto.response.SpendingInsightResponse;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.enums.TransactionType;
import com.expensetracker.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class SmartInsightService {

    private static final Logger log = LoggerFactory.getLogger(SmartInsightService.class);

    private final TransactionRepository transactionRepository;
    private final AnalyticsService analyticsService;
    private final BigDecimal lowBalanceThreshold;

    public SmartInsightService(
            TransactionRepository transactionRepository,
            AnalyticsService analyticsService,
            @Value("${app.insights.low-balance-threshold:250.00}") BigDecimal lowBalanceThreshold) {
        this.transactionRepository = transactionRepository;
        this.analyticsService = analyticsService;
        this.lowBalanceThreshold = lowBalanceThreshold;
    }

    public SpendingInsightResponse generateSpendingInsights(Long userId) {
        log.info("Generating rule-based financial spending insights for user ID: {}", userId);

        LocalDate today = LocalDate.now();
        LocalDate ninetyDaysAgo = today.minusDays(90);
        LocalDate fourteenDaysAgo = today.minusDays(14);

        // 1. Unusual Spending Detection
        List<SpendingInsightResponse.UnusualSpendingAlert> unusualAlerts = detectUnusualSpending(
                userId, ninetyDaysAgo, fourteenDaysAgo, today);

        // 2. Budget Recommendations
        List<SpendingInsightResponse.BudgetRecommendation> recommendations = generateBudgetRecommendations(
                userId, ninetyDaysAgo, today);

        // 3. Low Balance Check
        SpendingInsightResponse.LowBalanceAlert lowBalanceAlert = checkLowBalance(userId);

        // 4. Actionable Advice
        List<String> adviceList = generateActionableAdvice(userId, today);

        return new SpendingInsightResponse(unusualAlerts, recommendations, lowBalanceAlert, adviceList);
    }

    private List<SpendingInsightResponse.UnusualSpendingAlert> detectUnusualSpending(
            Long userId, LocalDate historyStart, LocalDate recentStart, LocalDate historyEnd) {
        List<Transaction> historicalTxs = transactionRepository
                .findAllByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(userId, historyStart, historyEnd);

        // Compute average transaction amount per category over past 90 days
        Map<Long, List<BigDecimal>> categoryAmounts = new HashMap<>();
        Map<Long, String> categoryNames = new HashMap<>();

        for (Transaction tx : historicalTxs) {
            if (tx.getType() == TransactionType.EXPENSE) {
                Long catId = tx.getCategory().getId();
                categoryAmounts.computeIfAbsent(catId, k -> new ArrayList<>()).add(tx.getAmount());
                categoryNames.put(catId, tx.getCategory().getName());
            }
        }

        Map<Long, BigDecimal> categoryAverages = new HashMap<>();
        for (Map.Entry<Long, List<BigDecimal>> entry : categoryAmounts.entrySet()) {
            BigDecimal sum = entry.getValue().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal avg = sum.divide(BigDecimal.valueOf(entry.getValue().size()), 2, RoundingMode.HALF_UP);
            categoryAverages.put(entry.getKey(), avg);
        }

        List<SpendingInsightResponse.UnusualSpendingAlert> alerts = new ArrayList<>();
        for (Transaction tx : historicalTxs) {
            // Check only recent transactions
            if (tx.getType() == TransactionType.EXPENSE && !tx.getTransactionDate().isBefore(recentStart)) {
                BigDecimal avg = categoryAverages.get(tx.getCategory().getId());
                if (avg != null && avg.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal anomalyThreshold = avg.multiply(BigDecimal.valueOf(2.5));
                    // Check if transaction is > 2.5x the average and > $30 to eliminate trivial purchases
                    if (tx.getAmount().compareTo(anomalyThreshold) > 0 && tx.getAmount().compareTo(BigDecimal.valueOf(30)) > 0) {
                        double multiple = tx.getAmount().divide(avg, 2, RoundingMode.HALF_UP).doubleValue();
                        alerts.add(new SpendingInsightResponse.UnusualSpendingAlert(
                                tx.getId(),
                                tx.getTitle(),
                                tx.getAmount(),
                                tx.getCategory().getName(),
                                avg,
                                multiple,
                                String.format("Transaction of %s is %.1fx higher than your 90-day category average of %s",
                                        tx.getAmount(), multiple, avg)
                        ));
                    }
                }
            }
        }

        return alerts;
    }

    private List<SpendingInsightResponse.BudgetRecommendation> generateBudgetRecommendations(
            Long userId, LocalDate start, LocalDate end) {
        List<CategoryBreakdownResponse> breakdown = analyticsService.getCategoryBreakdown(
                userId, TransactionType.EXPENSE, start, end);

        List<SpendingInsightResponse.BudgetRecommendation> recommendations = new ArrayList<>();
        for (CategoryBreakdownResponse cat : breakdown) {
            // 3-month total divided by 3
            BigDecimal avgMonthly = cat.totalAmount().divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
            if (avgMonthly.compareTo(BigDecimal.valueOf(20)) > 0) {
                // Add 10% realistic headroom
                BigDecimal recommendedTarget = avgMonthly.multiply(BigDecimal.valueOf(1.10)).setScale(2, RoundingMode.HALF_UP);
                recommendations.add(new SpendingInsightResponse.BudgetRecommendation(
                        cat.categoryId(),
                        cat.categoryName(),
                        avgMonthly,
                        recommendedTarget,
                        String.format("Based on your 3-month average of %s in %s, a target of %s provides a comfortable 10%% buffer.",
                                avgMonthly, cat.categoryName(), recommendedTarget)
                ));
            }
        }

        return recommendations;
    }

    private SpendingInsightResponse.LowBalanceAlert checkLowBalance(Long userId) {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = LocalDate.now();

        FinancialSummaryResponse summary = analyticsService.getFinancialSummary(userId, start, end);
        BigDecimal balance = summary.netBalance();

        boolean isTriggered = balance.compareTo(lowBalanceThreshold) < 0;
        String message = isTriggered
                ? String.format("Current net balance (%s) is below your safety threshold of %s. Consider reducing discretionary spending.",
                balance, lowBalanceThreshold)
                : String.format("Net balance is healthy (%s) and comfortably above safety threshold (%s).",
                balance, lowBalanceThreshold);

        return new SpendingInsightResponse.LowBalanceAlert(isTriggered, balance, lowBalanceThreshold, message);
    }

    private List<String> generateActionableAdvice(Long userId, LocalDate today) {
        List<String> advice = new ArrayList<>();
        LocalDate start = today.withDayOfMonth(1);
        FinancialSummaryResponse summary = analyticsService.getFinancialSummary(userId, start, today);

        if (summary.totalIncome().compareTo(BigDecimal.ZERO) > 0) {
            if (summary.savingsRatePercentage() >= 20.0) {
                advice.add(String.format("Great work! Your current savings rate is %.1f%%, exceeding the 20%% benchmark.",
                        summary.savingsRatePercentage()));
            } else {
                advice.add(String.format("Your savings rate is currently %.1f%%. Aim for at least 20%% to build an emergency fund.",
                        summary.savingsRatePercentage()));
            }
        }

        List<CategoryBreakdownResponse> breakdown = analyticsService.getCategoryBreakdown(
                userId, TransactionType.EXPENSE, start, today);

        if (!breakdown.isEmpty()) {
            CategoryBreakdownResponse top = breakdown.getFirst();
            if (top.percentageOfTotal() > 40.0) {
                advice.add(String.format("Category '%s' accounts for %.1f%% of your total expenses this month. Exploring ways to optimize this category could yield significant savings.",
                        top.categoryName(), top.percentageOfTotal()));
            }
        }

        if (advice.isEmpty()) {
            advice.add("Track consistent daily expenses to unlock personalized spending trends and advice.");
        }

        return advice;
    }
}
