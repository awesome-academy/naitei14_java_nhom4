package com.group4.expense_manager.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class BudgetTemplateResponse {
    private Integer id;
    private String name;
    private Integer month;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private List<BudgetTemplateItemResponse> items;
    
    @Getter
    @Setter
    public static class BudgetTemplateItemResponse {
        private Integer id;
        private Integer categoryId;
        private String categoryName;
        private BigDecimal defaultAmount;
    }
}
