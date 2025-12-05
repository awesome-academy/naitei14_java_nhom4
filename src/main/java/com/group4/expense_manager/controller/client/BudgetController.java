package com.group4.expense_manager.controller.client;

import com.group4.expense_manager.dto.request.CreateBudgetRequest;
import com.group4.expense_manager.dto.response.BudgetResponse;
import com.group4.expense_manager.entity.Budget;
import com.group4.expense_manager.entity.User;
import com.group4.expense_manager.mapper.BudgetMapper;
import com.group4.expense_manager.service.BudgetService;
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

import java.time.YearMonth;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {
    
    @Autowired
    private BudgetService budgetService;
    
    @Autowired
    private BudgetMapper budgetMapper;
    
    // LIST + FILTER
    @GetMapping
    public ResponseEntity<Page<BudgetResponse>> listBudgets(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Budget> budgets = budgetService.listBudgets(user, categoryId, month, pageable);
        return ResponseEntity.ok(budgets.map(budgetMapper::toResponse));
    }
    
    // GET DETAIL
    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> getBudget(
            @PathVariable Integer id,
            @AuthenticationPrincipal User user
    ) {
        Budget budget = budgetService.getBudget(id, user);
        return ResponseEntity.ok(budgetMapper.toResponse(budget));
    }
    
    // CREATE
    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid CreateBudgetRequest request
    ) {
        Budget budget = budgetService.createBudget(user, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(budgetMapper.toResponse(budget));
    }
    
    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> updateBudget(
            @PathVariable Integer id,
            @AuthenticationPrincipal User user,
            @RequestBody @Valid CreateBudgetRequest request
    ) {
        Budget budget = budgetService.updateBudget(id, user, request);
        return ResponseEntity.ok(budgetMapper.toResponse(budget));
    }
    
    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(
            @PathVariable Integer id,
            @AuthenticationPrincipal User user
    ) {
        budgetService.deleteBudget(id, user);
        return ResponseEntity.noContent().build();
    }
}