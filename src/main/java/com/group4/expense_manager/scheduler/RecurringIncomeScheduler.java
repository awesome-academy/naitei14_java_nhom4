package com.group4.expense_manager.scheduler;

import com.group4.expense_manager.entity.Income;
import com.group4.expense_manager.entity.RecurringInterval;
import com.group4.expense_manager.repository.IncomeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class RecurringIncomeScheduler {

    private final IncomeRepository incomeRepository;

    @Autowired
    public RecurringIncomeScheduler(IncomeRepository incomeRepository) {
        this.incomeRepository = incomeRepository;
    }

    // Chạy mỗi ngày lúc 00:05
    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void generateRecurringIncomes() {
        LocalDate today = LocalDate.now();

        // Lấy tất cả income recurring tới hạn
        List<Income> templates = incomeRepository
                .findByIsRecurringTrueAndNextOccurrenceDateNotNullAndNextOccurrenceDateLessThanEqual(today);

        for (Income template : templates) {
            LocalDate nextDate = template.getNextOccurrenceDate();

            // Nếu có ngày kết thúc và đã quá hạn thì bỏ qua
            if (template.getRecurringEndDate() != null
                    && nextDate.isAfter(template.getRecurringEndDate())) {
                continue;
            }

            // Có thể dùng while để "bù" những ngày bị miss, nhưng cho đơn giản cứ sinh 1 lần / ngày
            // Nếu muốn bù nhiều kỳ, có thể dùng while (nextDate <= today) { ... }
            while (!nextDate.isAfter(today)) {
                // Kiểm tra lại endDate
                if (template.getRecurringEndDate() != null
                        && nextDate.isAfter(template.getRecurringEndDate())) {
                    break;
                }

                // Tạo bản ghi income mới từ template
                Income newIncome = new Income();
                newIncome.setUser(template.getUser());
                newIncome.setCategory(template.getCategory());
                newIncome.setSource(template.getSource());
                newIncome.setAmount(template.getAmount());
                newIncome.setCurrency(template.getCurrency());
                newIncome.setIncomeDate(nextDate);
                newIncome.setNote(template.getNote());

                // Bản ghi phát sinh thực tế không cần là recurring nữa
                newIncome.setRecurring(false);
                newIncome.setRecurringInterval(null);
                newIncome.setRecurringEndDate(null);
                newIncome.setNextOccurrenceDate(null);

                incomeRepository.save(newIncome);

                // Cập nhật nextOccurrenceDate cho template
                nextDate = getNextDate(nextDate, template.getRecurringInterval());
                template.setNextOccurrenceDate(nextDate);
            }

            incomeRepository.save(template);
        }
    }

    private LocalDate getNextDate(LocalDate current, RecurringInterval interval) {
        return switch (interval) {
            case DAILY -> current.plusDays(1);
            case WEEKLY -> current.plusWeeks(1);
            case MONTHLY -> current.plusMonths(1);
            case YEARLY -> current.plusYears(1);
        };
    }
}
