package com.group4.expense_manager.controller;

import com.group4.expense_manager.entity.Expense;
import com.group4.expense_manager.entity.User;
import com.group4.expense_manager.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;

@Controller
@RequestMapping("/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    // ============================
    // LIST PAGE
    // ============================
    @GetMapping
    public String listExpenses(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            Model model
    ) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Expense> expenses = expenseService.filterExpensesOfUser(
                user, categoryId, fromDate, toDate, pageable, search, minAmount, maxAmount
        );

        if (expenses.isEmpty()) {
            model.addAttribute("message", "No expenses found.");
        }

        model.addAttribute("expenses", expenses);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalExpenses", expenseService.getTotalExpenses(user));
        model.addAttribute("totalAmount", expenseService.getTotalAmount(user));
        model.addAttribute("averageAmount", expenseService.getAverageAmount(user));
        model.addAttribute("totalPages", expenses.getTotalPages());
        return "client/expenses";
        
    }

    // ============================
    // DETAIL PAGE
    // ============================
    @GetMapping("/{id}")
    public String getExpenseDetail(
            @PathVariable Integer id,
            @AuthenticationPrincipal User user,
            Model model
    ) {
        Expense expense = expenseService.getExpenseOfUser(id, user);
        model.addAttribute("expense", expense);
        return "client/expense-detail"; // --> templates/client/expense-detail.html
    }

    // ============================
    // CREATE
    // ============================
    @PostMapping("/create")
    public String createExpense(
            @AuthenticationPrincipal User user,
            @ModelAttribute @Valid Expense expense
    ) {
        expenseService.createExpense(user, expense);
        return "redirect:/expenses";
    }

    // ============================
    // UPDATE
    // ============================
    @PostMapping("/update/{id}")
    public String updateExpense(
            @PathVariable Integer id,
            @AuthenticationPrincipal User user,
            @ModelAttribute @Valid Expense expense
    ) {
        expenseService.updateExpense(id, user, expense);
        return "redirect:/expenses/" + id;
    }

    // ============================
    // DELETE
    // ============================
    @PostMapping("/delete/{id}")
    public String deleteExpense(
            @PathVariable Integer id,
            @AuthenticationPrincipal User user
    ) {
        expenseService.deleteExpense(id, user);
        return "redirect:/expenses";
    }
}
