package com.group4.expense_manager.repository;

import com.group4.expense_manager.entity.Budget;
import com.group4.expense_manager.entity.Category;
import com.group4.expense_manager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Integer> {

    // Existing method for dashboard
    List<Budget> findByUserAndStartDateLessThanEqualAndEndDateGreaterThanEqual(User user,
                                                                              LocalDate end,
                                                                              LocalDate start);
    
    // New methods for CRUD operations
    Page<Budget> findByUser(User user, Pageable pageable);
    
    Page<Budget> findByUserAndCategory(User user, Category category, Pageable pageable);
    
    Page<Budget> findByUserAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
        User user, LocalDate end, LocalDate start, Pageable pageable);
    
    Page<Budget> findByUserAndCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
        User user, Category category, LocalDate end, LocalDate start, Pageable pageable);
    
    // ========================================================================
    // ADMIN METHODS - Search all budgets across all users
    // ========================================================================
    @Query("""
        SELECT b FROM Budget b
        WHERE (:user IS NULL OR b.user = :user)
          AND (:start IS NULL OR b.startDate >= :start)
          AND (:end IS NULL OR b.endDate <= :end)
          AND (
               :keyword IS NULL OR :keyword = '' OR
               LOWER(b.category.name) LIKE CONCAT('%', LOWER(:keyword), '%') OR
               LOWER(b.user.name) LIKE CONCAT('%', LOWER(:keyword), '%') OR
               LOWER(b.user.email) LIKE CONCAT('%', LOWER(:keyword), '%')
          )
        """)
    Page<Budget> searchBudgetsForAdmin(
            @Param("user") User user,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
