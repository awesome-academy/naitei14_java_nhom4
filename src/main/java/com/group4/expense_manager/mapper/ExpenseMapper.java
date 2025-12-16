package com.group4.expense_manager.mapper;

import com.group4.expense_manager.dto.response.ExpenseResponse;
import com.group4.expense_manager.entity.Expense;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {

    public ExpenseResponse toResponse(Expense expense) {
        ExpenseResponse dto = new ExpenseResponse();
        dto.setId(expense.getId());
        dto.setName(expense.getName());
        dto.setDescription(expense.getDescription());
        dto.setAmount(expense.getAmount());
        dto.setCurrency(expense.getCurrency());
        dto.setExpenseDate(expense.getExpenseDate());
        dto.setIsRecurring(expense.getIsRecurring());
        dto.setRecurringInterval(expense.getRecurringInterval() != null ? 
            expense.getRecurringInterval().name() : null);
        dto.setRecurringEndDate(expense.getRecurringEndDate());
        dto.setNote(expense.getNote());
        dto.setCreatedAt(expense.getCreatedAt() != null ? 
            expense.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null);
        dto.setUpdatedAt(expense.getUpdatedAt() != null ? 
            expense.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null);

        if (expense.getUser() != null) {
            dto.setUserId(expense.getUser().getId());
        }

        if (expense.getCategory() != null) {
            dto.setCategoryId(expense.getCategory().getId());
        }

        return dto;
    }
}
