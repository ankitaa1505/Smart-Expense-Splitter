package com.expensetracker.service;

import com.expensetracker.dto.response.CategoryBreakdownResponse;
import com.expensetracker.dto.response.FinancialSummaryResponse;
import com.expensetracker.dto.response.MonthOverMonthResponse;
import com.expensetracker.dto.response.MonthlyTrendResponse;
import com.expensetracker.entity.enums.TransactionType;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.projection.CategorySpendingProjection;
import com.expensetracker.repository.projection.MonthlySpendingProjection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final TransactionRepository transactionRepository;

    public AnalyticsService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Cacheable(value = "analytics:summary", key = "#userId + ':' + #startDate + ':' + #endDate")
    public FinancialSummaryResponse getFinancialSummary(Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("Calculating financial summary for user ID: {} from {} to {}", userId, startDate, endDate);

        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        BigDecimal totalIncome = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                userId, TransactionType.INCOME, startDate, endDate);
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;

        BigDecimal totalExpense = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                userId, TransactionType.EXPENSE, startDate, endDate);
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;

        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        // Savings rate formula: ((Income - Expense) / Income) * 100
        double savingsRatePercentage = 0.0;
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal savings = totalIncome.subtract(totalExpense);
            if (savings.compareTo(BigDecimal.ZERO) > 0) {
                savingsRatePercentage = savings.divide(totalIncome, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
            }
        }

        long daysInRange = Math.max(1, ChronoUnit.DAYS.between(startDate, endDate) + 1);
        BigDecimal averageDailySpending = totalExpense.divide(BigDecimal.valueOf(daysInRange), 2, RoundingMode.HALF_UP);

        List<CategorySpendingProjection> breakdown = transactionRepository.getCategoryBreakdown(
                userId, TransactionType.EXPENSE, startDate, endDate);

        String highestCategory = breakdown.isEmpty() ? "None" : breakdown.getFirst().getCategoryName();
        long transactionCount = transactionRepository.countByUserIdAndTransactionDateBetween(userId, startDate, endDate);

        return new FinancialSummaryResponse(
                totalIncome,
                totalExpense,
                netBalance,
                savingsRatePercentage,
                averageDailySpending,
                highestCategory,
                transactionCount,
                startDate,
                endDate
        );
    }

    public List<CategoryBreakdownResponse> getCategoryBreakdown(
            Long userId, TransactionType type, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();
        if (type == null) type = TransactionType.EXPENSE;

        BigDecimal totalAmount = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                userId, type, startDate, endDate);
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return Collections.emptyList();
        }

        List<CategorySpendingProjection> projections = transactionRepository.getCategoryBreakdown(
                userId, type, startDate, endDate);

        List<CategoryBreakdownResponse> result = new ArrayList<>();
        for (CategorySpendingProjection p : projections) {
            double percentage = p.getTotalAmount().divide(totalAmount, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();

            result.add(new CategoryBreakdownResponse(
                    p.getCategoryId(),
                    p.getCategoryName(),
                    p.getColorCode(),
                    p.getIcon(),
                    p.getTotalAmount(),
                    p.getTransactionCount() != null ? p.getTransactionCount() : 0L,
                    percentage
            ));
        }

        return result;
    }

    public List<MonthlyTrendResponse> getMonthlyTrends(Long userId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusMonths(6).withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        List<MonthlySpendingProjection> incomeProjections = transactionRepository.getMonthlySpendingTrend(
                userId, TransactionType.INCOME, startDate, endDate);
        List<MonthlySpendingProjection> expenseProjections = transactionRepository.getMonthlySpendingTrend(
                userId, TransactionType.EXPENSE, startDate, endDate);

        Map<String, BigDecimal> incomeMap = new HashMap<>();
        for (MonthlySpendingProjection p : incomeProjections) {
            incomeMap.put(p.getYear() + "-" + p.getMonth(), p.getTotalAmount());
        }

        Map<String, BigDecimal> expenseMap = new HashMap<>();
        for (MonthlySpendingProjection p : expenseProjections) {
            expenseMap.put(p.getYear() + "-" + p.getMonth(), p.getTotalAmount());
        }

        Set<String> periods = new TreeSet<>(Comparator.naturalOrder());
        periods.addAll(incomeMap.keySet());
        periods.addAll(expenseMap.keySet());

        List<MonthlyTrendResponse> trends = new ArrayList<>();
        for (String period : periods) {
            String[] parts = period.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);

            BigDecimal inc = incomeMap.getOrDefault(period, BigDecimal.ZERO);
            BigDecimal exp = expenseMap.getOrDefault(period, BigDecimal.ZERO);
            BigDecimal net = inc.subtract(exp);

            trends.add(new MonthlyTrendResponse(year, month, inc, exp, net));
        }

        return trends;
    }

    public MonthOverMonthResponse getMonthOverMonthChange(Long userId, int year, int month) {
        LocalDate currentStart = LocalDate.of(year, month, 1);
        LocalDate currentEnd = currentStart.withDayOfMonth(currentStart.lengthOfMonth());

        LocalDate priorStart = currentStart.minusMonths(1);
        LocalDate priorEnd = priorStart.withDayOfMonth(priorStart.lengthOfMonth());

        BigDecimal currentSpending = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                userId, TransactionType.EXPENSE, currentStart, currentEnd);
        if (currentSpending == null) currentSpending = BigDecimal.ZERO;

        BigDecimal priorSpending = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                userId, TransactionType.EXPENSE, priorStart, priorEnd);
        if (priorSpending == null) priorSpending = BigDecimal.ZERO;

        BigDecimal difference = currentSpending.subtract(priorSpending);

        double percentageChange = 0.0;
        if (priorSpending.compareTo(BigDecimal.ZERO) > 0) {
            percentageChange = difference.divide(priorSpending, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        return new MonthOverMonthResponse(
                year,
                month,
                currentSpending,
                priorStart.getYear(),
                priorStart.getMonthValue(),
                priorSpending,
                difference,
                percentageChange
        );
    }
}
