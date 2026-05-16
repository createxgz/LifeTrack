package com.lifetrack.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateWeightRecordDTO {

    private BigDecimal weightKg;

    private BigDecimal bmi;

    private BigDecimal bodyFatRate;

    private BigDecimal muscleMass;

    private LocalDate recordDate;

    private String timeSlot;
}
