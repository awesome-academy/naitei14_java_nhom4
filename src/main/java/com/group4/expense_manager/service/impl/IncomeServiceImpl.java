package com.group4.expense_manager.service.impl;

import com.group4.expense_manager.dto.request.IncomeRequest;
import com.group4.expense_manager.entity.*;
import com.group4.expense_manager.exception.ResourceNotFoundException;
import com.group4.expense_manager.repository.CategoryRepository;
import com.group4.expense_manager.repository.IncomeRepository;
import com.group4.expense_manager.service.IncomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public IncomeServiceImpl(IncomeRepository incomeRepository, CategoryRepository categoryRepository) {
        this.incomeRepository = incomeRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Page<Income> listIncomesOfUser(User user, Pageable pageable) {
        return incomeRepository.findByUser(user, pageable);
    }

    @Override
    public Page<Income> filterIncomesOfUser(
            User user,
            Integer categoryId,
            LocalDate fromDate,
            LocalDate toDate,
            String keyword,
            Pageable pageable
    ) {
        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại"));
        }

        return incomeRepository.searchIncomes(
                user,
                category,
                fromDate,
                toDate,
                keyword,
                pageable
        );
    }

    @Override
    public Income getIncomeOfUser(Integer incomeId, User user) {
        Income income = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new ResourceNotFoundException("Khoản thu nhập không tồn tại"));

        if (!income.getUser().getId().equals(user.getId())) {
            // quyền truy cập sai -> để RuntimeException, GlobalExceptionHandler sẽ trả 400
            throw new RuntimeException("Bạn không có quyền truy cập khoản thu nhập này");
        }
        return income;
    }

    @Override
    @Transactional
    public Income createIncome(User user, IncomeRequest request) {
        Income income = new Income();
        mapRequestToEntity(request, income, user);
        return incomeRepository.save(income);
    }

    @Override
    @Transactional
    public Income updateIncome(Integer incomeId, User user, IncomeRequest request) {
        Income income = getIncomeOfUser(incomeId, user); // Đã check quyền
        mapRequestToEntity(request, income, user);
        return incomeRepository.save(income);
    }

    @Override
    @Transactional
    public void deleteIncome(Integer incomeId, User user) {
        Income income = getIncomeOfUser(incomeId, user);
        incomeRepository.delete(income);
    }

    // --- HELPER METHODS ---

    private void mapRequestToEntity(IncomeRequest request, Income income, User user) {
        income.setUser(user);
        income.setSource(request.getSource());
        income.setAmount(request.getAmount());
        income.setIncomeDate(request.getIncomeDate());
        income.setNote(request.getNote());

        // Validate Category
        if (request.getCategoryId() != null) {
            Category category = validateCategory(request.getCategoryId(), user);
            income.setCategory(category);
        } else {
            income.setCategory(null);
        }

        // Currency Logic (Fallback)
        String currency = request.getCurrency();
        if (currency == null || currency.isBlank()) {
            currency = user.getDefaultCurrency();
        }
        income.setCurrency(currency);

        // Recurring Logic
        boolean recurring = (request.getIsRecurring() != null && request.getIsRecurring());
        income.setRecurring(recurring);

        if (recurring) {
            if (request.getRecurringInterval() == null) {
                throw new RuntimeException("Vui lòng chọn chu kỳ lặp lại.");
            }
            income.setRecurringInterval(request.getRecurringInterval());
            income.setRecurringEndDate(request.getRecurringEndDate());

            // Nếu là income mới hoặc trước đó không recurring → set nextOccurrenceDate từ incomeDate
            if (income.getId() == null || !income.isRecurring()) {
                income.setNextOccurrenceDate(
                        calculateNextOccurrenceDate(request.getIncomeDate(), request.getRecurringInterval())
                );
            } else {
                // Nếu user đổi incomeDate hoặc interval, ta cũng có thể recal lại:
                income.setNextOccurrenceDate(
                        calculateNextOccurrenceDate(request.getIncomeDate(), request.getRecurringInterval())
                );
            }

        } else {
            // Không recurring nữa
            income.setRecurringInterval(null);
            income.setRecurringEndDate(null);
            income.setNextOccurrenceDate(null);
        }
    }

    private Category validateCategory(Integer categoryId, User user) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại"));

        if (category.getType() != CategoryType.income) {
            throw new RuntimeException("Danh mục này không phải loại Thu nhập (Income).");
        }

        if (category.getUser() != null && !category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền sử dụng danh mục này.");
        }

        return category;
    }

    private LocalDate calculateNextOccurrenceDate(LocalDate baseDate, RecurringInterval interval) {
        if (baseDate == null || interval == null) return null;

        return switch (interval) {
            case DAILY -> baseDate.plusDays(1);
            case WEEKLY -> baseDate.plusWeeks(1);
            case MONTHLY -> baseDate.plusMonths(1);
            case YEARLY -> baseDate.plusYears(1);
        };
    }

}

