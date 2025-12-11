package com.group4.expense_manager.repository;

import com.group4.expense_manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:isActive IS NULL OR u.isActive = :isActive)")
    Page<User> searchUsers(@Param("keyword") String keyword,
                           @Param("isActive") Boolean isActive,
                           Pageable pageable);
    //Tìm user theo role (không phân biệt hoa thường)
    List<User> findByRoleIgnoreCase(String role);
}