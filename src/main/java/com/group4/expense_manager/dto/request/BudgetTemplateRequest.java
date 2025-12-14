package com.group4.expense_manager.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    
    @NotNull(message = "Tháng là bắt buộc")
    @Min(value = 1, message = "Tháng phải từ 1 đến 12")
    @Max(value = 12, message = "Tháng phải từ 1 đến 12")
    private Integer month;
    
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
