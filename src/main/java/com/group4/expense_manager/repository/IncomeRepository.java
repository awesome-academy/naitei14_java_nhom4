package com.group4.expense_manager.repository;

import com.group4.expense_manager.entity.Category;
import com.group4.expense_manager.entity.Income;
import com.group4.expense_manager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable; // SỬA: Import đúng thư viện này
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

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

    // Lọc theo user + category + khoảng thời gian
    Page<Income> findByUserAndCategoryAndIncomeDateBetween(
            User user,
            Category category,
            LocalDate start,
            LocalDate end,
            Pageable pageable
    );

    @Query("""
        SELECT i FROM Income i
        WHERE i.user = :user
          AND (:category IS NULL OR i.category = :category)
          AND (:start IS NULL OR i.incomeDate >= :start)
          AND (:end IS NULL OR i.incomeDate <= :end)
          AND (
               :keyword IS NULL OR :keyword = '' OR
               LOWER(i.source) LIKE CONCAT('%', LOWER(:keyword), '%') OR
               (i.note IS NOT NULL AND LOWER(i.note) LIKE CONCAT('%', LOWER(:keyword), '%'))
          )
        """)
    Page<Income> searchIncomes(
            @Param("user") User user,
            @Param("category") Category category,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    //Tìm các income recurring mà tới hạn sinh thêm giao dịch
    List<Income> findByIsRecurringTrueAndNextOccurrenceDateNotNullAndNextOccurrenceDateLessThanEqual(LocalDate date);


    // --- ADMIN METHODS ---

    // Admin: lấy income của toàn hệ thống
    // Page<Income> findAll(Pageable pageable); -> Method này đã có sẵn trong JpaRepository, không cần khai báo lại trừ khi muốn override.

    // Admin: filter theo date range (bất kể user nào)
    Page<Income> findByIncomeDateBetween(LocalDate start, LocalDate end, Pageable pageable);
}