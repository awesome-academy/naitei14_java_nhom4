package com.group4.expense_manager.dto.request;

import com.group4.expense_manager.entity.RecurringInterval;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class IncomeRequest {

    @NotBlank(message = "Nguồn thu nhập không được để trống.")
    private String source;

    @NotNull(message = "Số tiền không được để trống.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Số tiền phải lớn hơn 0.")
    private BigDecimal amount;

    // Currency: ĐỂ NULL → để service chủ động fallback
    private String currency;

    @NotNull(message = "Ngày thu nhập không được để trống.")
    private LocalDate incomeDate;

    private Integer categoryId;
    private String note;

    // Recurring
    private Boolean isRecurring = false;
    private RecurringInterval recurringInterval;
    private LocalDate recurringEndDate;
}
