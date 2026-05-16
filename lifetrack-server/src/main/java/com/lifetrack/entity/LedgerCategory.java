package com.lifetrack.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ledger_categories")
public class LedgerCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String name;
    private Integer type;
    private String icon;
    private String color;
    private Integer isDefault;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    private Integer isDeleted;
}
