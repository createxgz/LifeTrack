package com.lifetrack.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DietRecordVO {
    private Long id;
    private String mealType;
    private Long foodId;
    private String foodName;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal carbs;
    private LocalDateTime recordTime;
    private LocalDateTime createdAt;
}
