package com.group4.expense_manager.util;

import com.group4.expense_manager.entity.Expense;
import com.group4.expense_manager.entity.Income;
import com.group4.expense_manager.entity.User;
import com.group4.expense_manager.entity.Category;
import com.group4.expense_manager.entity.Budget;
import com.group4.expense_manager.entity.Category;
import com.group4.expense_manager.dto.response.UserCsvResponse;
import com.group4.expense_manager.dto.request.IncomeCsvRequest;
import com.group4.expense_manager.dto.request.BudgetCsvRequest;
import org.apache.commons.csv.*;
import org.springframework.web.multipart.MultipartFile;
import com.group4.expense_manager.entity.CategoryType;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CsvHelper {
    public static String TYPE = "text/csv";

    public static ByteArrayInputStream usersToCsv(List<UserCsvResponse> users) {
        final CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("ID", "Name", "Email", "Role", "Total Expenses", "Total Incomes", "Balance")
                .build();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)), format)) {

            for (UserCsvResponse user : users) {
                csvPrinter.printRecord(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getTotalExpense(),
                        user.getTotalIncome(),
                        user.getBalance()
                );
            }

            csvPrinter.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi ghi dữ liệu CSV User: " + e.getMessage());
        }
    }

    public static List<User> csvToUsers(InputStream is) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader,
                     CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).build())) {

            List<User> users = new ArrayList<>();
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecords) {
                User user = new User();
                user.setName(csvRecord.get("Name"));
                user.setEmail(csvRecord.get("Email"));
                user.setPasswordHash(csvRecord.get("Password"));
                String role = csvRecord.get("Role");
                user.setRole((role != null && !role.isEmpty()) ? role.toUpperCase() : "CLIENT");
                String activeStr = csvRecord.get("Active");
                user.setIsActive(Boolean.parseBoolean(activeStr)); // "true" -> true, khác -> false
                String currency = csvRecord.get("Currency");
                user.setDefaultCurrency((currency != null && !currency.isEmpty()) ? currency : "VND");
                users.add(user);
            }
            return users;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi đọc file CSV User: " + e.getMessage());
        }
    }

    public static ByteArrayInputStream incomesToCsv(List<Income> incomes) {
        // Định nghĩa Header cho file CSV
        final CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("ID", "Date", "Source", "Category Name", "Amount", "Currency", "Is Recurring", "Note", "User Email")
                .build();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), format)) {

            for (Income income : incomes) {
                csvPrinter.printRecord(
                        income.getId(),
                        income.getIncomeDate(),
                        income.getSource(),
                        income.getCategory() != null ? income.getCategory().getName() : "N/A", // Xử lý nếu Category null
                        income.getAmount(),
                        income.getCurrency(),
                        income.isRecurring(),
                        income.getNote(),
                        income.getUser().getEmail() // Quan trọng để map lại khi import
                );
            }

            csvPrinter.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error writing Income CSV data: " + e.getMessage());
        }
    }

    public static List<IncomeCsvRequest> csvToIncomeDtos(InputStream is) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader,
                     CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).build())) {

            List<IncomeCsvRequest> dtos = new ArrayList<>();
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecords) {
                IncomeCsvRequest dto = new IncomeCsvRequest();

                // Đọc dữ liệu thô (Raw Data)
                dto.setDate(csvRecord.get("Date"));
                dto.setSource(csvRecord.get("Source"));
                dto.setCategoryName(csvRecord.get("Category Name"));
                dto.setAmount(csvRecord.get("Amount"));
                dto.setCurrency(csvRecord.get("Currency"));
                dto.setIsRecurring(csvRecord.get("Is Recurring"));
                dto.setNote(csvRecord.get("Note"));
                dto.setUserEmail(csvRecord.get("User Email"));

                dtos.add(dto);
            }
            return dtos;

        } catch (IOException e) {
            throw new RuntimeException("Fail to parse CSV file: " + e.getMessage());
        }
    }

    public static ByteArrayInputStream budgetsToCsv(List<Budget> budgets) {
        final CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("ID", "Category Name", "Amount", "Currency", "Start Date", "End Date", "User Email")
                .build();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), format)) {

            for (Budget budget : budgets) {
                csvPrinter.printRecord(
                        budget.getId(),
                        budget.getCategory().getName(),
                        budget.getAmount(),
                        budget.getCurrency(),
                        budget.getStartDate(),
                        budget.getEndDate(),
                        budget.getUser().getEmail()
                );
            }
            csvPrinter.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error writing Budget CSV data: " + e.getMessage());
        }
    }

    // --- 8. IMPORT BUDGETS (Helper chỉ parse ra DTO) ---
    public static List<BudgetCsvRequest> csvToBudgetDtos(InputStream is) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader,
                     CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).build())) {

            List<BudgetCsvRequest> dtos = new ArrayList<>();
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecords) {
                BudgetCsvRequest dto = new BudgetCsvRequest();

                dto.setCategoryName(csvRecord.get("Category Name"));
                dto.setAmount(csvRecord.get("Amount"));
                dto.setCurrency(csvRecord.get("Currency"));
                dto.setStartDate(csvRecord.get("Start Date"));
                dto.setEndDate(csvRecord.get("End Date"));
                dto.setUserEmail(csvRecord.get("User Email"));

                dtos.add(dto);
            }
            return dtos;

        } catch (IOException e) {
            throw new RuntimeException("Fail to parse CSV file: " + e.getMessage());
        }
    }

    public static boolean hasCSVFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType()) || file.getOriginalFilename().endsWith(".csv");
    }

    // Category CSV Export/Import
    public static ByteArrayInputStream categoriesToCsv(List<Category> categories) {
        final CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("ID", "Name", "Description", "Icon", "Type", "Created At", "Updated At")
                .build();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)), format)) {

            for (Category category : categories) {
                csvPrinter.printRecord(
                        category.getId(),
                        category.getName(),
                        category.getDescription() != null ? category.getDescription() : "",
                        category.getIcon() != null ? category.getIcon() : "",
                        category.getType(),
                        category.getCreatedAt(),
                        category.getUpdatedAt()
                );
            }

            csvPrinter.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi ghi dữ liệu CSV Category: " + e.getMessage());
        }
    }

    public static List<Category> csvToCategories(InputStream is) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader,
                     CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).build())) {

            List<Category> categories = new ArrayList<>();
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecords) {
                Category category = new Category();
                
                String name = csvRecord.get("Name");
                if (name == null || name.trim().isEmpty()) {
                    throw new RuntimeException("Tên danh mục không được để trống (dòng " + csvRecord.getRecordNumber() + ")");
                }
                
                category.setName(name);
                category.setDescription(csvRecord.get("Description"));
                category.setIcon(csvRecord.get("Icon"));
                
                String typeStr = csvRecord.get("Type");
                try {
                    category.setType(CategoryType.valueOf(typeStr));
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Type không hợp lệ (dòng " + csvRecord.getRecordNumber() + "): " + typeStr);
                }
                
                category.setUser(null); // Global category
                category.setDeleted(false);
                
                categories.add(category);
            }
            return categories;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi đọc file CSV Category: " + e.getMessage());
        }
    }
}
