package com.group4.expense_manager.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("tongChiTieuThang", 0);
        model.addAttribute("tongThuNhapThang", 0);
        model.addAttribute("balance", 0);
        model.addAttribute("titleTrang", "Dashboard");
        return "client/dashboard";
    }
}