package com.group4.expense_manager.service;

import com.group4.expense_manager.dto.request.IncomeRequest;
import com.group4.expense_manager.entity.Income;
import com.group4.expense_manager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface IncomeService {
    Page<Income> listIncomesOfUser(User user, Pageable pageable);

    Page<Income> filterIncomesOfUser(User user, Integer categoryId, LocalDate fromDate, LocalDate toDate, Pageable pageable);

    Income getIncomeOfUser(Integer incomeId, User user);

    Income createIncome(User user, IncomeRequest request);

    Income updateIncome(Integer incomeId, User user, IncomeRequest request);

    void deleteIncome(Integer incomeId, User user);
}
