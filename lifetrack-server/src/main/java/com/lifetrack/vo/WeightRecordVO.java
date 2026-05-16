package com.lifetrack.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class WeightRecordVO {
    private Long id;
    private BigDecimal weightKg;
    private BigDecimal bmi;
    private BigDecimal bodyFatRate;
    private BigDecimal muscleMass;
    private LocalDate recordDate;
    private String timeSlot;
    private LocalDateTime createdAt;
}
