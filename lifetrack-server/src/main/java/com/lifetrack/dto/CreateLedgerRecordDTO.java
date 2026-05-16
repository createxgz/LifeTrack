package com.lifetrack.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateLedgerRecordDTO {

    @NotNull(message = "类型不能为空")
    private Integer type;

    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    private String payment;

    private String note;

    @NotNull(message = "日期不能为空")
    private LocalDate recordDate;
}
