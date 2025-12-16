package com.group4.expense_manager.service.impl;

import com.group4.expense_manager.dto.response.ActivityLogResponse;
import com.group4.expense_manager.entity.ActivityLog;
import com.group4.expense_manager.mapper.ActivityLogMapper;
import com.group4.expense_manager.repository.ActivityLogRepository;
import com.group4.expense_manager.service.ActivityLogService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ActivityLogMapper activityLogMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogResponse> getAllActivityLogs(Pageable pageable) {
        return activityLogRepository.findByFilters(null, "", "", "", null, null, pageable)
                .map(activityLogMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityLogResponse getActivityLogById(Long id) {
        return activityLogRepository.findById(id)
                .map(activityLogMapper::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogResponse> getActivityLogsByUserId(Long userId, Pageable pageable) {
        return activityLogRepository.findByFilters(userId, "", "", "", null, null, pageable)
                .map(activityLogMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogResponse> getActivityLogsByFilters(Long userId, String entityType, String action, 
                                                                String role, java.time.LocalDateTime startDate, 
                                                                java.time.LocalDateTime endDate, Pageable pageable) {
        // Chuyển null thành empty string để query hoạt động đúng
        String normalizedEntityType = (entityType != null && !entityType.trim().isEmpty()) ? entityType : "";
        String normalizedAction = (action != null && !action.trim().isEmpty()) ? action : "";
        String normalizedRole = (role != null && !role.trim().isEmpty()) ? role : "";
        
        // Chuyển đổi LocalDateTime sang Instant
        java.time.Instant startInstant = startDate != null ? startDate.atZone(java.time.ZoneId.systemDefault()).toInstant() : null;
        java.time.Instant endInstant = endDate != null ? endDate.atZone(java.time.ZoneId.systemDefault()).toInstant() : null;
        
        return activityLogRepository.findByFilters(userId, normalizedEntityType, normalizedAction, 
                                                    normalizedRole, startInstant, endInstant, pageable)
                .map(activityLogMapper::toResponse);
    }

    @Override
    public ActivityLog createActivityLog(ActivityLog activityLog) {
        return activityLogRepository.save(activityLog);
    }

    @Override
    public void deleteActivityLog(Long id) {
        activityLogRepository.deleteById(id);
    }

}
