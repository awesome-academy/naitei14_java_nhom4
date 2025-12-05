
package com.group4.expense_manager.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserFormRequest {
    private Integer id;
    private String name;
    private String email;
    private String password;
    private String role;
    private boolean isActive;
    private String defaultCurrency;
}