package com.group4.expense_manager.config;

import com.group4.expense_manager.entity.ActivityLog;
import com.group4.expense_manager.entity.User;
import com.group4.expense_manager.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationEventListener {

    private final ActivityLogService activityLogService;

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        // if (event.getAuthentication() instanceof UsernamePasswordAuthenticationToken){
        //     Object principal = event.getAuthentication().getPrincipal();

        //     if (principal instanceof User user) {
        //         // Tạo activity log
        //         ActivityLog log = new ActivityLog();
        //         log.setUser(user);
        //         log.setAction("LOGIN");
        //         log.setTargetEntity("User");
        //         log.setTargetId(user.getId());
        //         log.setDescription("Đã đăng nhập");

        //         activityLogService.createActivityLog(log);
        //     }
        // }

    }
}