package com.group4.expense_manager.repository;

import com.group4.expense_manager.entity.Category;
import com.group4.expense_manager.entity.Expense;
import com.group4.expense_manager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Integer> {

    Page<Expense> findByUser(User user, Pageable pageable);

    Page<Expense> findByUserAndDescriptionContainingAndAmountBetween(User user, String description, Double minAmount, Double maxAmount, Pageable pageable);

    Page<Expense> findByUserAndAmountBetween(User user, Double minAmount, Double maxAmount, Pageable pageable);

    Page<Expense> findByUserAndCategory(User user, Category category, Pageable pageable);

    Page<Expense> findByUserAndExpenseDateBetween(User user, LocalDate fromDate, LocalDate toDate, Pageable pageable);

    Page<Expense> findByUserAndCategoryAndExpenseDateBetween(User user, Category category, LocalDate fromDate, LocalDate toDate, Pageable pageable);

    Optional<Expense> findByIdAndUser(Integer id, User user);

    List<Expense> findByUserAndCategory(User user, Category category);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId")
    double sumAmountByUserId(@Param("userId") Integer userId);
    List<Expense> findByUser(User user);

    long countByUser(User user);
}