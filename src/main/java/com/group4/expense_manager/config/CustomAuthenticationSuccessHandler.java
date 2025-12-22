package com.group4.expense_manager.config;

import com.group4.expense_manager.entity.User;
import com.group4.expense_manager.service.ActivityLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.group4.expense_manager.entity.ActivityLog;

import java.io.IOException;
import java.util.Set;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private ActivityLogService activityLogService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        if (roles.contains("ROLE_ADMIN")) {
            if (authentication.getPrincipal() instanceof User user) {
                ActivityLog log = new ActivityLog();
                log.setUser(user);
                log.setAction("LOGIN");
                log.setTargetEntity("User");
//                log.setTargetId();
                log.setDescription("Admin đăng nhập");
                activityLogService.createActivityLog(log);
            }
            response.sendRedirect("/admin/dashboard");
        }
        else {

            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect("/login?error=not_admin");
        }
    }
}