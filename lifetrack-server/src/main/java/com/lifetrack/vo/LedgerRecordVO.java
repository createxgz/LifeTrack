package com.lifetrack.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LedgerRecordVO {
    private Long id;
    private Integer type;
    private BigDecimal amount;
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private String payment;
    private String note;
    private LocalDate recordDate;
    private LocalDateTime createdAt;
}
