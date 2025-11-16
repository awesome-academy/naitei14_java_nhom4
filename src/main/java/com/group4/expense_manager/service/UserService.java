package com.group4.expense_manager.service;

import com.group4.expense_manager.dto.request.UserRegistrationRequest;
import com.group4.expense_manager.entity.User;
import java.util.Optional;

public interface UserService {

    Optional<User> findByEmail(String email);

    User saveNewUser(UserRegistrationRequest userRequest);
}