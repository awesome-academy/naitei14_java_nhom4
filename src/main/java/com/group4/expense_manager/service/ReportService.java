package com.group4.expense_manager.service;

import com.group4.expense_manager.dto.response.AnalyticsDto.*;
import com.group4.expense_manager.repository.ExpenseRepository;
import com.group4.expense_manager.repository.IncomeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired private ExpenseRepository expenseRepository;
    @Autowired private IncomeRepository incomeRepository;

    public enum ReportType { MONTHLY, QUARTERLY, YEARLY }

    // --- 1. Báo cáo Chi tiêu (Tháng/Quý/Năm) ---
    public List<ExpenseReport> getExpenseReport(Integer userId, ReportType type, int year) {
        List<Object[]> results;
        List<ExpenseReport> report = new ArrayList<>();

        switch (type) {
            case MONTHLY:
                results = expenseRepository.findMonthlyExpenses(userId, year);
                for (Object[] row : results) {
                    String month = Month.of((int) row[0]).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                    report.add(new ExpenseReport(month, (BigDecimal) row[1]));
                }
                break;
            case QUARTERLY:
                results = expenseRepository.findQuarterlyExpenses(userId, year);
                for (Object[] row : results) {
                    report.add(new ExpenseReport("Q" + row[0], (BigDecimal) row[1]));
                }
                break;
            case YEARLY:
                results = expenseRepository.findYearlyExpenses(userId);
                for (Object[] row : results) {
                    report.add(new ExpenseReport(String.valueOf(row[0]), (BigDecimal) row[1]));
                }
                break;
        }
        return report;
    }

    // --- 2. Biểu đồ Phân bố danh mục ---
    public List<CategoryDistribution> getCategoryDistribution(Integer userId, LocalDate start, LocalDate end) {
        List<Object[]> results = expenseRepository.getExpenseDistribution(userId, start, end);
        BigDecimal total = results.stream()
                .map(r -> (BigDecimal) r[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategoryDistribution> dtos = new ArrayList<>();
        for (Object[] row : results) {
            String catName = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            double percent = total.compareTo(BigDecimal.ZERO) > 0
                    ? amount.divide(total, 4, RoundingMode.HALF_UP).doubleValue() * 100
                    : 0.0;
            dtos.add(new CategoryDistribution(catName, amount, percent));
        }
        return dtos;
    }

    // --- 3. So sánh Thu nhập và Chi tiêu ---
    public FinancialSummary getFinancialSummary(Integer userId, LocalDate start, LocalDate end) {
        BigDecimal income = incomeRepository.getTotalIncome(userId, start, end);
        BigDecimal expense = expenseRepository.getTotalExpense(userId, start, end);

        if (income == null) income = BigDecimal.ZERO;
        if (expense == null) expense = BigDecimal.ZERO;

        return FinancialSummary.builder()
                .totalIncome(income)
                .totalExpense(expense)
                .netBalance(income.subtract(expense))
                .build();
    }

    // --- 4. Xu hướng chi tiêu theo thời gian (12 tháng) ---
    public List<MonthlyTrend> getTrendAnalysis(Integer userId, int year) {
        Map<Integer, BigDecimal> incomeMap = listToMap(incomeRepository.getMonthlyIncomeTrend(userId, year));
        Map<Integer, BigDecimal> expenseMap = listToMap(expenseRepository.getMonthlyExpenseTrend(userId, year));

        List<MonthlyTrend> trends = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            String label = year + "-" + String.format("%02d", m);
            BigDecimal inc = incomeMap.getOrDefault(m, BigDecimal.ZERO);
            BigDecimal exp = expenseMap.getOrDefault(m, BigDecimal.ZERO);
            trends.add(new MonthlyTrend(label, inc, exp));
        }
        return trends;
    }

    private Map<Integer, BigDecimal> listToMap(List<Object[]> list) {
        return list.stream().collect(Collectors.toMap(
                row -> (Integer) row[0],
                row -> (BigDecimal) row[1]
        ));
    }
}