package com.lifetrack.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateBudgetDTO {
    private BigDecimal monthlyLimit;
}
