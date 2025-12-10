package com.group4.expense_manager.service;

import com.group4.expense_manager.dto.request.CreateBudgetRequest;
import com.group4.expense_manager.entity.Budget;
import com.group4.expense_manager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.YearMonth;

public interface BudgetService {
    // ========================================================================
    // CLIENT METHODS
    // ========================================================================
    Page<Budget> listBudgets(User user, Integer categoryId, YearMonth month, Pageable pageable);
    Budget getBudget(Integer budgetId, User user);
    Budget createBudget(User user, CreateBudgetRequest request);
    Budget updateBudget(Integer budgetId, User user, CreateBudgetRequest request);
    void deleteBudget(Integer budgetId, User user);
    
    // ========================================================================
    // ADMIN METHODS
    // ========================================================================
    Page<Budget> getAllBudgetsForAdmin(Integer userId, LocalDate startDate, LocalDate endDate, String keyword, org.springframework.data.domain.Pageable pageable);
    void adminUpdateBudget(Budget budget);
    void deleteBudgetById(Integer id);
}