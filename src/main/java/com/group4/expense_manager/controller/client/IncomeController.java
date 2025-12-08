package com.group4.expense_manager.controller.client;

import com.group4.expense_manager.dto.request.IncomeRequest;
import com.group4.expense_manager.dto.response.IncomeResponse;
import com.group4.expense_manager.entity.Income;
import com.group4.expense_manager.entity.User;
import com.group4.expense_manager.mapper.IncomeMapper;
import com.group4.expense_manager.service.IncomeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Controller xử lý các API liên quan đến Thu nhập (Income) cho phía Client (Người dùng cuối).
 * Mọi request đều yêu cầu xác thực và chỉ thao tác trên dữ liệu của chính user đó.
 */
@RestController
@RequestMapping("/api/incomes")
public class IncomeController {

    private final IncomeService incomeService;
    private final IncomeMapper incomeMapper;

    // Sử dụng Constructor Injection thay vì Field Injection để đảm bảo tính immutability và dễ test
    @Autowired
    public IncomeController(IncomeService incomeService, IncomeMapper incomeMapper) {
        this.incomeService = incomeService;
        this.incomeMapper = incomeMapper;
    }

    // ========================================================================
    // 1. LIST & FILTER
    // ========================================================================

    /**
     * Lấy danh sách thu nhập có phân trang và bộ lọc.
     * * @param user       User hiện tại (lấy từ Security Context).
     * @param page       Số trang (bắt đầu từ 0).
     * @param size       Số lượng phần tử mỗi trang.
     * @param categoryId (Optional) ID danh mục cần lọc.
     * @param fromDate   (Optional) Ngày bắt đầu (Format: yyyy-MM-dd).
     * @param toDate     (Optional) Ngày kết thúc (Format: yyyy-MM-dd).
     * @param keyword    (Optional) Từ khóa tìm kiếm trong Source hoặc Note.
     * @return Page<IncomeResponse>
     */
    @GetMapping
    public ResponseEntity<Page<IncomeResponse>> listIncomes(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String keyword
    ) {
        // Sắp xếp mặc định: Ngày thu nhập mới nhất lên đầu
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "incomeDate"));

        Page<Income> incomes = incomeService.filterIncomesOfUser(user, categoryId, fromDate, toDate, keyword, pageable);

        // Map Entity sang DTO Response trước khi trả về
        return ResponseEntity.ok(incomes.map(incomeMapper::toResponse));
    }

    // ========================================================================
    // 2. GET DETAIL
    // ========================================================================

    /**
     * Xem chi tiết một khoản thu nhập.
     * Service sẽ kiểm tra xem Income ID này có thuộc về User này không.
     */
    @GetMapping("/{id}")
    public ResponseEntity<IncomeResponse> getIncome(
            @PathVariable Integer id,
            @AuthenticationPrincipal User user
    ) {
        Income income = incomeService.getIncomeOfUser(id, user);
        return ResponseEntity.ok(incomeMapper.toResponse(income));
    }

    // ========================================================================
    // 3. CREATE
    // ========================================================================

    /**
     * Tạo mới một khoản thu nhập.
     * * @param request DTO chứa thông tin tạo mới (đã validate @Valid).
     * @return HTTP 201 Created kèm dữ liệu vừa tạo.
     */
    @PostMapping
    public ResponseEntity<IncomeResponse> createIncome(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid IncomeRequest request
    ) {
        Income income = incomeService.createIncome(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(incomeMapper.toResponse(income));
    }

    // ========================================================================
    // 4. UPDATE
    // ========================================================================

    /**
     * Cập nhật thông tin khoản thu nhập.
     * * @param id ID của khoản thu nhập cần sửa.
     * @param request DTO chứa thông tin cập nhật.
     * @return HTTP 200 OK kèm dữ liệu đã sửa.
     */
    @PutMapping("/{id}")
    public ResponseEntity<IncomeResponse> updateIncome(
            @PathVariable Integer id,
            @AuthenticationPrincipal User user,
            @RequestBody @Valid IncomeRequest request
    ) {
        Income income = incomeService.updateIncome(id, user, request);
        return ResponseEntity.ok(incomeMapper.toResponse(income));
    }

    // ========================================================================
    // 5. DELETE
    // ========================================================================

    /**
     * Xóa một khoản thu nhập.
     * * @param id ID cần xóa.
     * @return HTTP 204 No Content (Xóa thành công, không trả về body).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncome(
            @PathVariable Integer id,
            @AuthenticationPrincipal User user
    ) {
        incomeService.deleteIncome(id, user);
        return ResponseEntity.noContent().build();
    }
}