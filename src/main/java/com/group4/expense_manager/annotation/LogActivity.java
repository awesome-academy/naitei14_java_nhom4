package com.group4.expense_manager.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation để đánh dấu các method cần ghi log activity tự động.
 * Sử dụng với AOP để tự động ghi log sau khi method thực thi thành công.
 * 
 * Cách sử dụng:
 * - CREATE: Method phải return về Entity đã được lưu (có ID)
 * - UPDATE/DELETE: Tham số đầu tiên của method phải là ID của entity
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogActivity {
    
    /**
     * Hành động được thực hiện (CREATE, UPDATE, DELETE, etc.)
     */
    String action();
    
    /**
     * Loại đối tượng bị tác động (EXPENSE, INCOME, BUDGET, CATEGORY, etc.)
     */
    String targetEntity();
    
    /**
     * Mô tả chi tiết về hành động (có thể sử dụng placeholder {0}, {1}... cho tham số)
     */
    String description() default "";
}
