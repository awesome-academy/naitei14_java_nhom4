package com.group4.expense_manager.controller.admin;

import com.group4.expense_manager.service.CsvService;
import com.group4.expense_manager.util.CsvHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
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


@Controller
@RequestMapping("/admin/csv")
public class AdminCsvController {

    @Autowired
    private CsvService csvService;

    @GetMapping("/users/export")
    public ResponseEntity<Resource> exportUsers() {
        String filename = "users_report.csv";
        InputStreamResource file = new InputStreamResource(csvService.loadUserCsv());

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
    public ResponseEntity<Resource> exportIncomes() {
        String filename = "incomes_data.csv";
        InputStreamResource file = new InputStreamResource(csvService.loadIncomeCsv());

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
    public ResponseEntity<Resource> exportBudgets() {
        String filename = "budgets_data.csv";
        InputStreamResource file = new InputStreamResource(csvService.loadBudgetCsv());

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
                ra.addFlashAttribute("message", "Import Budgets thành công: " + file.getOriginalFilename());
            } catch (Exception e) {
                ra.addFlashAttribute("error", "Lỗi import file: " + e.getMessage());
            }
        } else {
            ra.addFlashAttribute("error", "Vui lòng chọn file định dạng CSV!");
        }
        return "redirect:/admin/budgets";
    }
}
