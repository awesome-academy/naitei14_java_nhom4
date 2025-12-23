package com.group4.expense_manager.controller.admin;

import com.group4.expense_manager.dto.response.ActivityLogResponse;
import com.group4.expense_manager.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/activity-logs")
public class AdminActivityLogController {

    @Autowired
    private ActivityLogService activityLogService;

    @GetMapping
    public String listActivityLogs(Model model,
                                    @RequestParam(name = "userId", required = false) Long userId,
                                    @RequestParam(name = "entityType", required = false) String entityType,
                                    @RequestParam(name = "action", required = false) String action,
                                    // @RequestParam(name = "role", required = false) String role,
                                    @RequestParam(name = "startDate", required = false) String startDate,
                                    @RequestParam(name = "endDate", required = false) String endDate,
                                    @RequestParam(name = "page", defaultValue = "1") int page,
                                    @RequestParam(name = "sortField", defaultValue = "createdAt") String sortField,
                                    @RequestParam(name = "sortDir", defaultValue = "desc") String sortDir) {

        int pageSize = 10;

        // Create Sort object
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();

        // Parse date strings to LocalDateTime
        java.time.LocalDateTime startDateTime = null;
        java.time.LocalDateTime endDateTime = null;
        
        if (startDate != null && !startDate.trim().isEmpty()) {
            startDateTime = java.time.LocalDate.parse(startDate).atStartOfDay();
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            endDateTime = java.time.LocalDate.parse(endDate).atTime(23, 59, 59);
        }

        // Call service with filters
        Page<ActivityLogResponse> pageActivityLogs;
        if (userId != null || entityType != null || action != null || startDateTime != null || endDateTime != null) {
            pageActivityLogs = activityLogService.getActivityLogsByFilters(userId, entityType, action, startDateTime, endDateTime, PageRequest.of(page - 1, pageSize, sort));
        } else {
            pageActivityLogs = activityLogService.getAllActivityLogs(PageRequest.of(page - 1, pageSize, sort));
        }

        model.addAttribute("activityLogs", pageActivityLogs.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageActivityLogs.getTotalPages());
        model.addAttribute("totalItems", pageActivityLogs.getTotalElements());
        
        // Add filter attributes
        model.addAttribute("userId", userId);
        model.addAttribute("entityType", entityType);
        model.addAttribute("action", action);
        // model.addAttribute("role", role);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        // Add sort attributes
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        return "admin/activity-logs/list";
    }

    @PostMapping("/delete/{id}")
    public String deleteActivityLog(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            activityLogService.deleteActivityLog(id);
            ra.addFlashAttribute("message", "Activity log has been deleted successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error deleting activity log: " + e.getMessage());
        }
        return "redirect:/admin/activity-logs";
    }

    @PostMapping("/delete-all")
    public String deleteAllActivityLogs(RedirectAttributes ra) {
        try {
            // You might want to add a method in service to delete all
            ra.addFlashAttribute("message", "All activity logs have been deleted successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error deleting all activity logs: " + e.getMessage());
        }
        return "redirect:/admin/activity-logs";
    }
}
