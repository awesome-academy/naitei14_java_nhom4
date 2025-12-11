package com.group4.expense_manager.repository;

import com.group4.expense_manager.entity.Category;
import com.group4.expense_manager.entity.CategoryType;
import com.group4.expense_manager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
	Page<Category> findByUserIsNullOrUser(User user, Pageable pageable);
	Page<Category> findByTypeAndUserIsNullOrUser(CategoryType type, User user, Pageable pageable);
	
	// Admin methods - Global categories only
	Page<Category> findByUserIsNull(Pageable pageable);
	Page<Category> findByUserIsNullAndType(CategoryType type, Pageable pageable);
	Page<Category> findByUserIsNullAndNameContainingIgnoreCase(String keyword, Pageable pageable);
	Page<Category> findByUserIsNullAndTypeAndNameContainingIgnoreCase(CategoryType type, String keyword, Pageable pageable);
}