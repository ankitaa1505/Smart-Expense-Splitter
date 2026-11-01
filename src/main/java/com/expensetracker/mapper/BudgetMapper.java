package com.expensetracker.mapper;

import com.expensetracker.dto.response.BudgetResponse;
import com.expensetracker.entity.Budget;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class BudgetMapper {

    public BudgetResponse toDto(Budget entity, BigDecimal actualSpent) {
        if (entity == null) return null;

        BigDecimal spent = (actualSpent != null) ? actualSpent : BigDecimal.ZERO;
        BigDecimal target = entity.getTargetAmount();
        BigDecimal remaining = target.subtract(spent);

        double utilizationPercentage = 0.0;
        if (target.compareTo(BigDecimal.ZERO) > 0) {
            utilizationPercentage = spent.divide(target, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        String status = "ON_TRACK";
        if (utilizationPercentage >= 100.0) {
            status = "EXCEEDED";
        } else if (utilizationPercentage >= 80.0) {
            status = "WARNING";
        }

        Long categoryId = entity.getCategory() != null ? entity.getCategory().getId() : null;
        String categoryName = entity.getCategory() != null ? entity.getCategory().getName() : "Overall Monthly Budget";

        return new BudgetResponse(
                entity.getId(),
                categoryId,
                categoryName,
                entity.getBudgetMonth(),
                entity.getBudgetYear(),
                entity.getTargetAmount(),
                spent,
                remaining,
                utilizationPercentage,
                status,
                entity.getCreatedAt()
        );
    }
}
