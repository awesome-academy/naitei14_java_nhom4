package com.group4.expense_manager.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CreateBudgetRequest {
    
    @NotNull(message = "Category is required")
    private Integer categoryId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    private String currency = "VND";
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    private LocalDate endDate;
}