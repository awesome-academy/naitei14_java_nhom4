package com.group4.expense_manager.repository;

import com.group4.expense_manager.entity.ActivityLog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    // Query tùy chỉnh để tìm kiếm linh hoạt với nhiều filters
    @Query("SELECT al FROM ActivityLog al WHERE " +
           "(:userId IS NULL OR al.user.id = :userId) AND " +
           "(:entityType IS NULL OR :entityType = '' OR al.targetEntity = :entityType) AND " +
           "(:action IS NULL OR :action = '' OR al.action = :action) AND " +
           "(:startDate IS NULL OR al.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR al.createdAt <= :endDate) " +
           "ORDER BY al.createdAt DESC")
    Page<ActivityLog> findByFilters(
            @Param("userId") Long userId,
            @Param("entityType") String entityType,
            @Param("action") String action,
            @Param("startDate") java.time.Instant startDate,
            @Param("endDate") java.time.Instant endDate,
            Pageable pageable);
}
