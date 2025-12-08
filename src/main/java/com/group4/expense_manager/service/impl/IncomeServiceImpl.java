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

    // =================================================================================
    // PHẦN 1: CLIENT METHODS (Dành cho người dùng thường)
    // Các hàm này luôn yêu cầu User để đảm bảo tính bảo mật (Chỉ xem/sửa dữ liệu của chính mình)
    // =================================================================================

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

        // Nếu có lọc theo danh mục, cần kiểm tra danh mục đó có hợp lệ với user này không
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại"));

            // Bảo mật: Không cho phép User A lọc theo Category của User B
            if (category.getUser() != null && !category.getUser().getId().equals(user.getId())) {
                throw new ResourceNotFoundException("Danh mục không tồn tại hoặc bạn không có quyền truy cập.");
            }
        }

        // Gọi Repository để tìm kiếm theo các tiêu chí (Dynamic Query)
        return incomeRepository.searchIncomes(user, category, fromDate, toDate, keyword, pageable);
    }

    @Override
    public Income getIncomeOfUser(Integer incomeId, User user) {
        Income income = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new ResourceNotFoundException("Khoản thu nhập không tồn tại"));

        // Bảo mật: Chặn nếu user cố tình truy cập ID không phải của mình
        // (Nếu là Admin gọi hàm này thì có thể truyền user = null để bypass check quyền - tùy logic, nhưng ở đây ta có hàm riêng cho Admin)
        if (user != null && !income.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền truy cập khoản thu nhập này");
        }
        return income;
    }

    @Override
    @Transactional
    public Income createIncome(User user, IncomeRequest request) {
        Income income = new Income();
        // Map dữ liệu từ DTO sang Entity và xử lý các logic nghiệp vụ (Currency, Recurring...)
        mapRequestToEntity(request, income, user);
        return incomeRepository.save(income);
    }

    @Override
    @Transactional
    public Income updateIncome(Integer incomeId, User user, IncomeRequest request) {
        // Lấy income cũ lên và check quyền sở hữu trước khi update
        Income income = getIncomeOfUser(incomeId, user);

        // Cập nhật thông tin mới
        mapRequestToEntity(request, income, user);
        return incomeRepository.save(income);
    }

    @Override
    @Transactional
    public void deleteIncome(Integer incomeId, User user) {
        // Lấy income và check quyền sở hữu
        Income income = getIncomeOfUser(incomeId, user);
        incomeRepository.delete(income);
    }

    // =================================================================================
    // PHẦN 2: ADMIN METHODS (Dành cho trang quản trị)
    // Các hàm này có quyền lực cao hơn, không bị giới hạn bởi quyền sở hữu cá nhân
    // =================================================================================

    @Override
    public Page<Income> getAllIncomesForAdmin(Integer userId, LocalDate startDate, LocalDate endDate, String keyword, Pageable pageable) {
        // Gọi Query tìm kiếm toàn cục (Global Search)
        return incomeRepository.searchIncomesForAdmin(userId, startDate, endDate, keyword, pageable);
    }

    @Override
    @Transactional
    public void adminUpdateIncome(Income incomeData) {
        Income existingIncome = incomeRepository.findById(incomeData.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Khoản thu nhập không tồn tại (ID: " + incomeData.getId() + ")"));

        // 1. Cập nhật các trường cơ bản (Partial Update)
        if (incomeData.getSource() != null && !incomeData.getSource().trim().isEmpty()) {
            existingIncome.setSource(incomeData.getSource());
        }
        if (incomeData.getAmount() != null) {
            existingIncome.setAmount(incomeData.getAmount());
        }

        // Nếu form gửi currency lên thì update, không thì giữ nguyên
        if (incomeData.getCurrency() != null && !incomeData.getCurrency().isEmpty()) {
            existingIncome.setCurrency(incomeData.getCurrency());
        }
        if (incomeData.getIncomeDate() != null) {
            existingIncome.setIncomeDate(incomeData.getIncomeDate());
        }
        if (incomeData.getCategory() != null) {
            existingIncome.setCategory(incomeData.getCategory());
        }



        // 2. Xử lý Ghi chú (Note) - Cho phép xóa trắng
        // Chỉ khi biến incomeData.getNote() != null (tức là form có gửi trường này lên) thì mới update.
        // Nếu form gửi chuỗi rỗng "", ta vẫn set vào để xóa ghi chú cũ.
        if (incomeData.getNote() != null) {
            existingIncome.setNote(incomeData.getNote());
        }

        // 3. Xử lý Recurring (Lặp lại)
        // Luôn cập nhật trạng thái bật/tắt (vì checkbox luôn gửi true/false)
        existingIncome.setRecurring(incomeData.isRecurring());

        if (incomeData.isRecurring()) {
            // Nếu bật lặp -> Cập nhật chu kỳ và ngày kết thúc
            if (incomeData.getRecurringInterval() != null) {
                existingIncome.setRecurringInterval(incomeData.getRecurringInterval());
            }
            // Ngày kết thúc có thể null (vô hạn) hoặc có giá trị
            existingIncome.setRecurringEndDate(incomeData.getRecurringEndDate());

            // Nếu đang bật lặp mà chưa có ngày chạy tiếp theo (do mới chuyển từ thường sang lặp) -> Tính toán
            if (existingIncome.getNextOccurrenceDate() == null) {
                existingIncome.setNextOccurrenceDate(
                        calculateNextOccurrenceDate(existingIncome.getIncomeDate(), existingIncome.getRecurringInterval())
                );
            }
        } else {
            // Nếu tắt lặp -> Xóa sạch cấu hình lặp
            existingIncome.setRecurringInterval(null);
            existingIncome.setRecurringEndDate(null);
            existingIncome.setNextOccurrenceDate(null);
        }

        incomeRepository.save(existingIncome);
    }

    @Override
    @Transactional
    public void deleteIncomeById(Integer id) {
        // Admin xóa trực tiếp (Force Delete), chỉ cần check tồn tại
        if (!incomeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Khoản thu nhập không tồn tại.");
        }
        incomeRepository.deleteById(id);
    }

    // =================================================================================
    // PHẦN 3: HELPER METHODS (Các hàm phụ trợ, xử lý logic dùng chung)
    // =================================================================================

    /**
     * Hàm trung gian để map dữ liệu từ DTO (Request) sang Entity (Income).
     * Xử lý các logic: Validate Category, Default Currency, Recurring Calculation.
     */
    private void mapRequestToEntity(IncomeRequest request, Income income, User user) {
        // 1. Map các trường thông tin cơ bản
        income.setUser(user);
        income.setSource(request.getSource());
        income.setAmount(request.getAmount());
        income.setIncomeDate(request.getIncomeDate());
        income.setNote(request.getNote());

        // 2. Xử lý Category (Danh mục)
        if (request.getCategoryId() != null) {
            Category category = validateCategory(request.getCategoryId(), user);
            income.setCategory(category);
        } else {
            income.setCategory(null);
        }

        // 3. Xử lý Currency (Tiền tệ)
        // Nếu client không gửi tiền tệ, lấy mặc định của User (VND/USD...)
        String currency = request.getCurrency();
        if (currency == null || currency.isBlank()) {
            currency = user.getDefaultCurrency();
        }
        income.setCurrency(currency);

        // 4. Xử lý Recurring (Thu nhập định kỳ - Tự động lặp lại)
        boolean isRecurring = (request.getIsRecurring() != null && request.getIsRecurring());
        income.setRecurring(isRecurring);

        if (isRecurring) {
            // Nếu bật lặp lại, bắt buộc phải chọn chu kỳ (Ngày/Tuần/Tháng...)
            if (request.getRecurringInterval() == null) {
                throw new RuntimeException("Vui lòng chọn chu kỳ lặp lại (Ngày, Tuần, Tháng...).");
            }

            income.setRecurringInterval(request.getRecurringInterval());
            income.setRecurringEndDate(request.getRecurringEndDate());

            // --- LOGIC TÍNH NGÀY KẾ TIẾP (Next Occurrence) ---
            // Trường hợp A: Tạo mới (id null) hoặc bản ghi cũ chưa từng bật recurring
            // => Tính ngày kế tiếp dựa trên ngày thu nhập hiện tại
            if (income.getId() == null || income.getNextOccurrenceDate() == null) {
                income.setNextOccurrenceDate(
                        calculateNextOccurrenceDate(request.getIncomeDate(), request.getRecurringInterval())
                );
            }
            // Trường hợp B: Đang sửa một bản ghi recurring có sẵn
            // => Tính lại ngày kế tiếp để đồng bộ với thay đổi của user
            else {
                income.setNextOccurrenceDate(
                        calculateNextOccurrenceDate(request.getIncomeDate(), request.getRecurringInterval())
                );
            }

        } else {
            // Nếu user tắt chế độ lặp lại => Xóa sạch các thông tin liên quan
            income.setRecurringInterval(null);
            income.setRecurringEndDate(null);
            income.setNextOccurrenceDate(null);
        }
    }

    /**
     * Kiểm tra tính hợp lệ của Category.
     * Category phải tồn tại, phải là loại INCOME, và phải thuộc về User đó (hoặc là System Category).
     */
    private Category validateCategory(Integer categoryId, User user) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại"));

        // Kiểm tra loại: Phải là Income
        if (category.getType() != CategoryType.income) {
            throw new RuntimeException("Danh mục này không phải loại Thu nhập (Income).");
        }

        // Kiểm tra quyền sở hữu: Của user này HOẶC là danh mục chung (user_id = null)
        if (category.getUser() != null && !category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền sử dụng danh mục này.");
        }

        return category;
    }

    /**
     * Tính toán ngày lặp lại tiếp theo dựa trên chu kỳ.
     * Ví dụ: Hôm nay 01/01, lặp MONTHLY => Trả về 01/02.
     */
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