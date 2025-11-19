package com.group4.expense_manager.controller;

import com.group4.expense_manager.dto.request.UserRegistrationRequest;
import com.group4.expense_manager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }
        return authentication.isAuthenticated();
    }

    @GetMapping("/login")
    public String showLoginForm() {
        if (isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        if (isAuthenticated()) {
            return "redirect:/dashboard";
        }
        model.addAttribute("userRequest", new UserRegistrationRequest());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userRequest") UserRegistrationRequest userRequest,
                               BindingResult result,
                               Model model) {

        if (result.hasErrors()) {
            return "register";
        }

        if (userService.findByEmail(userRequest.getEmail()).isPresent()) {
            model.addAttribute("emailError", "Email này đã được sử dụng.");
            return "register";
        }

        if (!userRequest.getPassword().equals(userRequest.getConfirmPassword())) {
            model.addAttribute("passwordError", "Mật khẩu và Xác nhận Mật khẩu không khớp.");
            return "register";
        }

        try {
            userService.saveNewUser(userRequest);
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("generalError", "Đăng ký thất bại. Vui lòng thử lại.");
            return "register";
        }
    }
}