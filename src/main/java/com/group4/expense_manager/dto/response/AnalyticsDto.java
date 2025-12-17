package com.group4.expense_manager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

public class AnalyticsDto {

    // 1. DTO cho Báo cáo theo Tháng/Quý/Năm
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExpenseReport {
        private String timeLabel;    // "January", "Q1", "2025"
        private BigDecimal totalAmount;
    }

    // 2. DTO cho Biểu đồ phân bố danh mục (Pie Chart)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryDistribution {
        private String categoryName;
        private BigDecimal amount;
        private Double percentage;   // % chiếm dụng
    }

    // 3. DTO cho So sánh Thu nhập - Chi tiêu (Summary)
    @Data
    @Builder
    public static class FinancialSummary {
        private BigDecimal totalIncome;
        private BigDecimal totalExpense;
        private BigDecimal netBalance; // Thu - Chi
    }

    // 4. DTO cho Xu hướng theo thời gian (Line Chart)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MonthlyTrend {
        private String month;         // "2025-01", "2025-02"...
        private BigDecimal income;
        private BigDecimal expense;
    }
}