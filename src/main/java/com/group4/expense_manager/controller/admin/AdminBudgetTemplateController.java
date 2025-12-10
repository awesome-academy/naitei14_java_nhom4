package com.group4.expense_manager.controller.admin;

import com.group4.expense_manager.dto.request.BudgetTemplateRequest;
import com.group4.expense_manager.entity.BudgetTemplate;
import com.group4.expense_manager.entity.CategoryType;
import com.group4.expense_manager.repository.CategoryRepository;
import com.group4.expense_manager.service.BudgetTemplateService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/budget-templates")
public class AdminBudgetTemplateController {
    
    private final BudgetTemplateService budgetTemplateService;
    private final CategoryRepository categoryRepository;
    
    @Autowired
    public AdminBudgetTemplateController(BudgetTemplateService budgetTemplateService,
                                         CategoryRepository categoryRepository) {
        this.budgetTemplateService = budgetTemplateService;
        this.categoryRepository = categoryRepository;
    }
    
    // --- LIST & FILTER ---
    @GetMapping
    public String listTemplates(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer month,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Page<BudgetTemplate> templatePage = budgetTemplateService.listTemplates(
                keyword, month, PageRequest.of(page - 1, size, sort)
        );
        
        model.addAttribute("templates", templatePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", templatePage.getTotalPages());
        model.addAttribute("totalItems", templatePage.getTotalElements());
        
        // Filter params
        model.addAttribute("keyword", keyword);
        model.addAttribute("month", month);
        
        // Sort params
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        
        model.addAttribute("pageTitle", "Quản lý Mẫu Ngân sách");
        
        return "admin/budget-templates/list";
    }
    
    // --- SHOW CREATE FORM ---
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("template", new BudgetTemplateRequest());
        model.addAttribute("categories", categoryRepository.findByType(CategoryType.expense));
        model.addAttribute("isEdit", false);
        model.addAttribute("pageTitle", "Tạo Mẫu Ngân sách mới");
        return "admin/budget-templates/form";
    }
    
    // --- SAVE (CREATE) ---
    @PostMapping("/save")
    public String saveTemplate(
            @ModelAttribute("template") @Valid BudgetTemplateRequest request,
            BindingResult bindingResult,
            @RequestParam(required = false) Integer[] categoryIds,
            @RequestParam(required = false) java.math.BigDecimal[] amounts,
            RedirectAttributes ra,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findByType(CategoryType.expense));
            model.addAttribute("isEdit", false);
            return "admin/budget-templates/form";
        }
        
        try {
            // Xử lý items từ form
            if (categoryIds != null && amounts != null) {
                java.util.List<BudgetTemplateRequest.BudgetTemplateItemRequest> items = new java.util.ArrayList<>();
                for (int i = 0; i < categoryIds.length; i++) {
                    if (categoryIds[i] != null && amounts[i] != null) {
                        BudgetTemplateRequest.BudgetTemplateItemRequest item = new BudgetTemplateRequest.BudgetTemplateItemRequest();
                        item.setCategoryId(categoryIds[i]);
                        item.setDefaultAmount(amounts[i]);
                        items.add(item);
                    }
                }
                request.setItems(items);
            }
            
            budgetTemplateService.createTemplate(request);
            ra.addFlashAttribute("message", "Tạo mẫu ngân sách thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi khi tạo mẫu: " + e.getMessage());
        }
        
        return "redirect:/admin/budget-templates";
    }
    
    // --- SHOW EDIT FORM ---
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        try {
            BudgetTemplate template = budgetTemplateService.getTemplate(id);
            
            // Map entity to request DTO
            BudgetTemplateRequest request = new BudgetTemplateRequest();
            request.setName(template.getName());
            request.setMonth(template.getMonth());
            request.setDescription(template.getDescription());
            
            model.addAttribute("template", request);
            model.addAttribute("templateEntity", template);
            model.addAttribute("categories", categoryRepository.findByType(CategoryType.expense));
            model.addAttribute("isEdit", true);
            model.addAttribute("templateId", id);
            model.addAttribute("pageTitle", "Chỉnh sửa Mẫu Ngân sách");
            
            return "admin/budget-templates/form";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không tìm thấy mẫu ngân sách: " + e.getMessage());
            return "redirect:/admin/budget-templates";
        }
    }
    
    // --- UPDATE ---
    @PostMapping("/update/{id}")
    public String updateTemplate(
            @PathVariable Integer id,
            @ModelAttribute("template") @Valid BudgetTemplateRequest request,
            BindingResult bindingResult,
            @RequestParam(required = false) Integer[] categoryIds,
            @RequestParam(required = false) java.math.BigDecimal[] amounts,
            RedirectAttributes ra,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findByType(CategoryType.expense));
            model.addAttribute("isEdit", true);
            model.addAttribute("templateId", id);
            return "admin/budget-templates/form";
        }
        
        try {
            // Xử lý items từ form
            if (categoryIds != null && amounts != null) {
                java.util.List<BudgetTemplateRequest.BudgetTemplateItemRequest> items = new java.util.ArrayList<>();
                for (int i = 0; i < categoryIds.length; i++) {
                    if (categoryIds[i] != null && amounts[i] != null) {
                        BudgetTemplateRequest.BudgetTemplateItemRequest item = new BudgetTemplateRequest.BudgetTemplateItemRequest();
                        item.setCategoryId(categoryIds[i]);
                        item.setDefaultAmount(amounts[i]);
                        items.add(item);
                    }
                }
                request.setItems(items);
            }
            
            budgetTemplateService.updateTemplate(id, request);
            ra.addFlashAttribute("message", "Cập nhật mẫu ngân sách thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi khi cập nhật: " + e.getMessage());
        }
        
        return "redirect:/admin/budget-templates";
    }
    
    // --- VIEW DETAIL ---
    @GetMapping("/detail/{id}")
    public String viewDetail(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        try {
            BudgetTemplate template = budgetTemplateService.getTemplate(id);
            model.addAttribute("template", template);
            model.addAttribute("pageTitle", "Chi tiết Mẫu Ngân sách");
            return "admin/budget-templates/detail";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không tìm thấy mẫu ngân sách: " + e.getMessage());
            return "redirect:/admin/budget-templates";
        }
    }
    
    // --- DELETE ---
    @GetMapping("/delete/{id}")
    public String deleteTemplate(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            budgetTemplateService.deleteTemplate(id);
            ra.addFlashAttribute("message", "Xóa mẫu ngân sách thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi khi xóa: " + e.getMessage());
        }
        return "redirect:/admin/budget-templates";
    }
}
