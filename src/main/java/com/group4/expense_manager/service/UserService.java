package com.group4.expense_manager.service;

import com.group4.expense_manager.dto.request.UserRegistrationRequest;
import com.group4.expense_manager.dto.request.AdminUserFormRequest;
import com.group4.expense_manager.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Optional<User> findByEmail(String email);
    User saveNewUser(UserRegistrationRequest userRequest);
    Page<User> getUsers(String keyword, Boolean isActive, Pageable pageable);
    User getUserById(Integer id);
    void saveAdminUser(AdminUserFormRequest adminUserFormRequest);
    void deleteUser(Integer id);
}