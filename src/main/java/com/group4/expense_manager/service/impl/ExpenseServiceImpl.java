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
    public Page<Expense> filterExpensesOfUser(User user, Integer categoryId, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId).orElse(null);
        }

        if (category != null && fromDate != null && toDate != null) {
            return expenseRepository.findByUserAndCategoryAndExpenseDateBetween(user, category, fromDate, toDate, pageable);
        }
        if (category != null) {
            return expenseRepository.findByUserAndCategory(user, category, pageable);
        }
        if (fromDate != null && toDate != null) {
            return expenseRepository.findByUserAndExpenseDateBetween(user, fromDate, toDate, pageable);
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
}