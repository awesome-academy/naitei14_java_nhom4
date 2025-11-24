package com.group4.expense_manager.repository;

import com.group4.expense_manager.entity.Budget;
import com.group4.expense_manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Integer> {

    // Các ngân sách của user còn hiệu lực (giao với khoảng [start, end])
    List<Budget> findByUserAndStartDateLessThanEqualAndEndDateGreaterThanEqual(User user,
                                                                              LocalDate end,
                                                                              LocalDate start);
}
