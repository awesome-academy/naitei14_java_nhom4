package com.group4.expense_manager.service;

import com.group4.expense_manager.dto.request.CreateBudgetRequest;
import com.group4.expense_manager.entity.Budget;
import com.group4.expense_manager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.YearMonth;

public interface BudgetService {
    Page<Budget> listBudgets(User user, Integer categoryId, YearMonth month, Pageable pageable);
    Budget getBudget(Integer budgetId, User user);
    Budget createBudget(User user, CreateBudgetRequest request);
    Budget updateBudget(Integer budgetId, User user, CreateBudgetRequest request);
    void deleteBudget(Integer budgetId, User user);
}