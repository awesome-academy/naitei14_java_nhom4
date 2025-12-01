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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/incomes")
public class IncomeController {

    @Autowired
    private IncomeService incomeService;

    @Autowired
    private IncomeMapper incomeMapper;

    // ====================================================
    // LIST + FILTER
    // ====================================================
    @GetMapping
    public ResponseEntity<Page<IncomeResponse>> listIncomes(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer categoryId,
            //Tự động convert String (yyyy-MM-dd) sang LocalDate
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String keyword
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Income> incomes = incomeService.filterIncomesOfUser(user, categoryId, fromDate, toDate, keyword, pageable);

        // Trả về 200 OK
        return ResponseEntity.ok(incomes.map(incomeMapper::toResponse));
    }

    // ====================================================
    // GET DETAIL
    // ====================================================
    @GetMapping("/{id}")
    public ResponseEntity<IncomeResponse> getIncome(
            @PathVariable Integer id,
            @AuthenticationPrincipal User user
    ) {
        Income income = incomeService.getIncomeOfUser(id, user);
        return ResponseEntity.ok(incomeMapper.toResponse(income));
    }

    // ====================================================
    // CREATE
    // ====================================================
    @PostMapping
    public ResponseEntity<IncomeResponse> createIncome(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid IncomeRequest request // SỬA: Thêm @Valid để kích hoạt validate
    ) {
        Income income = incomeService.createIncome(user, request);
        // Trả về 201 CREATED chuẩn REST
        return ResponseEntity.status(HttpStatus.CREATED).body(incomeMapper.toResponse(income));
    }

    // ====================================================
    // UPDATE
    // ====================================================
    @PutMapping("/{id}")
    public ResponseEntity<IncomeResponse> updateIncome(
            @PathVariable Integer id,
            @AuthenticationPrincipal User user,
            @RequestBody @Valid IncomeRequest request // SỬA: Thêm @Valid
    ) {
        Income income = incomeService.updateIncome(id, user, request);
        return ResponseEntity.ok(incomeMapper.toResponse(income));
    }

    // ====================================================
    // DELETE
    // ====================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncome( // SỬA: Trả về Void vì không có body
                                              @PathVariable Integer id,
                                              @AuthenticationPrincipal User user
    ) {
        incomeService.deleteIncome(id, user);
        // Trả về 204 NO CONTENT (Chuẩn cho hành động xóa thành công)
        return ResponseEntity.noContent().build();
    }
}