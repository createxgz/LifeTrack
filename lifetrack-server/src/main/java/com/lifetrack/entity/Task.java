package com.lifetrack.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("tasks")
public class Task {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parentTaskId;
    private Long userId;
    private String title;
    private String description;
    private Integer repeatType;
    private String cronExpr;
    private LocalTime remindTime;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;
    private Integer streakDays;
    private Integer maxStreakDays;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
