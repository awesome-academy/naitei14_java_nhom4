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

@Getter
@Setter
@Entity
@Table(name = "incomes", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_category_id", columnList = "category_id"),
        @Index(name = "idx_income_date", columnList = "income_date")
})
public class Income {

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
    private String source;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    // --- MỚI: Đa tiền tệ ---
    @Column(nullable = false, length = 3)
    private String currency = "VND";

    @Column(name = "income_date", nullable = false)
    private LocalDate incomeDate;

    // ---Thu nhập định kỳ (VD: Lương) ---
    @Column(name = "is_recurring", nullable = false)
    private boolean isRecurring = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurring_interval")
    private RecurringInterval recurringInterval;


    @Column(name = "next_occurrence_date")
    private LocalDate nextOccurrenceDate;

    @Column(name = "recurring_end_date")
    private LocalDate recurringEndDate;

    @Column(name = "note", length = 500)
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}