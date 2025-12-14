package com.group4.expense_manager.repository;

import com.group4.expense_manager.entity.Category;
import com.group4.expense_manager.entity.CategoryType;
import com.group4.expense_manager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    @Query("SELECT c FROM Category c WHERE c.name = :name AND c.user.id = :userId")
    Optional<Category> findByNameAndUserId(@Param("name") String name, @Param("userId") Integer userId);

    @Query("SELECT c FROM Category c WHERE c.name = :name AND c.user.id = :userId AND c.type = :type")
    Optional<Category> findByNameAndUserIdAndType(
            @Param("name") String name,
            @Param("userId") Integer userId,
            @Param("type") CategoryType type
    );

	Page<Category> findByUserIsNullOrUser(User user, Pageable pageable);
	Page<Category> findByTypeAndUserIsNullOrUser(CategoryType type, User user, Pageable pageable);


	// Lấy danh sách category theo type (cho admin)
	List<Category> findByType(CategoryType type);

	// Admin methods - Global categories only
	Page<Category> findByUserIsNull(Pageable pageable);
	Page<Category> findByUserIsNullAndType(CategoryType type, Pageable pageable);
	Page<Category> findByUserIsNullAndNameContainingIgnoreCase(String keyword, Pageable pageable);
	Page<Category> findByUserIsNullAndTypeAndNameContainingIgnoreCase(CategoryType type, String keyword, Pageable pageable);
}