package com.group4.expense_manager.aspect;

import com.group4.expense_manager.annotation.LogActivity;
import com.group4.expense_manager.entity.ActivityLog;
import com.group4.expense_manager.entity.User;
import com.group4.expense_manager.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class LogActivityAspect {
    
    private final ActivityLogRepository activityLogRepository;
    
    @AfterReturning(
        pointcut = "@annotation(com.group4.expense_manager.annotation.LogActivity)",
        returning = "result"
    )
    public void logActivity(JoinPoint joinPoint, Object result) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            LogActivity logActivity = method.getAnnotation(LogActivity.class);
            
            if (logActivity == null) {
                return;
            }
            
            // Lấy user hiện tại từ SecurityContext
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                log.warn("Cannot log activity: No authenticated user found");
                return;
            }
            
            // Lấy target ID dựa vào action
            Integer targetId = extractTargetId(logActivity.action(), joinPoint.getArgs(), result);
            
            // Tạo ActivityLog
            ActivityLog activityLog = new ActivityLog();
            activityLog.setUser(currentUser);
            activityLog.setAction(logActivity.action());
            activityLog.setTargetEntity(logActivity.targetEntity());
            activityLog.setTargetId(targetId);
            activityLog.setDescription(logActivity.description());

            // In biến activityLog để theo dõi giá trị
            log.info("Activity logged: {} {} {} (ID: {})", 
                logActivity.action(), logActivity.targetEntity(), logActivity.description(), targetId);
            
            // Lưu activity log
            activityLogRepository.save(activityLog);
            
            
            
        } catch (Exception e) {
            log.error("Error logging activity", e);
        }
    }
    
    /**
     * Lấy user hiện tại từ SecurityContext
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        return null;
    }
    
    /**
     * Trích xuất target ID:
     * - CREATE: Lấy ID từ object được return (phải có method getId())
     * - UPDATE/DELETE: Lấy từ argument đầu tiên (giả sử là ID)
     */
    private Integer extractTargetId(String action, Object[] args, Object result) {
        try {
            if ("CREATE".equalsIgnoreCase(action)) {
                // Với CREATE, lấy ID từ object được return
                if (result != null) {
                    Method getIdMethod = result.getClass().getMethod("getId");
                    Object id = getIdMethod.invoke(result);
                    if (id instanceof Long) {
                        return ((Long) id).intValue();
                    } else if (id instanceof Integer) {
                        return (Integer) id;
                    }
                }
            } else if (("UPDATE".equalsIgnoreCase(action) || "DELETE".equalsIgnoreCase(action)) && args.length > 0) {
                // Với UPDATE/DELETE, lấy từ argument đầu tiên
                Object firstArg = args[0];
                if (firstArg instanceof Long) {
                    return ((Long) firstArg).intValue();
                } else if (firstArg instanceof Integer) {
                    return (Integer) firstArg;
                }
            }
        } catch (Exception e) {
            log.warn("Cannot extract target ID", e);
        }
        return null;
    }
    
}
