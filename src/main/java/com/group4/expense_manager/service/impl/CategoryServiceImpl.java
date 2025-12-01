package com.group4.expense_manager.service.impl;

import com.group4.expense_manager.dto.request.CreateCategoryRequest;
import com.group4.expense_manager.entity.Category;
import com.group4.expense_manager.entity.CategoryType;
import com.group4.expense_manager.entity.User;
import com.group4.expense_manager.repository.CategoryRepository;
import com.group4.expense_manager.service.CategoryService;
import com.group4.expense_manager.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryServiceImpl implements CategoryService {

	private final CategoryRepository categoryRepository;

	private final CloudinaryService cloudinaryService;

	@Autowired
	public CategoryServiceImpl(CategoryRepository categoryRepository, CloudinaryService cloudinaryService) {
		this.categoryRepository = categoryRepository;
        this.cloudinaryService = cloudinaryService;
    }

	@Override
	public Page<Category> listCategories(User user, CategoryType type, Pageable pageable) {
		if (type == null) {
			return categoryRepository.findByUserIsNullOrUser(user, pageable);
		}
		return categoryRepository.findByTypeAndUserIsNullOrUser(type, user, pageable);
	}

	@Override
	public Category getCategory(Integer categoryId, User user) {
		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
		if (category.getUser() != null && !category.getUser().getId().equals(user.getId())) {
			throw new RuntimeException("Bạn không có quyền truy cập danh mục này");
		}
		return category;
	}

	@Override
	@Transactional
	public Category createCategory(User user, CreateCategoryRequest request) {
		Category category = new Category();
		category.setName(request.getName());
		category.setDescription(request.getDescription());
		category.setType(request.getType());
		category.setIcon(request.getIcon());
		category.setUser(user); // Danh mục riêng của user
		return categoryRepository.save(category);
	}

	@Override
	@Transactional
	public Category updateCategory(Integer categoryId, User user, CreateCategoryRequest request) {
		Category category = getCategory(categoryId, user);
		// Không cho sửa danh mục global (user == null)
		if (category.getUser() == null) {
			throw new RuntimeException("Không thể sửa danh mục dùng chung.");
		}
		category.setName(request.getName());
		category.setDescription(request.getDescription());
		category.setType(request.getType());
		if (request.getIcon() != null && !request.getIcon().isBlank()) {
			cloudinaryService.deleteIcon(category.getIcon());
			category.setIcon(request.getIcon());
		}
		return categoryRepository.save(category);
	}

	@Override
	@Transactional
	public void deleteCategory(Integer categoryId, User user) {
		Category category = getCategory(categoryId, user);
		if (category.getIcon() != null){
			cloudinaryService.deleteIcon(category.getIcon());
		}
		if (category.getUser() == null) {
			throw new RuntimeException("Không thể xóa danh mục dùng chung.");
		}
		categoryRepository.delete(category);
	}
}
