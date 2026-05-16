package com.lifetrack.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("ledger_records")
public class LedgerRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Integer type;
    private BigDecimal amount;
    private Long categoryId;
    private String payment;
    private String note;
    private LocalDate recordDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    private Integer isDeleted;
}
