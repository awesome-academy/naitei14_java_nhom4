package com.group4.expense_manager.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;

@Getter
@Setter
public class BudgetResponse {
    private Integer id;
    private Integer categoryId;
    private String categoryName;
    private BigDecimal amount;
    private String currency;
    private LocalDate startDate;
    private LocalDate endDate;
    private Instant createdAt;
    private Instant updatedAt;
}