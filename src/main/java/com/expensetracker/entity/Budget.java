package com.expensetracker.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "budgets", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_budget_period", columnNames = {"user_id", "category_id", "budget_month", "budget_year"})
})
public class Budget extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_budgets_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "fk_budgets_category"))
    private Category category;

    @Column(name = "budget_month", nullable = false)
    private int budgetMonth;

    @Column(name = "budget_year", nullable = false)
    private int budgetYear;

    @Column(name = "target_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "threshold_warned", nullable = false)
    private boolean thresholdWarned = false;

    @Column(name = "exceeded_notified", nullable = false)
    private boolean exceededNotified = false;

    public Budget() {
    }

    public Budget(User user, Category category, int budgetMonth, int budgetYear, BigDecimal targetAmount) {
        this.user = user;
        this.category = category;
        this.budgetMonth = budgetMonth;
        this.budgetYear = budgetYear;
        this.targetAmount = targetAmount;
        this.thresholdWarned = false;
        this.exceededNotified = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public int getBudgetMonth() {
        return budgetMonth;
    }

    public void setBudgetMonth(int budgetMonth) {
        this.budgetMonth = budgetMonth;
    }

    public int getBudgetYear() {
        return budgetYear;
    }

    public void setBudgetYear(int budgetYear) {
        this.budgetYear = budgetYear;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public boolean isThresholdWarned() {
        return thresholdWarned;
    }

    public void setThresholdWarned(boolean thresholdWarned) {
        this.thresholdWarned = thresholdWarned;
    }

    public boolean isExceededNotified() {
        return exceededNotified;
    }

    public void setExceededNotified(boolean exceededNotified) {
        this.exceededNotified = exceededNotified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Budget budget = (Budget) o;
        return Objects.equals(id, budget.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Budget{" +
                "id=" + id +
                ", budgetMonth=" + budgetMonth +
                ", budgetYear=" + budgetYear +
                ", targetAmount=" + targetAmount +
                ", thresholdWarned=" + thresholdWarned +
                ", exceededNotified=" + exceededNotified +
                '}';
    }
}
