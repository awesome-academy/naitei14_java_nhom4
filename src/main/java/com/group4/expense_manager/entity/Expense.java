package com.group4.expense_manager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;

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
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // NEW — trùng với DB
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "VND";

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Lob
    private String note;

    // NEW — trùng DB: is_recurring BIT
    @Column(name = "is_recurring", nullable = false)
    private Boolean isRecurring = false;

    // NEW — trùng DB: recurring_end_date DATE
    @Column(name = "recurring_end_date")
    private LocalDate recurringEndDate;

    // NEW — trùng DB: recurring_interval ENUM
    @Enumerated(EnumType.STRING)
    @Column(name = "recurring_interval")
    private RecurringInterval recurringInterval;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // ENUM cho recurring_interval — DAILY / MONTHLY / WEEKLY / YEARLY
    public enum RecurringInterval {
        DAILY,
        MONTHLY,
        WEEKLY,
        YEARLY
    }

    @Column(name = "attachment")
    private String attachment;

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }
}
