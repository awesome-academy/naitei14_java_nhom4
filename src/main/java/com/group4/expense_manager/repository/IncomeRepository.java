package com.group4.expense_manager.repository;

import com.group4.expense_manager.entity.Category;
import com.group4.expense_manager.entity.Income;
import com.group4.expense_manager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Integer> {

    // ========================================================================
    // 1. CLIENT METHODS - SEARCH & FILTER (Dành cho người dùng thường)
    // ========================================================================

    /**
     * Tìm kiếm nâng cao cho Client:
     * - Lọc theo Category, Ngày tháng, Từ khóa (Source, Note).
     * - Các tham số có thể NULL (nếu NULL thì bỏ qua điều kiện đó).
     */
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

    // Lấy tất cả income của user (có phân trang)
    Page<Income> findByUser(User user, Pageable pageable);

    // ========================================================================
    // 2. CLIENT METHODS - DASHBOARD & STATISTICS (Thống kê & Biểu đồ)
    // ========================================================================

    // Lấy list income trong khoảng thời gian (Dùng cho biểu đồ, không phân trang)
    List<Income> findByUserAndIncomeDateBetween(User user, LocalDate start, LocalDate end);

    // Tính tổng thu nhập của user trong khoảng thời gian (COALESCE để trả về 0 nếu null)
    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.user = :user AND i.incomeDate BETWEEN :start AND :end")
    BigDecimal sumByUserAndIncomeDateBetween(@Param("user") User user,
                                             @Param("start") LocalDate start,
                                             @Param("end") LocalDate end);

    // Tổng toàn bộ thu nhập từ trước đến nay của user
    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.user.id = :userId")
    double sumAmountByUserId(@Param("userId") Integer userId);

    // ========================================================================
    // 3. SYSTEM METHODS - SCHEDULER (Tự động hóa)
    // ========================================================================

    /**
     * Tìm các khoản thu nhập định kỳ (Recurring) cần được sinh ra vào ngày hôm nay.
     * Logic: isRecurring = true AND nextOccurrenceDate <= today
     */
    List<Income> findByIsRecurringTrueAndNextOccurrenceDateNotNullAndNextOccurrenceDateLessThanEqual(LocalDate date);

    // ========================================================================
    // 4. ADMIN METHODS - GLOBAL MANAGEMENT (Quản trị viên)
    // ========================================================================

    /**
     * Tìm kiếm nâng cao cho Admin (Toàn hệ thống):
     * - userId: Nếu null -> lấy tất cả user. Nếu có -> lọc theo user đó.
     * - keyword: Tìm trong Source hoặc Email người dùng.
     */
    @Query("""
        SELECT i FROM Income i
        WHERE (:userId IS NULL OR i.user.id = :userId)
          AND (:startDate IS NULL OR i.incomeDate >= :startDate)
          AND (:endDate IS NULL OR i.incomeDate <= :endDate)
          AND (
               :keyword IS NULL OR :keyword = '' OR
               LOWER(i.source) LIKE CONCAT('%', LOWER(:keyword), '%') OR
               LOWER(i.user.email) LIKE CONCAT('%', LOWER(:keyword), '%')
          )
        """)
    Page<Income> searchIncomesForAdmin(
            @Param("userId") Integer userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // Filter nhanh theo khoảng thời gian (Bất kể user nào)
    Page<Income> findByIncomeDateBetween(LocalDate start, LocalDate end, Pageable pageable);

}