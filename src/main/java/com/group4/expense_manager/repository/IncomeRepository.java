package com.group4.expense_manager.repository;

import com.group4.expense_manager.entity.Category;
import com.group4.expense_manager.entity.Income;
import com.group4.expense_manager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Integer> {

    // --- CLIENT METHODS ---

    // Lấy tất cả income của một user (hoặc Admin dùng để filter theo user)
    // Lưu ý: Method này dùng chung cho cả Client (lấy của chính mình) và Admin (lọc user cụ thể)
    Page<Income> findByUser(User user, Pageable pageable);

    // Lọc theo user + category
    Page<Income> findByUserAndCategory(User user, Category category, Pageable pageable);

    // Lọc theo user + khoảng thời gian
    Page<Income> findByUserAndIncomeDateBetween(User user, LocalDate start, LocalDate end, Pageable pageable);

    // Dùng cho dashboard: lấy tất cả income trong tháng (không cần phân trang)
    List<Income> findByUserAndIncomeDateBetween(User user, LocalDate start, LocalDate end);

    // Lọc theo user + category + khoảng thời gian
    Page<Income> findByUserAndCategoryAndIncomeDateBetween(
            User user,
            Category category,
            LocalDate start,
            LocalDate end,
            Pageable pageable
    );

    // --- ADMIN METHODS ---

    // Admin: lấy income của toàn hệ thống
    // Page<Income> findAll(Pageable pageable); -> Method này đã có sẵn trong JpaRepository, không cần khai báo lại trừ khi muốn override.

    // Admin: filter theo date range (bất kể user nào)
    Page<Income> findByIncomeDateBetween(LocalDate start, LocalDate end, Pageable pageable);

    // --- AGGREGATION ---
    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.user.id = :userId")
    double sumAmountByUserId(@Param("userId") Integer userId);

    // Tổng thu nhập user trong khoảng thời gian (COALESCE để tránh null)
    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.user = :user AND i.incomeDate BETWEEN :start AND :end")
    BigDecimal sumByUserAndIncomeDateBetween(@Param("user") User user,
                                             @Param("start") LocalDate start,
                                             @Param("end") LocalDate end);
}