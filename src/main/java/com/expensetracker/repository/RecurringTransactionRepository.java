package com.expensetracker.repository;

import com.expensetracker.entity.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {

    Optional<RecurringTransaction> findByIdAndUserId(Long id, Long userId);

    List<RecurringTransaction> findByUserIdOrderByNextRunDateAsc(Long userId);

    List<RecurringTransaction> findByIsActiveTrueAndNextRunDateLessThanEqual(LocalDate date);
}
