package com.expensetracker.entity;

import com.expensetracker.entity.enums.TransactionType;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "categories", uniqueConstraints = {
        @UniqueConstraint(name = "uk_category_per_user", columnNames = {"user_id", "name", "type"})
})
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_categories_user"))
    private User user;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(length = 50)
    private String icon = "default-icon";

    @Column(name = "color_code", length = 10)
    private String colorCode = "#4A90E2";

    @Column(name = "is_system_default", nullable = false)
    private boolean isSystemDefault = false;

    public Category() {
    }

    public Category(User user, String name, TransactionType type, String icon, String colorCode, boolean isSystemDefault) {
        this.user = user;
        this.name = name;
        this.type = type;
        this.icon = icon != null ? icon : "default-icon";
        this.colorCode = colorCode != null ? colorCode : "#4A90E2";
        this.isSystemDefault = isSystemDefault;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public boolean isSystemDefault() {
        return isSystemDefault;
    }

    public void setSystemDefault(boolean systemDefault) {
        isSystemDefault = systemDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", isSystemDefault=" + isSystemDefault +
                '}';
    }
}
