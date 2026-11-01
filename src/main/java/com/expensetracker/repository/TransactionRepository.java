package com.expensetracker.repository;

import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.enums.TransactionType;
import com.expensetracker.repository.projection.CategorySpendingProjection;
import com.expensetracker.repository.projection.MonthlySpendingProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.user.id = :userId
          AND t.type = :type
          AND t.transactionDate BETWEEN :startDate AND :endDate
    """)
    BigDecimal sumAmountByUserIdAndTypeAndDateBetween(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.user.id = :userId
          AND t.category.id = :categoryId
          AND t.type = :type
          AND t.transactionDate BETWEEN :startDate AND :endDate
    """)
    BigDecimal sumAmountByUserIdAndCategoryIdAndTypeAndDateBetween(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT t.category.id AS categoryId,
               t.category.name AS categoryName,
               t.category.colorCode AS colorCode,
               t.category.icon AS icon,
               SUM(t.amount) AS totalAmount,
               COUNT(t.id) AS transactionCount
        FROM Transaction t
        WHERE t.user.id = :userId
          AND t.type = :type
          AND t.transactionDate BETWEEN :startDate AND :endDate
        GROUP BY t.category.id, t.category.name, t.category.colorCode, t.category.icon
        ORDER BY totalAmount DESC
    """)
    List<CategorySpendingProjection> getCategoryBreakdown(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT YEAR(t.transactionDate) AS year,
               MONTH(t.transactionDate) AS month,
               SUM(t.amount) AS totalAmount
        FROM Transaction t
        WHERE t.user.id = :userId
          AND t.type = :type
          AND t.transactionDate BETWEEN :startDate AND :endDate
        GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate)
        ORDER BY year ASC, month ASC
    """)
    List<MonthlySpendingProjection> getMonthlySpendingTrend(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    long countByUserIdAndTransactionDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    List<Transaction> findAllByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
            Long userId, LocalDate startDate, LocalDate endDate);
}
