package com.expensetracker.repository.projection;

import java.math.BigDecimal;

public interface MonthlySpendingProjection {
    Integer getYear();
    Integer getMonth();
    BigDecimal getTotalAmount();
}
