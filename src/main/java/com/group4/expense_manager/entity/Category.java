package com.group4.expense_manager.entity;

// Category.java
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;

import java.time.Instant;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "categories", indexes = {
        @Index(name = "idx_type", columnList = "type"),
        @Index(name = "idx_user_id", columnList = "user_id")
})
@SQLDelete(sql = "UPDATE categories SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // 'user_id' có thể là NULL
    private User user;

    @Column(nullable = false)
    private String name;

    @Lob
    private String description;


    private String icon; // Lưu trữ đường dẫn đến icon

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType type;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(nullable = false)
    private Boolean deleted = false;

    // --- Mối quan hệ ---
    // ON DELETE SET NULL: Khi Category bị xóa, Expense.category_id sẽ set về NULL
    @OneToMany(mappedBy = "category")
    private Set<Expense> expenses;

    @OneToMany(mappedBy = "category")
    private Set<Income> incomes;

    // ON DELETE CASCADE: Khi Category bị xóa, Budget liên quan cũng bị xóa
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Budget> budgets;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BudgetTemplateItem> budgetTemplateItems;
}