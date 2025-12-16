package com.group4.expense_manager.controller.admin;

import com.group4.expense_manager.service.CsvService;
import com.group4.expense_manager.util.CsvHelper;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Sort;


@Controller
@RequestMapping("/admin/csv")
public class AdminCsvController {

    @Autowired
    private CsvService csvService;

    @GetMapping("/users/export")
    public ResponseEntity<Resource> exportUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean status,
            @RequestParam(defaultValue = "id,asc") String[] sort
    ) {
        String filename = "users_report.csv";

        Sort sortObj = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);
        InputStreamResource file = new InputStreamResource(
                csvService.loadUserCsv(keyword, status, sortObj)
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(file);
    }

    @PostMapping("/users/import")
    public String importUsers(@RequestParam("file") MultipartFile file, RedirectAttributes ra) {
        if (CsvHelper.hasCSVFormat(file)) {
            try {
                csvService.saveUsersFromCsv(file);
                ra.addFlashAttribute("message", "Import Users thành công: " + file.getOriginalFilename());
            } catch (Exception e) {
                ra.addFlashAttribute("error", "Lỗi import file: " + e.getMessage());
            }
        } else {
            ra.addFlashAttribute("error", "Vui lòng chọn file định dạng CSV!");
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/incomes/export")
    public ResponseEntity<Resource> exportIncomes(@RequestParam(required = false) Integer userId,@RequestParam(required = false) String keyword,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String filename = "incomes_data.csv";
        InputStreamResource file = new InputStreamResource(csvService.loadIncomeCsv(userId, keyword, startDate, endDate));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(file);
    }

    @PostMapping("/incomes/import")
    public String importIncomes(@RequestParam("file") MultipartFile file, RedirectAttributes ra) {
        if (CsvHelper.hasCSVFormat(file)) {
            try {
                csvService.saveIncomesFromCsv(file);
                ra.addFlashAttribute("message", "Import Incomes successfully: " + file.getOriginalFilename());
            } catch (Exception e) {
                ra.addFlashAttribute("error", "Failed to import file: " + e.getMessage());
            }
        } else {
            ra.addFlashAttribute("error", "Please upload a valid CSV file!");
        }

        return "redirect:/admin/incomes";
    }

    @GetMapping("/budgets/export")
    public ResponseEntity<Resource> exportBudgets(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String keyword
    ) {
        String filename = "budgets_data.csv";
        InputStreamResource file = new InputStreamResource(csvService.loadBudgetCsv(userId, startDate, endDate, keyword));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(file);
    }

    @PostMapping("/budgets/import")
    public String importBudgets(@RequestParam("file") MultipartFile file, RedirectAttributes ra) {
        if (CsvHelper.hasCSVFormat(file)) {
            try {
                csvService.saveBudgetsFromCsv(file);
                ra.addFlashAttribute("message", "Budget data imported successfully: " + file.getOriginalFilename());
            } catch (Exception e) {
                ra.addFlashAttribute("error", "Failed to upload Budget file: " + e.getMessage());
            }
        } else {
            ra.addFlashAttribute("error", "Please upload a valid CSV file!");
        }
        return "redirect:/admin/budgets";
    }

    @GetMapping("/expenses/export")
    public ResponseEntity<Resource> exportExpenses(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String keyword
    ) {
        String filename = "expenses_report.csv";

        // Gọi Service với các tham số lọc từ URL
        InputStreamResource file = new InputStreamResource(
                csvService.exportExpenseCsv(userId, categoryId, startDate, endDate, keyword )
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(file);
    }

    // --- 10. IMPORT EXPENSES ---
    @PostMapping("/expenses/import")
    public String importExpenses(@RequestParam("file") MultipartFile file, RedirectAttributes ra) {
        if (CsvHelper.hasCSVFormat(file)) {
            try {
                csvService.saveExpensesFromCsv(file);
                ra.addFlashAttribute("message", "Import Expenses successfully: " + file.getOriginalFilename());
            } catch (Exception e) {
                ra.addFlashAttribute("error", "Failed to import: " + e.getMessage());
            }
        } else {
            ra.addFlashAttribute("error", "Please upload a valid CSV file!");
        }
        return "redirect:/admin/expenses"; // Redirect về trang danh sách Expense
    }
}
