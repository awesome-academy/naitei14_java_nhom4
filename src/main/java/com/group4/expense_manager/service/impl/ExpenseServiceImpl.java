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

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public Page<Expense> listExpensesOfUser(User user, Pageable pageable) {
        return expenseRepository.findByUser(user, pageable);
    }

    @Override
    public Page<Expense> filterExpensesOfUser(User user, Integer categoryId,
                                              LocalDate fromDate, LocalDate toDate,
                                              Pageable pageable, String search,
                                              Double minAmount, Double maxAmount) {

        Category category = null;

        if (categoryId != null) {
            category = categoryRepository.findById(categoryId).orElse(null);
        }

        if (category != null && fromDate != null && toDate != null) {
            return expenseRepository.findByUserAndCategoryAndExpenseDateBetween(
                    user, category, fromDate, toDate, pageable);
        }

        if (category != null) {
            return expenseRepository.findByUserAndCategory(user, category, pageable);
        }

        if (fromDate != null && toDate != null) {
            return expenseRepository.findByUserAndExpenseDateBetween(
                    user, fromDate, toDate, pageable);
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
        return expenseRepository.findByIdAndUser(expenseId, user)
                .orElseThrow(() -> new RuntimeException("Khoản chi tiêu không tồn tại hoặc bạn không có quyền truy cập."));
    }

    @Override
    @Transactional
    public Expense createExpense(User user, Expense expense) {
        expense.setUser(user);
        return expenseRepository.save(expense);
    }

    @Override
    @Transactional
    public Expense updateExpense(Integer expenseId, User user, Expense expense) {
        Expense existingExpense = getExpenseOfUser(expenseId, user);

        existingExpense.setDescription(expense.getDescription());
        existingExpense.setAmount(expense.getAmount());
        existingExpense.setCurrency(expense.getCurrency());
        existingExpense.setExpenseDate(expense.getExpenseDate());
        existingExpense.setCategory(expense.getCategory());
        existingExpense.setNote(expense.getNote());

        return expenseRepository.save(existingExpense);
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
                .mapToDouble(expense -> expense.getAmount().doubleValue())
                .sum();
    }

    @Override
    public double getAverageAmount(User user) {
        long totalExpenses = getTotalExpenses(user);
        return totalExpenses == 0 ? 0 : getTotalAmount(user) / totalExpenses;
    }

    @Override
    public Page<Expense> listAllExpenses(Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }
        return expenseRepository.findAll(pageable);
    }

    @Override
    public Optional<Expense> getExpenseById(Integer expenseId) {
        if (expenseId == null) {
            throw new IllegalArgumentException("Expense ID cannot be null");
        }
        return Optional.ofNullable(expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found")));
    }

    @Override
    @Transactional
    public void deleteExpenseById(Integer expenseId) {
        if (expenseId == null) {
            throw new IllegalArgumentException("Expense ID cannot be null");
        }
        expenseRepository.deleteById(expenseId);
    }

    @Override
    @Transactional
    public Expense updateExpenseByAdmin(Integer expenseId, Expense expense) {
        if (expenseId == null || expense == null) {
            throw new IllegalArgumentException("Expense ID and Expense cannot be null");
        }
        Expense existingExpense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        existingExpense.setName(expense.getName());
        existingExpense.setAmount(expense.getAmount());
        existingExpense.setCurrency(expense.getCurrency());
        existingExpense.setExpenseDate(expense.getExpenseDate());
        existingExpense.setCategory(expense.getCategory());
        existingExpense.setNote(expense.getNote());
        existingExpense.setIsRecurring(expense.getIsRecurring());
        existingExpense.setRecurringEndDate(expense.getRecurringEndDate());
        existingExpense.setRecurringInterval(expense.getRecurringInterval());
        return expenseRepository.save(existingExpense);
    }
}
