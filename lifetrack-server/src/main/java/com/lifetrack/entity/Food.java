package com.lifetrack.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("foods")
public class Food {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String unit;
    private BigDecimal caloriesPerUnit;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal carbs;
    private String category;
    private Integer isDefault;
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    private Integer isDeleted;
}
