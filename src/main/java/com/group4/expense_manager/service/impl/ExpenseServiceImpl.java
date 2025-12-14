package com.group4.expense_manager.service.impl;

import com.group4.expense_manager.entity.Category;
import com.group4.expense_manager.entity.Expense;
import com.group4.expense_manager.entity.User;
import com.group4.expense_manager.repository.CategoryRepository;
import com.group4.expense_manager.repository.ExpenseRepository;
import com.group4.expense_manager.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public ExpenseServiceImpl(ExpenseRepository expenseRepository, CategoryRepository categoryRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
    }

    // ===============================
    // PHẦN 1: CLIENT METHODS
    // ===============================
    @Override
    public Page<Expense> listExpensesOfUser(User user, Pageable pageable) {
        return expenseRepository.findByUser(user, pageable);
    }

    @Override
    public Page<Expense> filterExpensesOfUser(User user, Integer categoryId, LocalDate fromDate, LocalDate toDate, Pageable pageable, String search, Double minAmount, Double maxAmount) {
        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId)
                    .orElse(null);
        }
        // Dynamic query logic (tùy chỉnh theo repo)
        // Ưu tiên: category + date, category, date, search, min/max, default
        if (category != null && fromDate != null && toDate != null) {
            return expenseRepository.findByUserAndCategoryAndExpenseDateBetween(user, category, fromDate, toDate, pageable);
        }
        if (category != null) {
            return expenseRepository.findByUserAndCategory(user, category, pageable);
        }
        if (fromDate != null && toDate != null) {
            return expenseRepository.findByUserAndExpenseDateBetween(user, fromDate, toDate, pageable);
        }
        if (search != null && !search.isEmpty()) {
            return expenseRepository.findByUserAndDescriptionContainingAndAmountBetween(
                    user, search,
                    minAmount != null ? minAmount : 0,
                    maxAmount != null ? maxAmount : Double.MAX_VALUE,
                    pageable);
        }
        if (minAmount != null || maxAmount != null) {
            return expenseRepository.findByUserAndAmountBetween(
                    user,
                    minAmount != null ? minAmount : 0,
                    maxAmount != null ? maxAmount : Double.MAX_VALUE,
                    pageable);
        }
        return expenseRepository.findByUser(user, pageable);
    }

    @Override
    public Expense getExpenseOfUser(Integer expenseId, User user) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Khoản chi tiêu không tồn tại"));
        if (user != null && !expense.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền truy cập khoản chi tiêu này");
        }
        return expense;
    }

    @Override
    @Transactional
    public Expense createExpense(User user, Expense expense) {
        // Map user và validate category nếu có
        expense.setUser(user);
        if (expense.getCategory() != null && expense.getCategory().getId() != null) {
            Category category = validateCategory(expense.getCategory().getId(), user);
            expense.setCategory(category);
        }
        // Currency mặc định nếu null
        if (expense.getCurrency() == null || expense.getCurrency().isBlank()) {
            expense.setCurrency(user.getDefaultCurrency());
        }
        return expenseRepository.save(expense);
    }

    @Override
    @Transactional
    public Expense updateExpense(Integer expenseId, User user, Expense expenseData) {
        Expense expense = getExpenseOfUser(expenseId, user);
        // Cập nhật các trường cơ bản
        if (expenseData.getName() != null) expense.setName(expenseData.getName());
        if (expenseData.getDescription() != null) expense.setDescription(expenseData.getDescription());
        if (expenseData.getAmount() != null) expense.setAmount(expenseData.getAmount());
        if (expenseData.getCurrency() != null) expense.setCurrency(expenseData.getCurrency());
        if (expenseData.getExpenseDate() != null) expense.setExpenseDate(expenseData.getExpenseDate());
        if (expenseData.getCategory() != null && expenseData.getCategory().getId() != null) {
            Category category = validateCategory(expenseData.getCategory().getId(), user);
            expense.setCategory(category);
        }
        if (expenseData.getNote() != null) expense.setNote(expenseData.getNote());
        // Recurring
        if (expenseData.getIsRecurring() != null) expense.setIsRecurring(expenseData.getIsRecurring());
        if (expenseData.getRecurringInterval() != null) expense.setRecurringInterval(expenseData.getRecurringInterval());
        if (expenseData.getRecurringEndDate() != null) expense.setRecurringEndDate(expenseData.getRecurringEndDate());
        return expenseRepository.save(expense);
    }

    @Override
    @Transactional
    public void deleteExpense(Integer expenseId, User user) {
        Expense expense = getExpenseOfUser(expenseId, user);
        expenseRepository.delete(expense);
    }

    @Override
    public long getTotalExpenses(User user) {
        return expenseRepository.countByUser(user);
    }

    @Override
    public double getTotalAmount(User user) {
        return expenseRepository.findByUser(user).stream()
                .mapToDouble(e -> e.getAmount().doubleValue())
                .sum();
    }

    @Override
    public double getAverageAmount(User user) {
        long total = getTotalExpenses(user);
        return total == 0 ? 0 : getTotalAmount(user) / total;
    }

    // ===============================
    // PHẦN 2: ADMIN METHODS
    // ===============================
    @Override
    public Page<Expense> getAllExpensesForAdmin(Integer userId, Integer categoryId, LocalDate startDate, LocalDate endDate, String keyword, Pageable pageable) {
        // Ưu tiên lọc: userId + categoryId + date + keyword
        if (userId != null && categoryId != null && startDate != null && endDate != null && keyword != null && !keyword.isEmpty()) {
            return expenseRepository.findByUserIdAndCategoryIdAndExpenseDateBetweenAndDescriptionContaining(userId, categoryId, startDate, endDate, keyword, pageable);
        }
        if (userId != null && categoryId != null && startDate != null && endDate != null) {
            return expenseRepository.findByUserIdAndCategoryIdAndExpenseDateBetween(userId, categoryId, startDate, endDate, pageable);
        }
        if (userId != null && categoryId != null) {
            return expenseRepository.findByUserIdAndCategoryId(userId, categoryId, pageable);
        }
        if (categoryId != null && startDate != null && endDate != null) {
            return expenseRepository.findByCategoryIdAndExpenseDateBetween(categoryId, startDate, endDate, pageable);
        }
        if (categoryId != null) {
            return expenseRepository.findByCategoryId(categoryId, pageable);
        }
        if (userId != null && startDate != null && endDate != null && keyword != null && !keyword.isEmpty()) {
            return expenseRepository.findByUserIdAndExpenseDateBetweenAndDescriptionContaining(userId, startDate, endDate, keyword, pageable);
        }
        if (userId != null && startDate != null && endDate != null) {
            return expenseRepository.findByUserIdAndExpenseDateBetween(userId, startDate, endDate, pageable);
        }
        if (userId != null) {
            return expenseRepository.findByUserId(userId, pageable);
        }
        if (startDate != null && endDate != null) {
            return expenseRepository.findByExpenseDateBetween(startDate, endDate, pageable);
        }
        if (keyword != null && !keyword.isEmpty()) {
            return expenseRepository.findByDescriptionContaining(keyword, pageable);
        }
        return expenseRepository.findAll(pageable);
    }

    @Override
    public Optional<Expense> getExpenseById(Integer expenseId) {
        if (expenseId == null) throw new IllegalArgumentException("Expense ID cannot be null");
        return expenseRepository.findById(expenseId);
    }

    @Override
    @Transactional
    public void deleteExpenseById(Integer expenseId) {
        if (expenseId == null) throw new IllegalArgumentException("Expense ID cannot be null");
        expenseRepository.deleteById(expenseId);
    }

    @Override
    @Transactional
    public void adminUpdateExpense(Expense expenseData) {
        Expense expense = expenseRepository.findById(expenseData.getId())
                .orElseThrow(() -> new RuntimeException("Khoản chi tiêu không tồn tại (ID: " + expenseData.getId() + ")"));
        if (expenseData.getName() != null && !expenseData.getName().trim().isEmpty()) expense.setName(expenseData.getName());
        if (expenseData.getDescription() != null) expense.setDescription(expenseData.getDescription());
        if (expenseData.getAmount() != null) expense.setAmount(expenseData.getAmount());
        if (expenseData.getCurrency() != null && !expenseData.getCurrency().isEmpty()) expense.setCurrency(expenseData.getCurrency());
        if (expenseData.getExpenseDate() != null) expense.setExpenseDate(expenseData.getExpenseDate());
        if (expenseData.getCategory() != null && expenseData.getCategory().getId() != null) {
            Category category = categoryRepository.findById(expenseData.getCategory().getId())
                    .orElse(null);
            expense.setCategory(category);
        }
        if (expenseData.getNote() != null) expense.setNote(expenseData.getNote());
        if (expenseData.getIsRecurring() != null) expense.setIsRecurring(expenseData.getIsRecurring());
        if (expenseData.getRecurringInterval() != null) expense.setRecurringInterval(expenseData.getRecurringInterval());
        if (expenseData.getRecurringEndDate() != null) expense.setRecurringEndDate(expenseData.getRecurringEndDate());
        expenseRepository.save(expense);
    }

    // ===============================
    // PHẦN 3: HELPER METHODS
    // ===============================
    private Category validateCategory(Integer categoryId, User user) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
        // Nếu là category cá nhân thì phải đúng user
        if (category.getUser() != null && !category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền sử dụng danh mục này.");
        }
        return category;
    }
}
