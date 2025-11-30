package com.group4.expense_manager.util;

import com.group4.expense_manager.entity.Expense;
import com.group4.expense_manager.entity.User;
import com.group4.expense_manager.dto.response.UserCsvResponse;
import org.apache.commons.csv.*;
import org.springframework.web.multipart.MultipartFile;

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

    public static boolean hasCSVFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType()) || file.getOriginalFilename().endsWith(".csv");
    }
}
