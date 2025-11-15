package com.group4.expense_manager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "expenses", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_category_id", columnList = "category_id"),
        @Index(name = "idx_expense_date", columnList = "expense_date")
})
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // SQL là ON DELETE CASCADE, DB sẽ xử lý

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id") // 'category_id' có thể là NULL
    @OnDelete(action = OnDeleteAction.SET_NULL) // Phù hợp với 'ON DELETE SET NULL'
    private Category category;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Lob
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // --- Mối quan hệ ---
    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Attachment> attachments;
}