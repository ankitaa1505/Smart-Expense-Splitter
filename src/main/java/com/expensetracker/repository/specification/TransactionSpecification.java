package com.expensetracker.repository.specification;

import com.expensetracker.dto.request.TransactionFilterRequest;
import com.expensetracker.entity.Transaction;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TransactionSpecification {

    public static Specification<Transaction> withFilters(TransactionFilterRequest filter, Long userId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Mandatory row-level tenant isolation
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            // 2. Filter by Transaction Type (INCOME or EXPENSE)
            if (filter.type() != null) {
                predicates.add(cb.equal(root.get("type"), filter.type()));
            }

            // 3. Filter by Category ID
            if (filter.categoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.categoryId()));
            }

            // 4. Date range filter
            if (filter.startDate() != null && filter.endDate() != null) {
                predicates.add(cb.between(root.get("transactionDate"), filter.startDate(), filter.endDate()));
            } else if (filter.startDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), filter.startDate()));
            } else if (filter.endDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), filter.endDate()));
            }

            // 5. Filter by Payment Method
            if (filter.paymentMethod() != null) {
                predicates.add(cb.equal(root.get("paymentMethod"), filter.paymentMethod()));
            }

            // 6. Case-insensitive search on title or description
            if (filter.searchTerm() != null && !filter.searchTerm().trim().isBlank()) {
                String searchPattern = "%" + filter.searchTerm().trim().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), searchPattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), searchPattern);
                predicates.add(cb.or(titleMatch, descMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
