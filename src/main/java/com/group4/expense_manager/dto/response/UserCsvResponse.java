package com.group4.expense_manager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserCsvResponse {
    private Integer id;
    private String name;
    private String email;
    private String role;
    private Double totalExpense;
    private Double totalIncome;

    public Double getBalance() {
        return (totalIncome != null ? totalIncome : 0) - (totalExpense != null ? totalExpense : 0);
    }
}