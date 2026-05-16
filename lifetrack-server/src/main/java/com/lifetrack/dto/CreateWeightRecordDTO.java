package com.lifetrack.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateWeightRecordDTO {

    @NotNull(message = "体重不能为空")
    private BigDecimal weightKg;

    private BigDecimal bmi;

    private BigDecimal bodyFatRate;

    private BigDecimal muscleMass;

    @NotNull(message = "记录日期不能为空")
    private LocalDate recordDate;

    private String timeSlot;
}
