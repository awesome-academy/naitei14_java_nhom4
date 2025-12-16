package com.group4.expense_manager.mapper;

import com.group4.expense_manager.dto.response.ActivityLogResponse;
import com.group4.expense_manager.entity.ActivityLog;
import org.springframework.stereotype.Component;

@Component
public class ActivityLogMapper {
    
    public ActivityLogResponse toResponse(ActivityLog activityLog) {
        if (activityLog == null) {
            return null;
        }
        
        ActivityLogResponse response = new ActivityLogResponse();
        response.setId(activityLog.getId());
        response.setAction(activityLog.getAction());
        response.setTargetEntity(activityLog.getTargetEntity());
        response.setTargetId(activityLog.getTargetId());
        response.setDescription(activityLog.getDescription());
        response.setCreatedAt(activityLog.getCreatedAt());
        
        if (activityLog.getUser() != null) {
            response.setUserId(activityLog.getUser().getId());
        }
        
        return response;
    }
}
