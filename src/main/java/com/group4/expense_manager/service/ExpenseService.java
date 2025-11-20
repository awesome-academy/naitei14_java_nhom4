package com.group4.expense_manager.service;

import com.group4.expense_manager.entity.Expense;
import com.group4.expense_manager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface ExpenseService {

    Page<Expense> listExpensesOfUser(User user, Pageable pageable);

    Page<Expense> filterExpensesOfUser(User user, Integer categoryId, LocalDate fromDate, LocalDate toDate, Pageable pageable);

    Expense getExpenseOfUser(Integer expenseId, User user);

    Expense createExpense(User user, Expense expense);

    Expense updateExpense(Integer expenseId, User user, Expense expense);

    void deleteExpense(Integer expenseId, User user);
}