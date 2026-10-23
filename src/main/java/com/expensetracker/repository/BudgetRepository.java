package com.expensetracker.repository;

import com.expensetracker.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByIdAndUserId(Long id, Long userId);

    List<Budget> findByUserIdAndBudgetMonthAndBudgetYear(Long userId, int budgetMonth, int budgetYear);

    Optional<Budget> findByUserIdAndCategoryIdAndBudgetMonthAndBudgetYear(Long userId, Long categoryId, int budgetMonth, int budgetYear);

    Optional<Budget> findByUserIdAndCategoryIsNullAndBudgetMonthAndBudgetYear(Long userId, int budgetMonth, int budgetYear);

    boolean existsByUserIdAndCategoryIdAndBudgetMonthAndBudgetYear(Long userId, Long categoryId, int budgetMonth, int budgetYear);

    boolean existsByUserIdAndCategoryIsNullAndBudgetMonthAndBudgetYear(Long userId, int budgetMonth, int budgetYear);
}
