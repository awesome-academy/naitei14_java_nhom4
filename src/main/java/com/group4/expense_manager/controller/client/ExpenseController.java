
package com.group4.expense_manager.controller.client;

import com.group4.expense_manager.entity.Expense;
import com.group4.expense_manager.entity.User;
import com.group4.expense_manager.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * RestController xử lý các API liên quan đến Chi tiêu (Expense) cho phía Client (Người dùng cuối).
 * Mọi request đều yêu cầu xác thực và chỉ thao tác trên dữ liệu của chính user đó.
 */
@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Autowired
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // ========================================================================
    // 1. LIST & FILTER
    // ========================================================================
    /**
     * Lấy danh sách chi tiêu có phân trang và bộ lọc.
     * @param user       User hiện tại (lấy từ Security Context).
     * @param page       Số trang (bắt đầu từ 0).
     * @param size       Số lượng phần tử mỗi trang.
     * @param categoryId (Optional) ID danh mục cần lọc.
     * @param fromDate   (Optional) Ngày bắt đầu (Format: yyyy-MM-dd).
     * @param toDate     (Optional) Ngày kết thúc (Format: yyyy-MM-dd).
     * @param search     (Optional) Từ khóa tìm kiếm.
     * @param minAmount  (Optional) Số tiền tối thiểu.
     * @param maxAmount  (Optional) Số tiền tối đa.
     * @return Page<Expense>
     */
    @GetMapping
    public ResponseEntity<Page<Expense>> listExpenses(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Expense> expenses = expenseService.filterExpensesOfUser(
                user, categoryId, fromDate, toDate, pageable, search, minAmount, maxAmount
        );
        return ResponseEntity.ok(expenses);
    }

    // ========================================================================
    // 2. GET DETAIL
    // ========================================================================
    /**
     * Xem chi tiết một khoản chi tiêu.
     * Service sẽ kiểm tra xem Expense ID này có thuộc về User này không.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Expense> getExpenseDetail(
            @PathVariable Integer id,
            @AuthenticationPrincipal User user
    ) {
        Expense expense = expenseService.getExpenseOfUser(id, user);
        return ResponseEntity.ok(expense);
    }

    // ========================================================================
    // 3. CREATE
    // ========================================================================
    /**
     * Tạo mới một khoản chi tiêu.
     * @param expense Entity chứa thông tin tạo mới (đã validate @Valid).
     * @return HTTP 201 Created kèm dữ liệu vừa tạo.
     */
    @PostMapping
    public ResponseEntity<Expense> createExpense(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid Expense expense
    ) {
        Expense created = expenseService.createExpense(user, expense);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ========================================================================
    // 4. UPDATE
    // ========================================================================
    /**
     * Cập nhật thông tin khoản chi tiêu.
     * @param id ID của khoản chi tiêu cần sửa.
     * @param expense Entity chứa thông tin cập nhật.
     * @return HTTP 200 OK kèm dữ liệu đã sửa.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(
            @PathVariable Integer id,
            @AuthenticationPrincipal User user,
            @RequestBody @Valid Expense expense
    ) {
        Expense updated = expenseService.updateExpense(id, user, expense);
        return ResponseEntity.ok(updated);
    }

    // ========================================================================
    // 5. DELETE
    // ========================================================================
    /**
     * Xóa một khoản chi tiêu.
     * @param id ID cần xóa.
     * @return HTTP 204 No Content (Xóa thành công, không trả về body).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Integer id,
            @AuthenticationPrincipal User user
    ) {
        expenseService.deleteExpense(id, user);
        return ResponseEntity.noContent().build();
    }
}
