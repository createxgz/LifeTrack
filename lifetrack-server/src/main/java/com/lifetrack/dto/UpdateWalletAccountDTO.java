package com.lifetrack.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateWalletAccountDTO {

    private String name;
    private BigDecimal amount;
    private String accountType;
    private Integer sortOrder;
}
