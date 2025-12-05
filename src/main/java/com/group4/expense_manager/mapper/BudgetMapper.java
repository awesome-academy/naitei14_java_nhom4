package com.group4.expense_manager.mapper;

import com.group4.expense_manager.dto.response.BudgetResponse;
import com.group4.expense_manager.entity.Budget;
import org.springframework.stereotype.Component;

@Component
public class BudgetMapper {
    
    public BudgetResponse toResponse(Budget budget) {
        BudgetResponse dto = new BudgetResponse();
        dto.setId(budget.getId());
        dto.setAmount(budget.getAmount());
        dto.setCurrency(budget.getCurrency());
        dto.setStartDate(budget.getStartDate());
        dto.setEndDate(budget.getEndDate());
        dto.setCreatedAt(budget.getCreatedAt());
        dto.setUpdatedAt(budget.getUpdatedAt());
        
        if (budget.getCategory() != null) {
            dto.setCategoryId(budget.getCategory().getId());
            dto.setCategoryName(budget.getCategory().getName());
        }
        
        return dto;
    }
}