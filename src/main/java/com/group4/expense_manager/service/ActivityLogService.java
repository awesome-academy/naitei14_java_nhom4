package com.group4.expense_manager.service;

import com.group4.expense_manager.dto.response.ActivityLogResponse;
import com.group4.expense_manager.entity.ActivityLog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;



public interface ActivityLogService {
    
    // Lấy tất cả activity logs
    Page<ActivityLogResponse> getAllActivityLogs(Pageable pageable);
    
    // Lấy activity log theo ID
    ActivityLogResponse getActivityLogById(Long id);
    
    // Lấy danh sách activity log của một user
    Page<ActivityLogResponse> getActivityLogsByUserId(Long userId, Pageable pageable);
    
    // Lấy danh sách activity log với filters kết hợp (userId, entityType, action, role, dateRange)
    // Truyền null cho tham số nào không muốn filter
    Page<ActivityLogResponse> getActivityLogsByFilters(Long userId, String entityType, String action, 
                                                        java.time.LocalDateTime startDate, 
                                                        java.time.LocalDateTime endDate, Pageable pageable);
    
    // Tạo activity log mới (cho internal use - từ AOP)
    ActivityLog createActivityLog(ActivityLog activityLog);
    
    // Xóa activity log theo ID
    void deleteActivityLog(Long id);
    
}
