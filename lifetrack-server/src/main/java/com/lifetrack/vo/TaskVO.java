package com.lifetrack.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class TaskVO {
    private Long id;
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
    private Boolean checkedToday;
    private Double weekRate;
    private LocalDateTime createdAt;
}
