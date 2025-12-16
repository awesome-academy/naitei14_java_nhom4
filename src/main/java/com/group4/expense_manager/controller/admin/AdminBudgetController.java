package com.group4.expense_manager.controller.admin;

import com.group4.expense_manager.entity.Budget;
import com.group4.expense_manager.entity.User;
import com.group4.expense_manager.entity.CategoryType;
import com.group4.expense_manager.repository.BudgetRepository;
import com.group4.expense_manager.repository.CategoryRepository;
import com.group4.expense_manager.repository.UserRepository;
import com.group4.expense_manager.service.BudgetService;
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

@Controller
@RequestMapping("/admin/budgets")
public class AdminBudgetController {

    private final BudgetService budgetService;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;

    @Autowired
    public AdminBudgetController(BudgetService budgetService, UserRepository userRepository,
                                 CategoryRepository categoryRepository, BudgetRepository budgetRepository) {
        this.budgetService = budgetService;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.budgetRepository = budgetRepository;
    }

    // --- LIST & FILTER & SORT ---
    @GetMapping
    public String listBudgets(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,

            // Filter Params
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String keyword,

            // Sort Params
            @RequestParam(defaultValue = "startDate") String sortField,
            @RequestParam(defaultValue = "desc") String sortDir,

            Model model
    ) {
        // 1. Tạo đối tượng Sort động
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();

        // 2. Gọi Service (PageRequest bắt đầu từ 0)
        Page<Budget> budgetPage = budgetService.getAllBudgetsForAdmin(
            categoryId, startDate, endDate, keyword,
                PageRequest.of(page - 1, size, sort)
        );

        // 3. Lấy danh sách Category (expense) cho dropdown
        var categories = categoryRepository.findByType(CategoryType.expense);

        // 4. Đổ dữ liệu ra View
        model.addAttribute("budgets", budgetPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", budgetPage.getTotalPages());
        model.addAttribute("totalItems", budgetPage.getTotalElements());
        model.addAttribute("categories", categories);

        // 5. Giữ lại giá trị Filter để hiển thị lại
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("keyword", keyword);

        // 6. Giữ lại giá trị Sort để hiển thị icon và link đảo chiều
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        model.addAttribute("pageTitle", "Quản lý Ngân sách");

        return "admin/budgets/list";
    }

    // --- SHOW EDIT FORM ---
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        Budget budget = budgetRepository.findById(id).orElse(null);
        if (budget == null) {
            ra.addFlashAttribute("error", "Không tìm thấy ngân sách.");
            return "redirect:/admin/budgets";
        }
        model.addAttribute("budget", budget);
        model.addAttribute("categories", categoryRepository.findByType(CategoryType.expense));
        return "admin/budgets/form";
    }

    // --- SAVE EDIT ---
    @PostMapping("/save")
    public String saveBudget(@ModelAttribute("budget") Budget budget, RedirectAttributes ra) {
        try {
            budgetService.adminUpdateBudget(budget);
            ra.addFlashAttribute("message", "Cập nhật ngân sách thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi cập nhật: " + e.getMessage());
        }
        return "redirect:/admin/budgets";
    }

    // --- DELETE ---
    @GetMapping("/delete/{id}")
    public String deleteBudget(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            budgetService.deleteBudgetById(id);
            redirectAttributes.addFlashAttribute("message", "Đã xóa ngân sách thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa: " + e.getMessage());
        }
        return "redirect:/admin/budgets";
    }
}
