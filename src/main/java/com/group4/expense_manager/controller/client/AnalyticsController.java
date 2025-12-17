package com.group4.expense_manager.controller.client;

import com.group4.expense_manager.dto.response.AnalyticsDto.*;
import com.group4.expense_manager.entity.User;
import com.group4.expense_manager.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired private ReportService reportService;

    // 1. Báo cáo Chi tiêu (Tháng, Quý, Năm)
    // URL: /api/analytics/report?type=MONTHLY&year=2025
    @GetMapping("/report")
    public ResponseEntity<List<ExpenseReport>> getExpenseReport(
            @AuthenticationPrincipal User user,
            @RequestParam ReportService.ReportType type,
            @RequestParam(defaultValue = "2025") int year
    ) {
        return ResponseEntity.ok(reportService.getExpenseReport(user.getId(), type, year));
    }

    // 2. Biểu đồ Phân bố danh mục
    // URL: /api/analytics/category-distribution?startDate=2025-01-01&endDate=2025-01-31
    @GetMapping("/category-distribution")
    public ResponseEntity<List<CategoryDistribution>> getCategoryDistribution(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(reportService.getCategoryDistribution(user.getId(), startDate, endDate));
    }

    // 3. So sánh Thu nhập vs Chi tiêu
    // URL: /api/analytics/summary?startDate=2025-01-01&endDate=2025-12-31
    @GetMapping("/summary")
    public ResponseEntity<FinancialSummary> getSummary(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(reportService.getFinancialSummary(user.getId(), startDate, endDate));
    }

    // 4. Xu hướng chi tiêu theo thời gian (Trend)
    // URL: /api/analytics/trend?year=2025
    @GetMapping("/trend")
    public ResponseEntity<List<MonthlyTrend>> getTrend(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "2025") int year
    ) {
        return ResponseEntity.ok(reportService.getTrendAnalysis(user.getId(), year));
    }
}