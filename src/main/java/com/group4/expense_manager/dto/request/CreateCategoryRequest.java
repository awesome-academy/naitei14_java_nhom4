package com.group4.expense_manager.dto.request;

import com.group4.expense_manager.entity.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCategoryRequest {
	@NotBlank(message = "Tên danh mục không được để trống.")
	private String name;

	private String description;

	// URL icon (có thể được set sau khi upload Cloudinary)
	private String icon;

	@NotNull(message = "Loại danh mục không được để trống.")
	private CategoryType type; // expense | income
}
