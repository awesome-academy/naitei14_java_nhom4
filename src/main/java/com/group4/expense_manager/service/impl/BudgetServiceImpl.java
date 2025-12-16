package com.group4.expense_manager.service.impl;

import com.group4.expense_manager.annotation.LogActivity;
import com.group4.expense_manager.dto.request.CreateBudgetRequest;
import com.group4.expense_manager.entity.Budget;
import com.group4.expense_manager.entity.BudgetTemplate;
import com.group4.expense_manager.entity.Category;
import com.group4.expense_manager.entity.CategoryType;
import com.group4.expense_manager.entity.User;
import com.group4.expense_manager.repository.BudgetRepository;
import com.group4.expense_manager.repository.BudgetTemplateRepository;
import com.group4.expense_manager.repository.CategoryRepository;
import com.group4.expense_manager.repository.UserRepository;
import com.group4.expense_manager.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetServiceImpl implements BudgetService {
    
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final BudgetTemplateRepository budgetTemplateRepository;
    
    @Autowired
    public BudgetServiceImpl(BudgetRepository budgetRepository, 
                             CategoryRepository categoryRepository,
                             UserRepository userRepository,
                             BudgetTemplateRepository budgetTemplateRepository) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.budgetTemplateRepository = budgetTemplateRepository;
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
    @LogActivity(
        action = "CREATE",
        targetEntity = "BUDGET",
        description = "Created new budget "
    )
    public Budget createBudget(User user, CreateBudgetRequest request) {
        Budget budget = new Budget();
        mapRequestToEntity(request, budget, user);
        return budgetRepository.save(budget);
    }
    
    @Override
    @Transactional
    @LogActivity(
        action = "UPDATE",
        targetEntity = "BUDGET",
        description = "Updated budget"
    )
    public Budget updateBudget(Integer budgetId, User user, CreateBudgetRequest request) {
        Budget budget = getBudget(budgetId, user);
        mapRequestToEntity(request, budget, user);
        return budgetRepository.save(budget);
    }
    
    @Override
    @Transactional
    @LogActivity(
        action = "DELETE",
        targetEntity = "BUDGET",
        description = "Deleted budget"
    )
    public void deleteBudget(Integer budgetId, User user) {
        Budget budget = getBudget(budgetId, user);
        budgetRepository.delete(budget);
    }

    @Override
    @Transactional
    public List<Budget> applyTemplate(User user, Integer templateId, YearMonth month) {
        if (month == null) {
            throw new IllegalArgumentException("Month is required");
        }

        BudgetTemplate template = budgetTemplateRepository.findByIdWithItems(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));

        if (template.getItems() == null || template.getItems().isEmpty()) {
            throw new RuntimeException("Template has no items");
        }

        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();

        List<Budget> budgets = template.getItems().stream()
                .map(item -> {
                    Budget budget = new Budget();
                    budget.setUser(user);
                    budget.setCategory(item.getCategory());
                    budget.setAmount(item.getDefaultAmount());
                    budget.setCurrency("VND");
                    budget.setStartDate(startDate);
                    budget.setEndDate(endDate);
                    return budget;
                })
                .collect(Collectors.toList());

        return budgetRepository.saveAll(budgets);
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
    
    // ========================================================================
    // ADMIN METHODS
    // ========================================================================
    
    @Override
    public Page<Budget> getAllBudgetsForAdmin(Integer categoryId, LocalDate startDate, LocalDate endDate, 
                                               String keyword, Pageable pageable) {
        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId).orElse(null);
        }
        return budgetRepository.searchBudgetsForAdmin(category, startDate, endDate, keyword, pageable);
    }
    
    @Override
    @Transactional
    @LogActivity(
        action = "UPDATE",
        targetEntity = "BUDGET",
        description = " updated budget "
    )
    public void adminUpdateBudget(Budget budget) {
        Budget existingBudget = budgetRepository.findById(budget.getId())
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        
        // Admin có thể cập nhật các field cần thiết
        existingBudget.setAmount(budget.getAmount());
        existingBudget.setCurrency(budget.getCurrency());
        existingBudget.setStartDate(budget.getStartDate());
        existingBudget.setEndDate(budget.getEndDate());
        
        if (budget.getCategory() != null && budget.getCategory().getId() != null) {
            Category category = categoryRepository.findById(budget.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            existingBudget.setCategory(category);
        }
        
        budgetRepository.save(existingBudget);
    }
    
    @Override
    @Transactional
    @LogActivity(
        action = "DELETE",
        targetEntity = "BUDGET",
        description = "deleted budget"
    )
    public void deleteBudgetById(Integer id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        budgetRepository.delete(budget);
    }
}
