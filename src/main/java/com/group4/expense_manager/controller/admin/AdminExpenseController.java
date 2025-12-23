package com.group4.expense_manager.controller.admin;

import com.group4.expense_manager.entity.Category;
import com.group4.expense_manager.entity.CategoryType;
import com.group4.expense_manager.entity.Expense;
import com.group4.expense_manager.repository.CategoryRepository;
import com.group4.expense_manager.repository.UserRepository;
import com.group4.expense_manager.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/expenses")
public class AdminExpenseController {

    private final ExpenseService expenseService;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public AdminExpenseController(ExpenseService expenseService, UserRepository userRepository, CategoryRepository categoryRepository) {
        this.expenseService = expenseService;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    // --- LIST & FILTER & SORT ---
    @GetMapping
        public String listExpenses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            // Filter Params
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String keyword,
            // Sort Params
            @RequestParam(defaultValue = "expenseDate") String sortField,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model
        ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Page<Expense> expensePage = expenseService.getAllExpensesForAdmin(
            userId, categoryId, startDate, endDate, keyword,
            PageRequest.of(page - 1, size, sort)
        );
        List<?> users = userRepository.findByRoleIgnoreCase("CLIENT");
        
        // Lấy danh sách categoryId đã được sử dụng trong chi tiêu
        List<Integer> usedCategoryIds = expensePage.getContent().stream()
            .filter(e -> e.getCategory() != null)
            .map(e -> e.getCategory().getId())
            .distinct()
            .collect(Collectors.toList());
        
        // Chỉ lấy các category type = expense đã được sử dụng
        List<Category> categories = usedCategoryIds.isEmpty() 
            ? List.of() 
            : categoryRepository.findAllById(usedCategoryIds).stream()
                .filter(cat -> cat.getType() == CategoryType.expense)
                .collect(Collectors.toList());
        
        model.addAttribute("expenses", expensePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", expensePage.getTotalPages());
        model.addAttribute("totalItems", expensePage.getTotalElements());
        model.addAttribute("users", users);
        model.addAttribute("userId", userId);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("categories", categories);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("pageTitle", "Quản lý Chi tiêu");
        return "admin/expenses/list";
        }

    // --- SHOW EDIT FORM ---
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        Expense expense = expenseService.getExpenseById(id).orElse(null);
        if (expense == null) {
            ra.addFlashAttribute("error", "Không tìm thấy khoản chi tiêu.");
            return "redirect:/admin/expenses";
        }
        model.addAttribute("expense", expense);
        model.addAttribute("categories", categoryRepository.findByType(CategoryType.expense));
        return "admin/expenses/form";
    }

    // --- SAVE EDIT ---
    @PostMapping("/save")
    public String saveExpense(@ModelAttribute("expense") Expense expense, RedirectAttributes ra) {
        try {
            expenseService.adminUpdateExpense(expense);
            ra.addFlashAttribute("message", "Cập nhật thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi cập nhật: " + e.getMessage());
        }
        return "redirect:/admin/expenses";
    }

    // --- DELETE ---
    @GetMapping("/delete/{id}")
    public String deleteExpense(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            expenseService.deleteExpenseById(id);
            redirectAttributes.addFlashAttribute("message", "Đã xóa thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa: " + e.getMessage());
        }
        return "redirect:/admin/expenses";
    }
}