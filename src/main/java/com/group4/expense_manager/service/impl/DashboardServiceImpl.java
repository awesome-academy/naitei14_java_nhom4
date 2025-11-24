package com.group4.expense_manager.service.impl;

import com.group4.expense_manager.entity.Income;
import com.group4.expense_manager.entity.Budget;
import com.group4.expense_manager.entity.User;
import com.group4.expense_manager.repository.IncomeRepository;
import com.group4.expense_manager.repository.BudgetRepository;
import com.group4.expense_manager.service.DashboardService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

	private final IncomeRepository incomeRepository;
	private final BudgetRepository budgetRepository;

	public DashboardServiceImpl(IncomeRepository incomeRepository,
								BudgetRepository budgetRepository) {
		this.incomeRepository = incomeRepository;
		this.budgetRepository = budgetRepository;
	}

	@Override
	public BigDecimal getTotalIncomeOfMonth(User user, YearMonth month) {
		LocalDate start = month.atDay(1);
		LocalDate end = month.atEndOfMonth();
		BigDecimal sum = incomeRepository.sumByUserAndIncomeDateBetween(user, start, end);
		return sum != null ? sum : BigDecimal.ZERO;
	}

	@Override
	public List<Income> getIncomesOfMonth(User user, YearMonth month) {
		LocalDate start = month.atDay(1);
		LocalDate end = month.atEndOfMonth();
		return incomeRepository.findByUserAndIncomeDateBetween(user, start, end);
	}

	@Override
	public List<Budget> getActiveBudgetsOfMonth(User user, YearMonth month) {
		LocalDate start = month.atDay(1);
		LocalDate end = month.atEndOfMonth();
		return budgetRepository.findByUserAndStartDateLessThanEqualAndEndDateGreaterThanEqual(user, end, start);
	}
}
