package com.group4.expense_manager.dto.response;

import com.group4.expense_manager.entity.RecurringInterval;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;

@Getter
@Setter
public class IncomeResponse {

    private Integer id;

    private String source;
    private BigDecimal amount;
    private String currency;

    private LocalDate incomeDate;

    private Boolean isRecurring;
    private RecurringInterval recurringInterval;
    private LocalDate recurringEndDate;

    private String note;

    private Integer categoryId;
    private String categoryName;

    private Instant createdAt;
    private Instant updatedAt;
}
