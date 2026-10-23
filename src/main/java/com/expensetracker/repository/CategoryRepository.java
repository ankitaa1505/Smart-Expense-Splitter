package com.expensetracker.repository;

import com.expensetracker.entity.Category;
import com.expensetracker.entity.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c WHERE c.user.id = :userId OR c.isSystemDefault = true ORDER BY c.name ASC")
    List<Category> findAllAvailableForUser(@Param("userId") Long userId);

    @Query("SELECT c FROM Category c WHERE (c.user.id = :userId OR c.isSystemDefault = true) AND c.type = :type ORDER BY c.name ASC")
    List<Category> findAllAvailableForUserAndType(@Param("userId") Long userId, @Param("type") TransactionType type);

    @Query("SELECT c FROM Category c WHERE c.id = :categoryId AND (c.user.id = :userId OR c.isSystemDefault = true)")
    Optional<Category> findByIdAndUserIdOrSystemDefault(@Param("categoryId") Long categoryId, @Param("userId") Long userId);

    boolean existsByNameIgnoreCaseAndUserIdAndType(String name, Long userId, TransactionType type);

    boolean existsByNameIgnoreCaseAndIsSystemDefaultTrueAndType(String name, TransactionType type);
}
