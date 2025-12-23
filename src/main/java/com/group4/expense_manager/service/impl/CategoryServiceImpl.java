package com.group4.expense_manager.service.impl;

import com.group4.expense_manager.annotation.LogActivity;
import com.group4.expense_manager.dto.request.CreateCategoryRequest;
import com.group4.expense_manager.entity.Category;
import com.group4.expense_manager.entity.CategoryType;
import com.group4.expense_manager.entity.User;
import com.group4.expense_manager.repository.CategoryRepository;
import com.group4.expense_manager.service.CategoryService;
import com.group4.expense_manager.service.CloudinaryService;
import com.group4.expense_manager.service.CsvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final CloudinaryService cloudinaryService;

    private final CsvService csvService;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, CloudinaryService cloudinaryService, CsvService csvService) {
        this.categoryRepository = categoryRepository;
        this.cloudinaryService = cloudinaryService;
        this.csvService = csvService;
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
	@LogActivity(
        action = "CREATE",
        targetEntity = "CATEGORY",
        description = "Created new category"
    )
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
	@LogActivity(
        action = "UPDATE",
        targetEntity = "CATEGORY",
        description = "Updated category information"
    )
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
			// cloudinaryService.deleteIcon(category.getIcon());
			category.setIcon(request.getIcon());
		}
		return categoryRepository.save(category);
	}

	@Override
	@Transactional
	@LogActivity(
        action = "DELETE",
        targetEntity = "CATEGORY",
        description = "Deleted category"
    )
	public void deleteCategory(Integer categoryId, User user) {
		Category category = getCategory(categoryId, user);
		if (category.getUser() == null) {
			throw new RuntimeException("Không thể xóa danh mục dùng chung.");
		}
		// Soft delete - set deleted flag to true
		category.setDeleted(true);
		categoryRepository.save(category);
	}

	// Admin methods for global categories management
	@Override
	public Page<Category> adminListGlobalCategories(CategoryType type, String keyword, Pageable pageable) {
		// Filter by type and keyword
		if (type != null && keyword != null && !keyword.trim().isEmpty()) {
			return categoryRepository.findByUserIsNullAndTypeAndNameContainingIgnoreCase(type, keyword.trim(), pageable);
		}
		// Filter by type only
		if (type != null) {
			return categoryRepository.findByUserIsNullAndType(type, pageable);
		}
		// Filter by keyword only
		if (keyword != null && !keyword.trim().isEmpty()) {
			return categoryRepository.findByUserIsNullAndNameContainingIgnoreCase(keyword.trim(), pageable);
		}
		// No filter
		return categoryRepository.findByUserIsNull(pageable);
	}

	@Override
	public Category adminGetGlobalCategory(Integer categoryId) {
		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
		if (category.getUser() != null) {
			throw new RuntimeException("Đây không phải danh mục hệ thống");
		}
		return category;
	}

	@Override
	@Transactional
	@LogActivity(
        action = "CREATE",
        targetEntity = "CATEGORY",
        description = "Created new category"
    )
	public Category adminCreateGlobalCategory(CreateCategoryRequest request) {
		Category category = new Category();
		category.setName(request.getName());
		category.setDescription(request.getDescription());
		category.setType(request.getType());
		category.setIcon(request.getIcon());
		category.setUser(null); // Global category
		category.setDeleted(false);
		return categoryRepository.save(category);
	}

	@Override
	@Transactional
	@LogActivity(
        action = "UPDATE",
        targetEntity = "CATEGORY",
        description = "updated category"
    )
	public Category adminUpdateGlobalCategory(Integer categoryId, CreateCategoryRequest request) {
		Category category = adminGetGlobalCategory(categoryId);
		category.setName(request.getName());
		category.setDescription(request.getDescription());
		category.setType(request.getType());
		if (request.getIcon() != null && !request.getIcon().isBlank()) {
			// // Delete old icon if exists and different from new one
			// if (category.getIcon() != null && !category.getIcon().equals(request.getIcon())) {
			// 	cloudinaryService.deleteIcon(category.getIcon());
			// }
			category.setIcon(request.getIcon());
		}
		return categoryRepository.save(category);
	}

	@Override
	@Transactional
	@LogActivity(
        action = "DELETE",
        targetEntity = "CATEGORY",
        description = "deleted category"
    )
	public void adminSoftDeleteCategory(Integer categoryId) {
		Category category = adminGetGlobalCategory(categoryId);
		// Soft delete - set deleted flag to true
		category.setDeleted(true);
		categoryRepository.save(category);
	}

	@Override
	public byte[] exportCategoriesToCsv() {
		try {
			java.io.ByteArrayInputStream stream = csvService.loadCategoryCsv();
			return stream.readAllBytes();
		} catch (Exception e) {
			throw new RuntimeException("Lỗi khi export CSV: " + e.getMessage());
		}
	}

	@Override
	@Transactional
	public void importCategoriesFromCsv(org.springframework.web.multipart.MultipartFile file) throws Exception {
		csvService.saveCategoriesFromCsv(file);
	}
}
