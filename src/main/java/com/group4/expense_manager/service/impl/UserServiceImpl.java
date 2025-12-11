package com.group4.expense_manager.service.impl;

import com.group4.expense_manager.dto.request.UserRegistrationRequest;
import com.group4.expense_manager.entity.User;
import com.group4.expense_manager.repository.UserRepository;
import com.group4.expense_manager.service.UserService;
import com.group4.expense_manager.dto.request.AdminUserFormRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.group4.expense_manager.exception.ResourceNotFoundException;

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
    public Page<User> getUsers(String keyword, Boolean isActive, Pageable pageable) {
        return userRepository.searchUsers(keyword, isActive, pageable);
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

        // Kiểm tra xem user có chọn tiền tệ không, nếu có thì set, không thì mặc định VND
        if (userRequest.getDefaultCurrency() != null && !userRequest.getDefaultCurrency().isEmpty()) {
            newUser.setDefaultCurrency(userRequest.getDefaultCurrency());
        } else {
            newUser.setDefaultCurrency("VND");
        }

        return userRepository.save(newUser);
    }
    @Override
    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found user id: " + id));
    }

    @Override
    public void saveAdminUser(AdminUserFormRequest dto) {
        User user;
        if (dto.getId() != null) {
            user = getUserById(dto.getId());
        } else {
            user = new User();
        }
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setIsActive(dto.isActive());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        userRepository.save(user);
    }

    @Override
    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }
}