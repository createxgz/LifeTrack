package com.lifetrack.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wallet_accounts")
public class WalletAccount {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String name;
    private BigDecimal amount;
    private String accountType;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
