package com.group4.expense_manager.dto.request;

import lombok.Data;

@Data
public class IncomeCsvRequest {
    private String date;
    private String source;
    private String categoryName;
    private String amount;
    private String currency;
    private String isRecurring;
    private String note;
    private String userEmail;
}
