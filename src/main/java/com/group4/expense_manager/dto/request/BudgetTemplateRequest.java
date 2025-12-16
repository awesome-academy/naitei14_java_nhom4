package com.group4.expense_manager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class BudgetTemplateRequest {
    
    @NotBlank(message = "Tên mẫu ngân sách là bắt buộc")
    private String name;
    
    // Tháng không còn được nhập ở UI; giữ giá trị mặc định = 1 để phù hợp entity
    private Integer month = 1;
    
    private String description;
    
    private List<BudgetTemplateItemRequest> items;
    
    @Getter
    @Setter
    public static class BudgetTemplateItemRequest {
        @NotNull(message = "Danh mục là bắt buộc")
        private Integer categoryId;
        
        @NotNull(message = "Số tiền mặc định là bắt buộc")
        private BigDecimal defaultAmount;
    }
}
