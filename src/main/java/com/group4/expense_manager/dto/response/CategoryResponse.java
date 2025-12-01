package com.group4.expense_manager.dto.response;

import com.group4.expense_manager.entity.CategoryType;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CategoryResponse {
    private Integer id;
    private String name;
    private String description;
    private String icon; // đường dẫn icon
    private CategoryType type;
    private Integer userId; // null nếu là global
    private Instant createdAt;
    private Instant updatedAt;
}
