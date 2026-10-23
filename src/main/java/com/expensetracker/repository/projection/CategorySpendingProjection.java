package com.expensetracker.repository.projection;

import java.math.BigDecimal;

public interface CategorySpendingProjection {
    Long getCategoryId();
    String getCategoryName();
    String getColorCode();
    String getIcon();
    BigDecimal getTotalAmount();
    Long getTransactionCount();
}
