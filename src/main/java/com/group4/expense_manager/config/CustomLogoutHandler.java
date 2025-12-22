package com.group4.expense_manager.config;

import com.group4.expense_manager.entity.ActivityLog;
import com.group4.expense_manager.entity.User;
import com.group4.expense_manager.service.ActivityLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private final ActivityLogService activityLogService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            // Tạo activity log
            ActivityLog log = new ActivityLog();
            log.setUser(user);
            log.setAction("LOGOUT");
            log.setTargetEntity("User");
//            log.setTargetId(user.getId());
            log.setDescription("Đăng xuất");
            
            activityLogService.createActivityLog(log);
        }
    }
}
