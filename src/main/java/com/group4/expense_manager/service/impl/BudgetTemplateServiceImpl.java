package com.group4.expense_manager.service.impl;

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
    public Page<BudgetTemplate> listTemplates(String keyword, Integer month, Pageable pageable) {
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasMonth = month != null;
        
        if (hasKeyword && hasMonth) {
            return templateRepository.findByNameContainingIgnoreCaseAndMonth(keyword, month, pageable);
        } else if (hasKeyword) {
            return templateRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else if (hasMonth) {
            return templateRepository.findByMonth(month, pageable);
        } else {
            return templateRepository.findAll(pageable);
        }
    }
    
    @Override
    public BudgetTemplate getTemplate(Integer id) {
        return templateRepository.findByIdWithItems(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mẫu ngân sách với ID: " + id));
    }
    
    @Override
    @Transactional
    public BudgetTemplate createTemplate(BudgetTemplateRequest request) {
        BudgetTemplate template = new BudgetTemplate();
        template.setName(request.getName());
        template.setMonth(request.getMonth());
        template.setDescription(request.getDescription());
        
        // Lưu template trước
        template = templateRepository.save(template);
        
        // Tạo items nếu có
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            Set<BudgetTemplateItem> items = new HashSet<>();
            for (BudgetTemplateRequest.BudgetTemplateItemRequest itemRequest : request.getItems()) {
                Category category = validateCategory(itemRequest.getCategoryId());
                
                BudgetTemplateItem item = new BudgetTemplateItem();
                item.setTemplate(template);
                item.setCategory(category);
                item.setDefaultAmount(itemRequest.getDefaultAmount());
                items.add(item);
            }
            template.setItems(items);
            itemRepository.saveAll(items);
        }
        
        return template;
    }
    
    @Override
    @Transactional
    public BudgetTemplate updateTemplate(Integer id, BudgetTemplateRequest request) {
        BudgetTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mẫu ngân sách với ID: " + id));
        
        template.setName(request.getName());
        template.setMonth(request.getMonth());
        template.setDescription(request.getDescription());
        
        // Xóa items cũ
        if (template.getItems() != null) {
            template.getItems().clear();
        }
        
        // Tạo items mới nếu có
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            Set<BudgetTemplateItem> items = new HashSet<>();
            for (BudgetTemplateRequest.BudgetTemplateItemRequest itemRequest : request.getItems()) {
                Category category = validateCategory(itemRequest.getCategoryId());
                
                BudgetTemplateItem item = new BudgetTemplateItem();
                item.setTemplate(template);
                item.setCategory(category);
                item.setDefaultAmount(itemRequest.getDefaultAmount());
                items.add(item);
            }
            template.setItems(items);
        }
        
        return templateRepository.save(template);
    }
    
    @Override
    @Transactional
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
