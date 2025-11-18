package com.group4.expense_manager.service.impl;

import com.group4.expense_manager.dto.request.IncomeRequest;
import com.group4.expense_manager.entity.Category;
import com.group4.expense_manager.entity.CategoryType;
import com.group4.expense_manager.entity.Income;
import com.group4.expense_manager.entity.User;
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
    public Page<Income> filterIncomesOfUser(User user, Integer categoryId, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        // Không cần parse String -> LocalDate nữa vì Controller đã làm rồi

        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId).orElse(null);
        }

        // 1. Category + Date
        if (category != null && fromDate != null && toDate != null) {
            return incomeRepository.findByUserAndCategoryAndIncomeDateBetween(user, category, fromDate, toDate, pageable);
        }
        // 2. Category only
        if (category != null) {
            return incomeRepository.findByUserAndCategory(user, category, pageable);
        }
        // 3. Date only
        if (fromDate != null && toDate != null) {
            return incomeRepository.findByUserAndIncomeDateBetween(user, fromDate, toDate, pageable);
        }

        // 4. No filter
        return incomeRepository.findByUser(user, pageable);
    }

    @Override
    public Income getIncomeOfUser(Integer incomeId, User user) {
        Income income = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new RuntimeException("Khoản thu nhập không tồn tại"));

        if (!income.getUser().getId().equals(user.getId())) {
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
        } else {
            income.setRecurringInterval(null);
            income.setRecurringEndDate(null);
        }
    }

    private Category validateCategory(Integer categoryId, User user) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        if (category.getType() != CategoryType.income) {
            throw new RuntimeException("Danh mục này không phải loại Thu nhập (Income).");
        }

        if (category.getUser() != null && !category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền sử dụng danh mục này.");
        }

        return category;
    }
}