package com.group4.expense_manager.controller.admin;

import com.group4.expense_manager.dto.request.CreateCategoryRequest;
import com.group4.expense_manager.entity.Category;
import com.group4.expense_manager.entity.CategoryType;
import com.group4.expense_manager.service.CategoryService;
import com.group4.expense_manager.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final CloudinaryService cloudinaryService;

    @Autowired
    public AdminCategoryController(CategoryService categoryService, CloudinaryService cloudinaryService) {
        this.categoryService = categoryService;
        this.cloudinaryService = cloudinaryService;
    }

    // LIST & FILTER & SORT
    @GetMapping
    public String listCategories(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) CategoryType type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "name") String sortField,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        
        Page<Category> categoryPage = categoryService.adminListGlobalCategories(
                type, keyword, PageRequest.of(page - 1, size, sort)
        );

        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", categoryPage.getTotalPages());
        model.addAttribute("totalItems", categoryPage.getTotalElements());
        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("categoryTypes", CategoryType.values());

        return "admin/categories/list";
    }

    // SHOW CREATE FORM
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("categoryTypes", CategoryType.values());
        return "admin/categories/form";
    }

    // SHOW EDIT FORM
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        try {
            Category category = categoryService.adminGetGlobalCategory(id);
            model.addAttribute("category", category);
            model.addAttribute("categoryTypes", CategoryType.values());
            return "admin/categories/form";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/categories";
        }
    }

    // SAVE (CREATE & UPDATE)
    @PostMapping("/save")
    public String saveCategory(
            @RequestParam(required = false) Integer id,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam CategoryType type,
            @RequestParam(required = false) MultipartFile iconFile,
            RedirectAttributes ra
    ) {
        try {
            CreateCategoryRequest request = new CreateCategoryRequest();
            request.setName(name);
            request.setDescription(description);
            request.setType(type);

            // Handle icon upload
            if (iconFile != null && !iconFile.isEmpty()) {
                String iconUrl = cloudinaryService.uploadIcon(iconFile, "categories");
                request.setIcon(iconUrl);
            } else if (id != null) {
                // Keep existing icon when editing
                Category existing = categoryService.adminGetGlobalCategory(id);
                request.setIcon(existing.getIcon());
            } else {
                // No icon for new category
                request.setIcon("");
            }

            // Create or Update
            if (id != null) {
                categoryService.adminUpdateGlobalCategory(id, request);
                ra.addFlashAttribute("message", "Cập nhật danh mục thành công!");
            } else {
                categoryService.adminCreateGlobalCategory(request);
                ra.addFlashAttribute("message", "Tạo danh mục thành công!");
            }
            
            return "redirect:/admin/categories";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/categories";
        }
    }

    // SOFT DELETE
    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            categoryService.adminSoftDeleteCategory(id);
            ra.addFlashAttribute("message", "Xóa danh mục thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể xóa: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    // EXPORT CSV
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv() {
        try {
            byte[] csvData = categoryService.exportCategoriesToCsv();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
            headers.setContentDispositionFormData("attachment", "categories_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // IMPORT CSV
    @PostMapping("/import")
    public String importCsv(@RequestParam("file") MultipartFile file, RedirectAttributes ra) {
        try {
            if (file.isEmpty()) {
                ra.addFlashAttribute("error", "Vui lòng chọn file CSV để import");
                return "redirect:/admin/categories";
            }
            
            categoryService.importCategoriesFromCsv(file);
            ra.addFlashAttribute("message", "Import categories thành công!");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Import thất bại: " + e.getMessage());
            return "redirect:/admin/categories";
        }
    }
}
