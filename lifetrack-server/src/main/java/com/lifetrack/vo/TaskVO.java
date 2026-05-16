package com.lifetrack.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class TaskVO {
    private Long id;
    private Long parentTaskId;
    private String parentTaskTitle;
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
    private Integer subtaskCount;
    private List<TaskVO> subtasks;
    private Boolean checkedToday;
    private Double weekRate;
    private LocalDateTime createdAt;
}
