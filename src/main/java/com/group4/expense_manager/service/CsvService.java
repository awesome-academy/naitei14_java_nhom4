package com.group4.expense_manager.service;

import com.group4.expense_manager.dto.response.UserCsvResponse;
import com.group4.expense_manager.dto.request.IncomeCsvRequest;
import com.group4.expense_manager.entity.Category;
import com.group4.expense_manager.entity.Expense;
import com.group4.expense_manager.entity.Income;
import com.group4.expense_manager.entity.User;
import com.group4.expense_manager.repository.CategoryRepository;
import com.group4.expense_manager.repository.ExpenseRepository;
import com.group4.expense_manager.repository.IncomeRepository;
import com.group4.expense_manager.repository.UserRepository;
import com.group4.expense_manager.util.CsvHelper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
@Service
public class CsvService {
    @Autowired private UserRepository userRepository;
    @Autowired private ExpenseRepository expenseRepository;
    @Autowired private IncomeRepository incomeRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    public ByteArrayInputStream loadUserCsv() {
        List<User> users = userRepository.findAll();
                List<UserCsvResponse> userCsvResponses = new ArrayList<>();

        for (User user : users) {
            Double totalExp = expenseRepository.sumAmountByUserId(user.getId());
            Double totalInc = incomeRepository.sumAmountByUserId(user.getId());

            userCsvResponses.add(new UserCsvResponse(
                    user.getId(), user.getName(), user.getEmail(), user.getRole(),
                    totalExp != null ? totalExp : 0.0,
                    totalInc != null ? totalInc : 0.0
            ));
        }
        return CsvHelper.usersToCsv(userCsvResponses);
    }

    public void saveUsersFromCsv(MultipartFile file) {
        try {
            List<User> users = CsvHelper.csvToUsers(file.getInputStream());
            List<User> usersToSave = new ArrayList<>();
            for (User user : users) {
                if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                    continue;
                }
                String rawPassword = user.getPasswordHash();
                user.setPasswordHash(passwordEncoder.encode(rawPassword));
                usersToSave.add(user);
            }
            if (!usersToSave.isEmpty()) {
                userRepository.saveAll(usersToSave);
            }

        } catch (IOException e) {
            throw new RuntimeException("Fail to store CSV data: " + e.getMessage());
        }
    }

    public void saveIncomesFromCsv(MultipartFile file) {
        try {
            // 1. Nhờ Helper đọc file và lấy List DTO
            List<IncomeCsvRequest> dtos = CsvHelper.csvToIncomeDtos(file.getInputStream());

            List<Income> incomesToSave = new ArrayList<>();

            for (IncomeCsvRequest dto : dtos) {
                Income income = new Income();

                // 2. Xử lý Logic tìm DB (Service làm việc này)

                // A. Tìm User
                User user = userRepository.findByEmail(dto.getUserEmail())
                        .orElseThrow(() -> new RuntimeException("User not found: " + dto.getUserEmail()));
                income.setUser(user);

                // B. Tìm Category
                if (dto.getCategoryName() != null && !dto.getCategoryName().equals("N/A")) {
                    Category category = categoryRepository.findByNameAndUserId(dto.getCategoryName(), user.getId())
                            .orElse(null);
                    income.setCategory(category);
                }

                // C. Convert dữ liệu
                income.setIncomeDate(LocalDate.parse(dto.getDate())); // Cần try-catch nếu sợ sai format
                income.setAmount(new BigDecimal(dto.getAmount()));
                income.setSource(dto.getSource());
                income.setNote(dto.getNote());

                // Default value logic
                String currency = (dto.getCurrency() != null && !dto.getCurrency().isEmpty()) ? dto.getCurrency() : "VND";
                income.setCurrency(currency);

                boolean isRecurring = Boolean.parseBoolean(dto.getIsRecurring());
                income.setRecurring(isRecurring);

                incomesToSave.add(income);
            }

            // 3. Lưu vào DB
            incomeRepository.saveAll(incomesToSave);

        } catch (IOException e) {
            throw new RuntimeException("Error processing CSV: " + e.getMessage());
        }
    }

    public ByteArrayInputStream loadIncomeCsv() {
        List<Income> incomes = incomeRepository.findAll();
        return CsvHelper.incomesToCsv(incomes);
    }

}
