package com.group4.expense_manager.service;

import com.group4.expense_manager.entity.Income;
import com.group4.expense_manager.entity.Budget;
import com.group4.expense_manager.entity.User;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public interface DashboardService {

	BigDecimal getTotalIncomeOfMonth(User user, YearMonth month);

	List<Income> getIncomesOfMonth(User user, YearMonth month);

	List<Budget> getActiveBudgetsOfMonth(User user, YearMonth month);
}
