package com.lifetrack.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("weight_records")
public class WeightRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private java.math.BigDecimal weightKg;
    private java.math.BigDecimal bmi;
    private java.math.BigDecimal bodyFatRate;
    private java.math.BigDecimal muscleMass;
    private java.time.LocalDate recordDate;
    private String timeSlot;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
