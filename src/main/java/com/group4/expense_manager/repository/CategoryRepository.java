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

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    @Query("SELECT c FROM Category c WHERE c.name = :name AND c.user.id = :userId")
    Optional<Category> findByNameAndUserId(@Param("name") String name, @Param("userId") Integer userId);

	Page<Category> findByUserIsNullOrUser(User user, Pageable pageable);
	Page<Category> findByTypeAndUserIsNullOrUser(CategoryType type, User user, Pageable pageable);

}