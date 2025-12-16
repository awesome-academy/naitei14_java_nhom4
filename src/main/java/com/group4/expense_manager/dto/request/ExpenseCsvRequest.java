package com.group4.expense_manager.dto.request;

import lombok.Data;

@Data
public class ExpenseCsvRequest {
    private String name;
    private String description;
    private String amount;
    private String currency;
    private String expenseDate;
    private String categoryName;
    private String note;
    private String userEmail;
    private String isRecurring;
}
