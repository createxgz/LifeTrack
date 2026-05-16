package com.lifetrack.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("diet_records")
public class DietRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String mealType;
    private Long foodId;
    private BigDecimal quantity;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal carbs;
    private LocalDateTime recordTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
