package com.group4.expense_manager.service;

import com.group4.expense_manager.dto.request.BudgetTemplateRequest;
import com.group4.expense_manager.entity.BudgetTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BudgetTemplateService {
    
    // Lấy danh sách template có phân trang và filter
    Page<BudgetTemplate> listTemplates(String keyword, Pageable pageable);
    
    // Lấy chi tiết template theo ID
    BudgetTemplate getTemplate(Integer id);
    
    // Tạo mới template
    BudgetTemplate createTemplate(BudgetTemplateRequest request);
    
    // Cập nhật template
    BudgetTemplate updateTemplate(Integer id, BudgetTemplateRequest request);
    
    // Xóa template
    void deleteTemplate(Integer id);
}
