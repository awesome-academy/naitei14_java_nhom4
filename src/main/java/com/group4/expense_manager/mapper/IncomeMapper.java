package com.group4.expense_manager.mapper;

import com.group4.expense_manager.dto.response.IncomeResponse;
import com.group4.expense_manager.entity.Income;
import org.springframework.stereotype.Component;

@Component
public class IncomeMapper {

    public IncomeResponse toResponse(Income income) {

        IncomeResponse dto = new IncomeResponse();
        dto.setId(income.getId());
        dto.setSource(income.getSource());
        dto.setAmount(income.getAmount());
        dto.setCurrency(income.getCurrency());
        dto.setIncomeDate(income.getIncomeDate());
        dto.setIsRecurring(income.isRecurring());
        dto.setRecurringInterval(income.getRecurringInterval());
        dto.setRecurringEndDate(income.getRecurringEndDate());
        dto.setNote(income.getNote());
        dto.setCreatedAt(income.getCreatedAt());
        dto.setUpdatedAt(income.getUpdatedAt());

        if (income.getCategory() != null) {
            dto.setCategoryId(income.getCategory().getId());
            dto.setCategoryName(income.getCategory().getName());
        }

        return dto;
    }
}
