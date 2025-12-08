package com.group4.expense_manager.service.impl;

import com.group4.expense_manager.dto.request.CreateBudgetRequest;
import com.group4.expense_manager.entity.Budget;
import com.group4.expense_manager.entity.Category;
import com.group4.expense_manager.entity.CategoryType;
import com.group4.expense_manager.entity.User;
import com.group4.expense_manager.repository.BudgetRepository;
import com.group4.expense_manager.repository.CategoryRepository;
import com.group4.expense_manager.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
public class BudgetServiceImpl implements BudgetService {
    
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    
    @Autowired
    public BudgetServiceImpl(BudgetRepository budgetRepository, CategoryRepository categoryRepository) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
    }
    
    @Override
    public Page<Budget> listBudgets(User user, Integer categoryId, YearMonth month, Pageable pageable) {
        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId).orElse(null);
        }
        
        LocalDate startDate = null;
        LocalDate endDate = null;
        if (month != null) {
            startDate = month.atDay(1);
            endDate = month.atEndOfMonth();
        }
        
        // Filter by category and date range
        if (category != null && startDate != null && endDate != null) {
            return budgetRepository.findByUserAndCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                user, category, endDate, startDate, pageable);
        }
        
        // Filter by category only
        if (category != null) {
            return budgetRepository.findByUserAndCategory(user, category, pageable);
        }
        
        // Filter by date range only
        if (startDate != null && endDate != null) {
            return budgetRepository.findByUserAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                user, endDate, startDate, pageable);
        }
        
        // No filter - return all user's budgets
        return budgetRepository.findByUser(user, pageable);
    }
    
    @Override
    public Budget getBudget(Integer budgetId, User user) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
                
        if (!budget.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You don't have permission to access this budget");
        }
        
        return budget;
    }
    
    @Override
    @Transactional
    public Budget createBudget(User user, CreateBudgetRequest request) {
        Budget budget = new Budget();
        mapRequestToEntity(request, budget, user);
        return budgetRepository.save(budget);
    }
    
    @Override
    @Transactional
    public Budget updateBudget(Integer budgetId, User user, CreateBudgetRequest request) {
        Budget budget = getBudget(budgetId, user);
        mapRequestToEntity(request, budget, user);
        return budgetRepository.save(budget);
    }
    
    @Override
    @Transactional
    public void deleteBudget(Integer budgetId, User user) {
        Budget budget = getBudget(budgetId, user);
        budgetRepository.delete(budget);
    }
    
    // Helper method
    private void mapRequestToEntity(CreateBudgetRequest request, Budget budget, User user) {
        Category category = validateCategory(request.getCategoryId(), user);
        
        budget.setUser(user);
        budget.setCategory(category);
        budget.setAmount(request.getAmount());
        budget.setCurrency(request.getCurrency());
        budget.setStartDate(request.getStartDate());
        budget.setEndDate(request.getEndDate());
        
        // Validate date range
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("End date cannot be before start date");
        }
    }
    
    private Category validateCategory(Integer categoryId, User user) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
                
        if (category.getType() != CategoryType.expense) {
            throw new RuntimeException("Budget can only be created for expense categories");
        }
        
        if (category.getUser() != null && !category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You don't have permission to use this category");
        }
        
        return category;
    }
}