package com.group4.expense_manager.service;

import com.group4.expense_manager.entity.Expense;
import com.group4.expense_manager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

public interface ExpenseService {

    // User methods
    Page<Expense> listExpensesOfUser(User user, Pageable pageable);
    Page<Expense> filterExpensesOfUser(User user, Integer categoryId, LocalDate fromDate, LocalDate toDate, Pageable pageable, String search, Double minAmount, Double maxAmount);
    Expense getExpenseOfUser(Integer expenseId, User user);
    Expense createExpense(User user, Expense expense);
    Expense updateExpense(Integer expenseId, User user, Expense expense);
    void deleteExpense(Integer expenseId, User user);
    long getTotalExpenses(User user);
    double getTotalAmount(User user);
    double getAverageAmount(User user);

    // Admin methods
    Page<Expense> getAllExpensesForAdmin(Integer userId, Integer categoryId, LocalDate startDate, LocalDate endDate, String keyword, Pageable pageable);
    Optional<Expense> getExpenseById(Integer expenseId);
    void deleteExpenseById(Integer expenseId);
    void adminUpdateExpense(Expense expense);
}