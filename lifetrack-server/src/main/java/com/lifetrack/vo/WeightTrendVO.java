package com.lifetrack.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class WeightTrendVO {
    private LocalDate date;
    private BigDecimal weightKg;
    private String direction;
}
