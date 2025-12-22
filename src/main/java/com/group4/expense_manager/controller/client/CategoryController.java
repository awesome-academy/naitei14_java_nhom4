package com.group4.expense_manager.controller.client;

import com.group4.expense_manager.dto.request.CreateCategoryRequest;
import com.group4.expense_manager.dto.response.CategoryResponse;
import com.group4.expense_manager.entity.Category;
import com.group4.expense_manager.entity.CategoryType;
import com.group4.expense_manager.entity.User;
import com.group4.expense_manager.mapper.CategoryMapper;
import com.group4.expense_manager.service.CategoryService;
import com.group4.expense_manager.service.CloudinaryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private CategoryMapper categoryMapper;

	@Autowired
	private CloudinaryService cloudinaryService;

	// LIST (Optional filter by type)
	@GetMapping
	public ResponseEntity<Page<CategoryResponse>> listCategories(
			@AuthenticationPrincipal User user,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) CategoryType type
	) {
		System.out.println("Listing categories for user: " + user.getUsername());
		Pageable pageable = PageRequest.of(page, size);
		Page<Category> categories = categoryService.listCategories(user, type, pageable);
		return ResponseEntity.ok(categories.map(categoryMapper::toResponse));
	}

	// GET DETAIL
	@GetMapping("/{id}")
	public ResponseEntity<CategoryResponse> getCategory(
			@PathVariable Integer id,
			@AuthenticationPrincipal User user
	) {
		Category category = categoryService.getCategory(id, user);
		return ResponseEntity.ok(categoryMapper.toResponse(category));
	}


	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<CategoryResponse> createCategory(
			@AuthenticationPrincipal User user,
			@RequestParam("name") String name,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam("type") CategoryType type, // Spring sẽ tự convert chuỗi sang Enum
			@RequestParam(value = "iconFile", required = false) MultipartFile iconFile
	) {
		String iconUrl = "";
		if (iconFile != null && !iconFile.isEmpty()) {
			iconUrl = cloudinaryService.uploadIcon(iconFile, "categories");
		}
		CreateCategoryRequest request = new CreateCategoryRequest();
		request.setName(name);
		request.setDescription(description);
		request.setType(type);
		request.setIcon(iconUrl);
		Category category = categoryService.createCategory(user, request);
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(categoryMapper.toResponse(category));
	}


	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<CategoryResponse> updateCategory(
			@PathVariable Integer id,
			@AuthenticationPrincipal User user,
			@RequestParam("name") String name,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam("type") CategoryType type,
			@RequestParam(value = "iconFile", required = false) MultipartFile iconFile
	) {
		CreateCategoryRequest request = new CreateCategoryRequest();
		request.setName(name);
		request.setDescription(description);
		request.setType(type);
		// Upload icon mới nếu có
		if (iconFile != null && !iconFile.isEmpty()) {
			String iconUrl = cloudinaryService.uploadIcon(iconFile, "categories");
			request.setIcon(iconUrl);
		}

		Category category = categoryService.updateCategory(id, user, request);
		return ResponseEntity.ok(categoryMapper.toResponse(category));
	}


	// DELETE (Only user-owned)
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteCategory(
			@PathVariable Integer id,
			@AuthenticationPrincipal User user
	) {
		categoryService.deleteCategory(id, user);
		return ResponseEntity.noContent().build();
	}
}
