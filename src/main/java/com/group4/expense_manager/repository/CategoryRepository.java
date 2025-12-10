package com.group4.expense_manager.repository;

import com.group4.expense_manager.entity.Category;
import com.group4.expense_manager.entity.CategoryType;
import com.group4.expense_manager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
	Page<Category> findByUserIsNullOrUser(User user, Pageable pageable);
	Page<Category> findByTypeAndUserIsNullOrUser(CategoryType type, User user, Pageable pageable);
	
	// Lấy danh sách category theo type (cho admin)
	List<Category> findByType(CategoryType type);
}