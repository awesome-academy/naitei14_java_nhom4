package com.group4.expense_manager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "budget_template_items", uniqueConstraints = {
        @UniqueConstraint(name = "uk_template_category",
                columnNames = {"template_id", "category_id"})
})
public class BudgetTemplateItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private BudgetTemplate template; // SQL là ON DELETE CASCADE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category; // SQL là ON DELETE CASCADE

    @Column(name = "default_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal defaultAmount;
}