package com.lifetrack.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetVO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private BigDecimal monthlyLimit;
    private BigDecimal spent;
    private BigDecimal percentage;
    private Integer year;
    private Integer month;
}
