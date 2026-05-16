package com.lifetrack.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class LedgerStatsVO {
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;

    @Data
    public static class CategoryItem {
        private Long categoryId;
        private String categoryName;
        private String categoryIcon;
        private BigDecimal amount;
        private BigDecimal percentage;
    }

    private List<CategoryItem> incomeCategories;
    private List<CategoryItem> expenseCategories;
}
