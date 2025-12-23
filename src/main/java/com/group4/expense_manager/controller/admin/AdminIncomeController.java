package com.group4.expense_manager.controller.admin;

import com.group4.expense_manager.entity.CategoryType;
import com.group4.expense_manager.entity.Income;
import com.group4.expense_manager.entity.User;
import com.group4.expense_manager.repository.CategoryRepository;
import com.group4.expense_manager.repository.IncomeRepository;
import com.group4.expense_manager.repository.UserRepository;
import com.group4.expense_manager.service.IncomeService;
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
@RequestMapping("/admin/incomes")
public class AdminIncomeController {

    private final IncomeService incomeService;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final IncomeRepository incomeRepository;

    @Autowired
    public AdminIncomeController(IncomeService incomeService, UserRepository userRepository,
                                 CategoryRepository categoryRepository, IncomeRepository incomeRepository) {
        this.incomeService = incomeService;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.incomeRepository = incomeRepository;
    }

    // --- LIST & FILTER & SORT ---
    @GetMapping
    public String listIncomes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,

            // Filter Params
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String keyword,

            // Sort Params
            @RequestParam(defaultValue = "incomeDate") String sortField,
            @RequestParam(defaultValue = "desc") String sortDir,

            Model model
    ) {
        // 1. Tạo đối tượng Sort động
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();

        // 2. Gọi Service (PageRequest bắt đầu từ 0)
        Page<Income> incomePage = incomeService.getAllIncomesForAdmin(
                userId, startDate, endDate, keyword,
                PageRequest.of(page - 1, size, sort)
        );

        // 3. Lấy danh sách Client cho dropdown
        List<User> users = userRepository.findByRoleIgnoreCase("CLIENT");

        // 4. Đổ dữ liệu ra View
        model.addAttribute("incomes", incomePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", incomePage.getTotalPages());
        model.addAttribute("totalItems", incomePage.getTotalElements());
        model.addAttribute("users", users);

        // 5. Giữ lại giá trị Filter để hiển thị lại
        model.addAttribute("userId", userId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("keyword", keyword);

        // 6. Giữ lại giá trị Sort để hiển thị icon và link đảo chiều
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        model.addAttribute("pageTitle", "Quản lý Thu nhập");

        return "admin/incomes/list";
    }

    // --- SHOW EDIT FORM ---
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        Income income = incomeRepository.findById(id).orElse(null);
        if (income == null) {
            ra.addFlashAttribute("error", "Không tìm thấy khoản thu nhập.");
            return "redirect:/admin/incomes";
        }
        model.addAttribute("income", income);
        model.addAttribute("categories", categoryRepository.findByType(CategoryType.income));
        return "admin/incomes/form";
    }

    // --- SAVE EDIT ---
    @PostMapping("/save")
    public String saveIncome(@ModelAttribute("income") Income income, RedirectAttributes ra) {
        try {
            incomeService.adminUpdateIncome(income);
            ra.addFlashAttribute("message", "Cập nhật thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi cập nhật: " + e.getMessage());
        }
        return "redirect:/admin/incomes";
    }

    // --- DELETE ---
    @GetMapping("/delete/{id}")
    public String deleteIncome(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            incomeService.deleteIncomeById(id);
            redirectAttributes.addFlashAttribute("message", "Đã xóa thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa: " + e.getMessage());
        }
        return "redirect:/admin/incomes";
    }
}