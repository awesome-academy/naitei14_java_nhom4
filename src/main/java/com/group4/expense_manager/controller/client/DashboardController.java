package com.group4.expense_manager.controller.client;

import com.group4.expense_manager.entity.User;
import com.group4.expense_manager.service.DashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.YearMonth;

@Controller
public class DashboardController {

	private final DashboardService dashboardService;

	public DashboardController(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	@GetMapping("/dashboard")
	public String showDashboard(@AuthenticationPrincipal User user, Model model) {
		YearMonth currentMonth = YearMonth.now();

		model.addAttribute("titleTrang", "Dashboard");
		model.addAttribute("tongThuNhapThang", dashboardService.getTotalIncomeOfMonth(user, currentMonth));
		model.addAttribute("incomesThangNay", dashboardService.getIncomesOfMonth(user, currentMonth));
		model.addAttribute("budgetsHienTai", dashboardService.getActiveBudgetsOfMonth(user, currentMonth));

		return "client/dashboard";
	}
}
