package com.group4.expense_manager.repository;

import com.group4.expense_manager.entity.BudgetTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetTemplateRepository extends JpaRepository<BudgetTemplate, Integer> {
    
    // Tìm kiếm theo tên
    Page<BudgetTemplate> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    // Tìm kiếm theo tháng
    Page<BudgetTemplate> findByMonth(int month, Pageable pageable);
    
    // Tìm kiếm kết hợp: theo tên và tháng
    Page<BudgetTemplate> findByNameContainingIgnoreCaseAndMonth(String name, int month, Pageable pageable);
    
    // Lấy tất cả template kèm theo items (FETCH JOIN để tránh N+1)
    @Query("SELECT DISTINCT t FROM BudgetTemplate t LEFT JOIN FETCH t.items i LEFT JOIN FETCH i.category WHERE t.id = :id")
    Optional<BudgetTemplate> findByIdWithItems(@Param("id") Integer id);
    
    // Lấy danh sách template theo tháng
    List<BudgetTemplate> findByMonth(int month);

    @Query("""
        SELECT bt FROM BudgetTemplate bt
        WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(bt.name) LIKE CONCAT('%', LOWER(:keyword), '%'))
          AND (:month IS NULL OR bt.month = :month)
        """)
    Page<BudgetTemplate> searchTemplates(
            @Param("keyword") String keyword,
            @Param("month") Integer month,
            Pageable pageable
    );
}
