package com.lifetrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateWalletAccountDTO {

    @NotBlank(message = "账户名称不能为空")
    private String name;

    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    private String accountType;

    private Integer sortOrder;
}
