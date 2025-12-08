package com.group4.expense_manager.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component("urlUtils")
public class UrlUtils {

    public String getRequestURI() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getRequestURI();
    }

    public boolean isActive(String path) {
        String currentUri = getRequestURI();
        return currentUri.startsWith(path);
    }
}