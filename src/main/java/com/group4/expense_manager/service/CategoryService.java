package com.group4.expense_manager.service;

import com.group4.expense_manager.dto.request.CreateCategoryRequest;
import com.group4.expense_manager.entity.Category;
import com.group4.expense_manager.entity.CategoryType;
import com.group4.expense_manager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
	Page<Category> listCategories(User user, CategoryType type, Pageable pageable);
	Category getCategory(Integer categoryId, User user);
	Category createCategory(User user, CreateCategoryRequest request);
	Category updateCategory(Integer categoryId, User user, CreateCategoryRequest request);
	void deleteCategory(Integer categoryId, User user);
	
	// Admin methods for global categories
	Page<Category> adminListGlobalCategories(CategoryType type, String keyword, Pageable pageable);
	Category adminGetGlobalCategory(Integer categoryId);
	Category adminCreateGlobalCategory(CreateCategoryRequest request);
	Category adminUpdateGlobalCategory(Integer categoryId, CreateCategoryRequest request);
	void adminSoftDeleteCategory(Integer categoryId);
	
	// Import/Export CSV
	byte[] exportCategoriesToCsv();
	void importCategoriesFromCsv(org.springframework.web.multipart.MultipartFile file) throws Exception;
}
