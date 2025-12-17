package com.group4.expense_manager.repository;

import com.group4.expense_manager.entity.Category;
import com.group4.expense_manager.entity.Expense;
import com.group4.expense_manager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Integer> {

    // ADMIN FILTER BY CATEGORY
    Page<Expense> findByUserIdAndCategoryIdAndExpenseDateBetweenAndDescriptionContaining(Integer userId, Integer categoryId, LocalDate fromDate, LocalDate toDate, String description, Pageable pageable);
    Page<Expense> findByUserIdAndCategoryIdAndExpenseDateBetween(Integer userId, Integer categoryId, LocalDate fromDate, LocalDate toDate, Pageable pageable);
    Page<Expense> findByUserIdAndCategoryId(Integer userId, Integer categoryId, Pageable pageable);
    Page<Expense> findByCategoryIdAndExpenseDateBetween(Integer categoryId, LocalDate fromDate, LocalDate toDate, Pageable pageable);
    Page<Expense> findByCategoryId(Integer categoryId, Pageable pageable);

    Page<Expense> findByUser(User user, Pageable pageable);

    Page<Expense> findByUserAndDescriptionContainingAndAmountBetween(User user, String description, Double minAmount, Double maxAmount, Pageable pageable);

    Page<Expense> findByUserAndAmountBetween(User user, Double minAmount, Double maxAmount, Pageable pageable);

    Page<Expense> findByUserAndCategory(User user, Category category, Pageable pageable);

    Page<Expense> findByUserAndExpenseDateBetween(User user, LocalDate fromDate, LocalDate toDate, Pageable pageable);

    Page<Expense> findByUserAndCategoryAndExpenseDateBetween(User user, Category category, LocalDate fromDate, LocalDate toDate, Pageable pageable);

    Optional<Expense> findByIdAndUser(Integer id, User user);

    List<Expense> findByUserAndCategory(User user, Category category);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId")
    double sumAmountByUserId(@Param("userId") Integer userId);
    List<Expense> findByUser(User user);

    long countByUser(User user);

    // ADMIN QUERIES
    Page<Expense> findByUserIdAndExpenseDateBetweenAndDescriptionContaining(Integer userId, LocalDate fromDate, LocalDate toDate, String description, Pageable pageable);

    Page<Expense> findByUserIdAndExpenseDateBetween(Integer userId, LocalDate fromDate, LocalDate toDate, Pageable pageable);

    Page<Expense> findByUserId(Integer userId, Pageable pageable);

    Page<Expense> findByExpenseDateBetween(LocalDate fromDate, LocalDate toDate, Pageable pageable);

    Page<Expense> findByDescriptionContaining(String description, Pageable pageable);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE " +
            "e.user.id = :userId AND " +
            "e.category.id = :categoryId AND " +
            "e.expenseDate BETWEEN :startDate AND :endDate")
    Optional<BigDecimal> sumExpensesByBudgetPeriod(
            @Param("userId") Integer userId,
            @Param("categoryId") Integer categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // --- 1. Báo cáo Chi tiêu (Tháng/Quý/Năm) ---
    @Query("SELECT MONTH(e.expenseDate), SUM(e.amount) FROM Expense e WHERE e.user.id = :userId AND YEAR(e.expenseDate) = :year GROUP BY MONTH(e.expenseDate) ORDER BY MONTH(e.expenseDate)")
    List<Object[]> findMonthlyExpenses(@Param("userId") Integer userId, @Param("year") int year);

    @Query("SELECT QUARTER(e.expenseDate), SUM(e.amount) FROM Expense e WHERE e.user.id = :userId AND YEAR(e.expenseDate) = :year GROUP BY QUARTER(e.expenseDate) ORDER BY QUARTER(e.expenseDate)")
    List<Object[]> findQuarterlyExpenses(@Param("userId") Integer userId, @Param("year") int year);

    @Query("SELECT YEAR(e.expenseDate), SUM(e.amount) FROM Expense e WHERE e.user.id = :userId GROUP BY YEAR(e.expenseDate) ORDER BY YEAR(e.expenseDate)")
    List<Object[]> findYearlyExpenses(@Param("userId") Integer userId);

    // --- 2. Phân bố theo Danh mục ---
    @Query("SELECT e.category.name, SUM(e.amount) FROM Expense e WHERE e.user.id = :userId AND e.expenseDate BETWEEN :startDate AND :endDate GROUP BY e.category.name")
    List<Object[]> getExpenseDistribution(@Param("userId") Integer userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // --- Hỗ trợ tính tổng ---
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId AND e.expenseDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalExpense(@Param("userId") Integer userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // --- 4. Xu hướng (Trend) ---
    @Query("SELECT MONTH(e.expenseDate), SUM(e.amount) FROM Expense e WHERE e.user.id = :userId AND YEAR(e.expenseDate) = :year GROUP BY MONTH(e.expenseDate)")
    List<Object[]> getMonthlyExpenseTrend(@Param("userId") Integer userId, @Param("year") int year);
}