package com.group4.expense_manager.service.impl;

import com.group4.expense_manager.annotation.LogActivity;
import com.group4.expense_manager.dto.request.ExpenseRequest;
import com.group4.expense_manager.entity.Category;
import com.group4.expense_manager.entity.Expense;
import com.group4.expense_manager.entity.Budget;
import com.group4.expense_manager.entity.User;
import com.group4.expense_manager.repository.CategoryRepository;
import com.group4.expense_manager.repository.ExpenseRepository;
import com.group4.expense_manager.repository.BudgetRepository;
import com.group4.expense_manager.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.group4.expense_manager.service.EmailService;

import java.time.LocalDate;
import java.util.Optional;
import java.math.BigDecimal;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;

    @Autowired
    public ExpenseServiceImpl(ExpenseRepository expenseRepository, CategoryRepository categoryRepository, BudgetRepository budgetRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.budgetRepository = budgetRepository;
    }

    @Autowired
    private EmailService emailService;

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
    public void checkBudgetAlert(Expense newExpense) {
        if (newExpense.getCategory() == null) return;

        Budget budget = budgetRepository.findActiveBudgetForExpense(
                newExpense.getUser().getId(),
                newExpense.getCategory().getId(),
                newExpense.getExpenseDate()
        ).orElse(null);

        if (budget == null) return;

        BigDecimal totalSpent = expenseRepository.sumExpensesByBudgetPeriod(
                budget.getUser().getId(),
                budget.getCategory().getId(),
                budget.getStartDate(),
                budget.getEndDate()
        ).orElse(BigDecimal.ZERO);

        if (totalSpent.compareTo(budget.getAmount()) > 0) {
            String subject = "Cảnh báo vượt hạn mức ngân sách: " + budget.getCategory().getName();
            String message = String.format(
                    "Chào %s,\n\nBạn đã chi tiêu vượt quá hạn mức cho danh mục '%s'.\n" +
                            "- Ngân sách đặt ra: %.2f\n" +
                            "- Tổng chi tiêu hiện tại: %.2f\n" +
                            "- Ngày giao dịch: %s\n\n" +
                            "Vui lòng kiểm tra lại kế hoạch chi tiêu của mình!",
                    newExpense.getUser().getName(),
                    budget.getCategory().getName(),
                    budget.getAmount(),
                    totalSpent,
                    newExpense.getExpenseDate()
            );

            // Gửi email
            try {
                emailService.sendSimpleEmail(newExpense.getUser().getEmail(), subject, message);
                System.out.println("Email cảnh báo đã được gửi tới: " + newExpense.getUser().getEmail());
            } catch (Exception e) {
                System.err.println("Lỗi khi gửi email: " + e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    @LogActivity(
        action = "CREATE",
        targetEntity = "EXPENSE",
        description = "Created new expense"
    )
    public Expense createExpense(User user, ExpenseRequest request) {
        Expense expense = new Expense();
        mapRequestToEntity(request, expense, user);
        checkBudgetAlert(expense);
        return expenseRepository.save(expense);
    }

    @Override
    @Transactional
    @LogActivity(
        action = "UPDATE",
        targetEntity = "EXPENSE",
        description = "Updated expense"
    )
    public Expense updateExpense(Integer expenseId, User user, ExpenseRequest request) {
        Expense expense = getExpenseOfUser(expenseId, user);
        mapRequestToEntity(request, expense, user);
        return expenseRepository.save(expense);
    }

    @Override
    @Transactional
    @LogActivity(
        action = "DELETE",
        targetEntity = "EXPENSE",
        description = "Deleted expense "
    )
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
    @LogActivity(
        action = "DELETE",
        targetEntity = "EXPENSE",
        description = "deleted expense "
    )
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
    
    /**
     * Map dữ liệu từ ExpenseRequest sang Expense entity
     */
    private void mapRequestToEntity(ExpenseRequest request, Expense expense, User user) {
        // 1. Map các trường thông tin cơ bản
        expense.setUser(user);
        expense.setName(request.getName());
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setNote(request.getNote());

        // 2. Xử lý Category
        if (request.getCategoryId() != null) {
            Category category = validateCategory(request.getCategoryId(), user);
            expense.setCategory(category);
        } else {
            expense.setCategory(null);
        }

        // 3. Xử lý Currency
        String currency = request.getCurrency();
        if (currency == null || currency.isBlank()) {
            currency = user.getDefaultCurrency();
        }
        expense.setCurrency(currency);

        // 4. Xử lý Recurring
        boolean isRecurring = (request.getIsRecurring() != null && request.getIsRecurring());
        expense.setIsRecurring(isRecurring);

        if (isRecurring) {
            if (request.getRecurringInterval() == null) {
                throw new RuntimeException("Vui lòng chọn chu kỳ lặp lại.");
            }
            expense.setRecurringInterval(request.getRecurringInterval());
            expense.setRecurringEndDate(request.getRecurringEndDate());
        } else {
            expense.setRecurringInterval(null);
            expense.setRecurringEndDate(null);
        }
    }
    
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
