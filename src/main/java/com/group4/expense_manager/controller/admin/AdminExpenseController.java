package com.group4.expense_manager.controller.admin;

import com.group4.expense_manager.entity.Expense;
import com.group4.expense_manager.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/admin/expenses")
public class AdminExpenseController {

    @Autowired
    private ExpenseService expenseService;

    // ============================
    // LIST ALL EXPENSES
    // ============================
    @GetMapping
    public Page<Expense> listAllExpenses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return expenseService.listAllExpenses(pageable);
    }

    // ============================
    // GET EXPENSE DETAIL
    // ============================
    @GetMapping("/{id}")
    public Optional<Expense> getExpenseDetail(@PathVariable Integer id) {
        return expenseService.getExpenseById(id);
    }

    // ============================
    // DELETE EXPENSE
    // ============================
    @DeleteMapping("/{id}")
    public void deleteExpense(@PathVariable Integer id) {
        expenseService.deleteExpenseById(id);
    }

    // ============================
    // UPDATE EXPENSE
    // ============================
    @PutMapping("/{id}")
    public Expense updateExpense(
            @PathVariable Integer id,
            @RequestBody Expense expense
    ) {
        return expenseService.updateExpenseByAdmin(id, expense);
    }
}