package com.group4.expense_manager.mapper;

import com.group4.expense_manager.dto.response.CategoryResponse;
import com.group4.expense_manager.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public CategoryResponse toResponse(Category category) {
        CategoryResponse dto = new CategoryResponse();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setIcon(category.getIcon());
        dto.setType(category.getType());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        if (category.getUser() != null) {
            dto.setUserId(category.getUser().getId());
        }
        return dto;
    }
}