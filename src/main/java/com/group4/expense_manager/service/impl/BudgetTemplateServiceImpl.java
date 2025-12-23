package com.group4.expense_manager.service.impl;

import com.group4.expense_manager.annotation.LogActivity;
import com.group4.expense_manager.dto.request.BudgetTemplateRequest;
import com.group4.expense_manager.entity.BudgetTemplate;
import com.group4.expense_manager.entity.BudgetTemplateItem;
import com.group4.expense_manager.entity.Category;
import com.group4.expense_manager.entity.CategoryType;
import com.group4.expense_manager.repository.BudgetTemplateItemRepository;
import com.group4.expense_manager.repository.BudgetTemplateRepository;
import com.group4.expense_manager.repository.CategoryRepository;
import com.group4.expense_manager.service.BudgetTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class BudgetTemplateServiceImpl implements BudgetTemplateService {
    
    private final BudgetTemplateRepository templateRepository;
    private final BudgetTemplateItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    
    @Autowired
    public BudgetTemplateServiceImpl(BudgetTemplateRepository templateRepository,
                                      BudgetTemplateItemRepository itemRepository,
                                      CategoryRepository categoryRepository) {
        this.templateRepository = templateRepository;
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<BudgetTemplate> listTemplates(String keyword, Pageable pageable) {
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        Page<BudgetTemplate> page;
        if (hasKeyword) {
            page = templateRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else {
            page = templateRepository.findAll(pageable);
        }
        // Eager load items + category to avoid LazyInitializationException
        page.getContent().forEach(t -> {
            if (t.getItems() != null) {
                t.getItems().size(); // trigger fetch
                t.getItems().forEach(item -> {
                    if (item.getCategory() != null) {
                        item.getCategory().getName(); // trigger fetch
                    }
                });
            }
        });
        return page;
    }
    
    @Override
    public BudgetTemplate getTemplate(Integer id) {
        return templateRepository.findByIdWithItems(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mẫu ngân sách với ID: " + id));
    }
    
    @Override
    @Transactional
    @LogActivity(
        action = "CREATE",
        targetEntity = "BUDGET_TEMPLATE",
        description = "Created new budget template"
    )
    public BudgetTemplate createTemplate(BudgetTemplateRequest request) {
        BudgetTemplate template = new BudgetTemplate();
        template.setName(request.getName());
        template.setMonth(request.getMonth() != null ? request.getMonth() : 1);
        template.setDescription(request.getDescription());
        
        // Bảo đảm tập items luôn dùng cùng reference để tránh orphanRemoval lỗi
        Set<BudgetTemplateItem> items = template.getItems();
        if (items == null) {
            items = new HashSet<>();
            template.setItems(items);
        } else {
            items.clear();
        }

        // Tạo items nếu có
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (BudgetTemplateRequest.BudgetTemplateItemRequest itemRequest : request.getItems()) {
                Category category = validateCategory(itemRequest.getCategoryId());

                BudgetTemplateItem item = new BudgetTemplateItem();
                item.setTemplate(template);
                item.setCategory(category);
                item.setDefaultAmount(itemRequest.getDefaultAmount());
                items.add(item);
            }
        }

        // Cascade ALL sẽ lưu items theo template
        return templateRepository.save(template);
    }
    
    @Override
    @Transactional
    @LogActivity(
        action = "UPDATE",
        targetEntity = "BUDGET_TEMPLATE",
        description = "Updated budget template"
    )
    public BudgetTemplate updateTemplate(Integer id, BudgetTemplateRequest request) {
        BudgetTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mẫu ngân sách với ID: " + id));
        
        template.setName(request.getName());
        template.setMonth(request.getMonth() != null ? request.getMonth() : 1);
        template.setDescription(request.getDescription());
        
        // Dùng cùng reference collection để tránh lỗi orphanRemoval
        Set<BudgetTemplateItem> items = template.getItems();
        if (items == null) {
            items = new HashSet<>();
            template.setItems(items);
        } else {
            items.clear();
        }

        // Tạo items mới nếu có
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (BudgetTemplateRequest.BudgetTemplateItemRequest itemRequest : request.getItems()) {
                Category category = validateCategory(itemRequest.getCategoryId());

                BudgetTemplateItem item = new BudgetTemplateItem();
                item.setTemplate(template);
                item.setCategory(category);
                item.setDefaultAmount(itemRequest.getDefaultAmount());
                items.add(item);
            }
        }

        return templateRepository.save(template);
    }
    
    @Override
    @Transactional
    @LogActivity(
        action = "DELETE",
        targetEntity = "BUDGET_TEMPLATE",
        description = "Deleted budget template"
    )
    public void deleteTemplate(Integer id) {
        BudgetTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mẫu ngân sách với ID: " + id));
        templateRepository.delete(template);
    }
    
    private Category validateCategory(Integer categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + categoryId));
        
        if (category.getType() != CategoryType.expense) {
            throw new RuntimeException("Ngân sách chỉ có thể tạo cho danh mục chi tiêu");
        }
        
        return category;
    }
}
