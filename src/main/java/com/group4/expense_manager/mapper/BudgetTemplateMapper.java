package com.group4.expense_manager.mapper;

import com.group4.expense_manager.dto.response.BudgetTemplateResponse;
import com.group4.expense_manager.entity.BudgetTemplate;
import com.group4.expense_manager.entity.BudgetTemplateItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BudgetTemplateMapper {
    
    public BudgetTemplateResponse toResponse(BudgetTemplate template) {
        BudgetTemplateResponse response = new BudgetTemplateResponse();
        response.setId(template.getId());
        response.setName(template.getName());
        response.setMonth(template.getMonth());
        response.setDescription(template.getDescription());
        response.setCreatedAt(template.getCreatedAt());
        response.setUpdatedAt(template.getUpdatedAt());
        
        if (template.getItems() != null && !template.getItems().isEmpty()) {
            List<BudgetTemplateResponse.BudgetTemplateItemResponse> itemResponses = new ArrayList<>();
            for (BudgetTemplateItem item : template.getItems()) {
                BudgetTemplateResponse.BudgetTemplateItemResponse itemResponse = new BudgetTemplateResponse.BudgetTemplateItemResponse();
                itemResponse.setId(item.getId());
                itemResponse.setDefaultAmount(item.getDefaultAmount());
                
                if (item.getCategory() != null) {
                    itemResponse.setCategoryId(item.getCategory().getId());
                    itemResponse.setCategoryName(item.getCategory().getName());
                }
                
                itemResponses.add(itemResponse);
            }
            response.setItems(itemResponses);
        }
        
        return response;
    }
}
