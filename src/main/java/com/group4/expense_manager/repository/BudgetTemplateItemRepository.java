package com.group4.expense_manager.repository;

import com.group4.expense_manager.entity.BudgetTemplateItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetTemplateItemRepository extends JpaRepository<BudgetTemplateItem, Integer> {
    
    List<BudgetTemplateItem> findByTemplateId(Integer templateId);
    
    @Modifying
    void deleteByTemplateId(Integer templateId);
}
