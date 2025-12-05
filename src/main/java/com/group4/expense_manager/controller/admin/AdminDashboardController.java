package com.group4.expense_manager.controller.admin;

import com.group4.expense_manager.repository.CategoryRepository;
import com.group4.expense_manager.repository.ExpenseRepository;
import com.group4.expense_manager.repository.IncomeRepository;
import com.group4.expense_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ExpenseRepository expenseRepository;
    @Autowired private IncomeRepository incomeRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long totalUsers = userRepository.count();
        long totalCategories = categoryRepository.count();
        long totalExpenses = expenseRepository.count();
        long totalIncomes = incomeRepository.count();

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalCategories", totalCategories);
        model.addAttribute("totalExpenses", totalExpenses);
        model.addAttribute("totalIncomes", totalIncomes);


        return "admin/dashboard";
    }
}