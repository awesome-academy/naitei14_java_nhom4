package com.group4.expense_manager.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseRequest {

    private String name;
    private BigDecimal amount;
    private String currency;
    private LocalDate expenseDate;
    private Integer categoryId;
    private Boolean isRecurring;
    private String note;
    private LocalDate recurringEndDate;
    private String recurringInterval;
    private String description;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Boolean getIsRecurring() {
        return isRecurring;
    }

    public void setIsRecurring(Boolean isRecurring) {
        this.isRecurring = isRecurring;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDate getRecurringEndDate() {
        return recurringEndDate;
    }

    public void setRecurringEndDate(LocalDate recurringEndDate) {
        this.recurringEndDate = recurringEndDate;
    }

    public String getRecurringInterval() {
        return recurringInterval;
    }

    public void setRecurringInterval(String recurringInterval) {
        this.recurringInterval = recurringInterval;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}