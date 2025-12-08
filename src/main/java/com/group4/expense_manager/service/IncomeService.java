package com.group4.expense_manager.service;

import com.group4.expense_manager.dto.request.IncomeRequest;
import com.group4.expense_manager.entity.Income;
import com.group4.expense_manager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface IncomeService {
    // ========================================================================
    // CLIENT METHODS
    // ========================================================================
    Page<Income> listIncomesOfUser(User user, Pageable pageable);

    Page<Income> filterIncomesOfUser(User user, Integer categoryId, LocalDate fromDate, LocalDate toDate, String keyword, Pageable pageable);

    Income getIncomeOfUser(Integer incomeId, User user);

    Income createIncome(User user, IncomeRequest request);

    Income updateIncome(Integer incomeId, User user, IncomeRequest request);

    void deleteIncome(Integer incomeId, User user);

    // ========================================================================
    // ADMIN METHODS
    // ========================================================================

    // Lấy danh sách cho Admin (Search toàn bộ hệ thống)
    Page<Income> getAllIncomesForAdmin(Integer userId, LocalDate startDate, LocalDate endDate, String keyword, Pageable pageable);

    // Update dành cho Admin (Không check quyền sở hữu) --> MỚI THÊM
    void adminUpdateIncome(Income income);

    // Delete dành cho Admin (Force delete)
    void deleteIncomeById(Integer id);
}