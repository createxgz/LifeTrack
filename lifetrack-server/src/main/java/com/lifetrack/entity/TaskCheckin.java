package com.lifetrack.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("task_checkins")
public class TaskCheckin {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;
    private Long userId;
    private LocalDate checkinDate;
    private String note;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
