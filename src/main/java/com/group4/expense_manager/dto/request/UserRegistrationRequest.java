package com.group4.expense_manager.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationRequest {

    @NotEmpty(message = "Tên không được để trống")
    private String name;

    @NotEmpty(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;


    @NotEmpty(message = "Mật khẩu không được để trống.")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự.")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
            message = "Mật khẩu phải chứa ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt."
    )
    private String password;

    @NotEmpty(message = "Xác nhận mật khẩu không được để trống")
    private String confirmPassword;
    // --- THÊM MỚI: Để người dùng chọn tiền tệ lúc đăng ký ---
    // Nếu không chọn thì Service sẽ tự set là VND
    private String defaultCurrency;
}