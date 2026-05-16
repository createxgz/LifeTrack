package com.lifetrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateTaskDTO {

    @NotBlank(message = "任务名称不能为空")
    private String title;

    private String description;

    @NotNull(message = "重复类型不能为空")
    private Integer repeatType;

    private String cronExpr;

    private LocalTime remindTime;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    private LocalDate endDate;
}
