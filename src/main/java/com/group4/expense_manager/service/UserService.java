package com.group4.expense_manager.service;

import com.group4.expense_manager.dto.request.UserRegistrationRequest;
import com.group4.expense_manager.dto.request.AdminUserFormRequest;
import com.group4.expense_manager.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface UserService {
    Optional<User> findByEmail(String email);
    User saveNewUser(UserRegistrationRequest userRequest);
    Page<User> getUsers(String keyword, Boolean isActive, int page, int size);
    User getUserById(Integer id);
    void saveAdminUser(AdminUserFormRequest adminUserFormRequest);
    void deleteUser(Integer id);
}