package com.lifetrack.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class UpdateTaskDTO {

    private String title;
    private String description;
    private Integer repeatType;
    private String cronExpr;
    private LocalTime remindTime;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;
    private Long parentTaskId;
}
