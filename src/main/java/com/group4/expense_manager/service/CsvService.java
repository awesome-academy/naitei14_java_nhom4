package com.group4.expense_manager.service;

import com.group4.expense_manager.dto.response.UserCsvResponse;
import com.group4.expense_manager.dto.request.IncomeCsvRequest;
import com.group4.expense_manager.dto.request.BudgetCsvRequest;
import com.group4.expense_manager.entity.Category;
import com.group4.expense_manager.entity.CategoryType;
import com.group4.expense_manager.entity.Expense;
import com.group4.expense_manager.entity.Income;
import com.group4.expense_manager.entity.User;
import com.group4.expense_manager.entity.Budget;
import com.group4.expense_manager.repository.CategoryRepository;
import com.group4.expense_manager.repository.ExpenseRepository;
import com.group4.expense_manager.repository.IncomeRepository;
import com.group4.expense_manager.repository.UserRepository;
import com.group4.expense_manager.repository.BudgetRepository;
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
import java.time.format.DateTimeParseException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
@Service
public class CsvService {
    @Autowired private UserRepository userRepository;
    @Autowired private ExpenseRepository expenseRepository;
    @Autowired private IncomeRepository incomeRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private BudgetRepository budgetRepository;
    @Autowired private IncomeService incomeService;
    @Autowired private BudgetService budgetService;
    @Autowired private UserService userService;

    

    public ByteArrayInputStream loadUserCsv(
            String keyword,
            Boolean status,
            Sort sort
    ) {
        Page<User> pageUser = userService.getUsers(
                keyword,
                status,
                PageRequest.of(0, Integer.MAX_VALUE, sort)
        );
        List<User> users = pageUser.getContent();
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

    // Category CSV Methods
    public ByteArrayInputStream loadCategoryCsv() {
        List<Category> categories = categoryRepository.findByUserIsNull(
                org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)
        ).getContent();
        return CsvHelper.categoriesToCsv(categories);
    }

    public void saveCategoriesFromCsv(MultipartFile file) {
        try {
            if (!CsvHelper.hasCSVFormat(file)) {
                throw new RuntimeException("File không đúng định dạng CSV");
            }
            
            List<Category> categories = CsvHelper.csvToCategories(file.getInputStream());
            List<Category> categoriesToSave = new ArrayList<>();
            
            for (Category category : categories) {
                // Check if category with same name and type already exists
                org.springframework.data.domain.Page<Category> allCategories = categoryRepository.findByUserIsNull(
                        org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)
                );
                
                boolean exists = allCategories.getContent().stream().anyMatch(c -> 
                    c.getName().equalsIgnoreCase(category.getName()) && 
                    c.getType() == category.getType()
                );
                
                if (!exists) {
                    categoriesToSave.add(category);
                }
            }
            
            if (!categoriesToSave.isEmpty()) {
                categoryRepository.saveAll(categoriesToSave);
            }
            
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu CSV data: " + e.getMessage());
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
                    final CategoryType CATEGORY_TYPE = CategoryType.income;
                    Category category = categoryRepository.findByNameAndUserIdAndType(
                                    dto.getCategoryName(),
                                    user.getId(),
                                    CATEGORY_TYPE
                            )
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
            incomeRepository.saveAll(incomesToSave);

        } catch (IOException e) {
            throw new RuntimeException("Error processing CSV: " + e.getMessage());
        }
    }

    public ByteArrayInputStream loadBudgetCsv(Integer userId,
                                              LocalDate startDate,
                                              LocalDate endDate,
                                              String keyword) {
        Page<Budget> budgetPage = budgetService.getAllBudgetsForAdmin(
                userId,
                startDate,
                endDate,
                keyword,
                Pageable.unpaged() // <-- Lấy TOÀN BỘ dữ liệu phù hợp với lọc
        );
        List<Budget> budgets = budgetPage.getContent();
        return CsvHelper.budgetsToCsv(budgets);
    }

    // --- IMPORT BUDGET ---
    public void saveBudgetsFromCsv(MultipartFile file) {
        try {
            List<BudgetCsvRequest> dtos = CsvHelper.csvToBudgetDtos(file.getInputStream());

            List<Budget> budgetsToSave = new ArrayList<>();

            for (BudgetCsvRequest dto : dtos) {
                Budget budget = new Budget();

                User user = userRepository.findByEmail(dto.getUserEmail())
                        .orElseThrow(() -> new RuntimeException("User not found: " + dto.getUserEmail()));
                budget.setUser(user);

                String categoryName = dto.getCategoryName();
                final CategoryType CATEGORY_TYPE = CategoryType.expense;
                Category category = categoryRepository.findByNameAndUserIdAndType(
                                categoryName,
                                user.getId(),
                                CATEGORY_TYPE
                        )
                        .orElseThrow(() -> new RuntimeException("Category EXPENSE not found for user " + user.getEmail() + ": " + categoryName));
                budget.setCategory(category);
                try {
                    budget.setStartDate(LocalDate.parse(dto.getStartDate()));
                    budget.setEndDate(LocalDate.parse(dto.getEndDate()));
                } catch (DateTimeParseException e) {
                    throw new RuntimeException("Invalid Date format in CSV. Expected YYYY-MM-DD.");
                }

                budget.setAmount(new BigDecimal(dto.getAmount()));

                String currency = (dto.getCurrency() != null && !dto.getCurrency().isEmpty()) ? dto.getCurrency() : user.getDefaultCurrency();
                budget.setCurrency(currency);

                // Lưu ý: Logic check trùng lặp (ví dụ: cùng Category, cùng thời gian) nên được xử lý ở đây.

                budgetsToSave.add(budget);
            }
            budgetRepository.saveAll(budgetsToSave);

        } catch (IOException e) {
            throw new RuntimeException("Error processing CSV: " + e.getMessage());
        }
    }

    public ByteArrayInputStream loadIncomeCsv(Integer userId, 
            String keyword, 
            LocalDate startDate, 
            LocalDate endDate) {
        // List<Income> incomes = incomeRepository.findAll();
        Page<Income> incomePage = incomeService.getAllIncomesForAdmin(
            userId, 
            startDate, 
            endDate, 
            keyword,
            Pageable.unpaged() // <-- Yếu tố quyết định để lấy TOÀN BỘ
        );
        return CsvHelper.incomesToCsv(incomePage.getContent());
    }

    // Category CSV Methods
    public ByteArrayInputStream loadCategoryCsv() {
        List<Category> categories = categoryRepository.findByUserIsNull(
                org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)
        ).getContent();
        return CsvHelper.categoriesToCsv(categories);
    }

    public void saveCategoriesFromCsv(MultipartFile file) {
        try {
            if (!CsvHelper.hasCSVFormat(file)) {
                throw new RuntimeException("File không đúng định dạng CSV");
            }
            
            List<Category> categories = CsvHelper.csvToCategories(file.getInputStream());
            List<Category> categoriesToSave = new ArrayList<>();
            
            for (Category category : categories) {
                // Check if category with same name and type already exists
                org.springframework.data.domain.Page<Category> allCategories = categoryRepository.findByUserIsNull(
                        org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)
                );
                
                boolean exists = allCategories.getContent().stream().anyMatch(c -> 
                    c.getName().equalsIgnoreCase(category.getName()) && 
                    c.getType() == category.getType()
                );
                
                if (!exists) {
                    categoriesToSave.add(category);
                }
            }
            
            if (!categoriesToSave.isEmpty()) {
                categoryRepository.saveAll(categoriesToSave);
            }
            
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu CSV data: " + e.getMessage());
        }
    }

}
