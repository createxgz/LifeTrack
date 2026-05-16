package com.lifetrack.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateLedgerRecordDTO {

    private Integer type;

    private BigDecimal amount;

    private Long categoryId;

    private String payment;

    private String note;

    private LocalDate recordDate;
}
