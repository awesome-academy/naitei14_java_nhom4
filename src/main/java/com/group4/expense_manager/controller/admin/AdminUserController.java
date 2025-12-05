package com.group4.expense_manager.controller.admin;

import com.group4.expense_manager.dto.request.AdminUserFormRequest;
import com.group4.expense_manager.entity.User;
import com.group4.expense_manager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String listUsers(Model model,
                            @RequestParam(name = "keyword", required = false) String keyword,
                            @RequestParam(name = "status", required = false) Boolean status,
                            @RequestParam(name = "page", defaultValue = "1") int page) {

        int pageSize = 5;
        Page<User> pageUser = userService.getUsers(keyword, status, page, pageSize);

        model.addAttribute("users", pageUser.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageUser.getTotalPages());
        model.addAttribute("totalItems", pageUser.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);

        return "admin/users/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("userForm", new AdminUserFormRequest());
        model.addAttribute("pageTitle", "Create New User");
        return "admin/users/form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        User user = userService.getUserById(id);

        AdminUserFormRequest dto = new AdminUserFormRequest();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setActive(user.isEnabled());
        dto.setDefaultCurrency(user.getDefaultCurrency());

        model.addAttribute("userForm", dto);
        model.addAttribute("pageTitle", "Edit User (ID: " + id + ")");
        return "admin/users/form";
    }

    @PostMapping("/save")
    public String saveUser(@ModelAttribute("userForm") AdminUserFormRequest userDto, RedirectAttributes ra) {
        userService.saveAdminUser(userDto);
        ra.addFlashAttribute("message", "User saved successfully!");
        return "redirect:/admin/users";
    }

    @GetMapping("/detail/{id}")
    public String viewDetail(@PathVariable("id") Integer id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "admin/users/detail";
    }

    // 6. Xóa User
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Integer id, RedirectAttributes ra) {
        try {
            userService.deleteUser(id);
            ra.addFlashAttribute("message", "Delete user successful!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Cannot delete this user (Due to data constraint.");
        }
        return "redirect:/admin/users";
    }
}