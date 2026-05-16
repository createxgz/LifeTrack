package com.lifetrack.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateBudgetDTO {
    @NotNull
    private Long categoryId;

    @NotNull
    private BigDecimal monthlyLimit;

    @NotNull
    private Integer year;

    @NotNull
    private Integer month;
}
