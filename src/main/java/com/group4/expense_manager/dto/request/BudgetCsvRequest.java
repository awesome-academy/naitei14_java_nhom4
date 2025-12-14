package com.group4.expense_manager.dto.request;

import lombok.Data;

@Data
public class BudgetCsvRequest {
    private String categoryName;
    private String amount;
    private String currency;
    private String startDate;
    private String endDate;
    private String userEmail;
}