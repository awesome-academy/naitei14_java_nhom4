package com.group4.expense_manager.service.impl;

import com.group4.expense_manager.dto.request.UserRegistrationRequest;
import com.group4.expense_manager.entity.User;
import com.group4.expense_manager.repository.UserRepository;
import com.group4.expense_manager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User saveNewUser(UserRegistrationRequest userRequest) {
        User newUser = new User();

        newUser.setName(userRequest.getName());
        newUser.setEmail(userRequest.getEmail());

        String encodedPassword = passwordEncoder.encode(userRequest.getPassword());
        newUser.setPasswordHash(encodedPassword);

        newUser.setRole("client");
        newUser.setIsActive(true);

        return userRepository.save(newUser);
    }
}