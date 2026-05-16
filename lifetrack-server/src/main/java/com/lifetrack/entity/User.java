package com.lifetrack.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String email;
    private String passwordHash;
    private String nickname;
    private String avatar;
    private java.time.LocalDate birthday;
    private Integer gender;
    private java.math.BigDecimal height;
    private java.math.BigDecimal targetWeight;
    private String notifyEmail;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
