package com.lifetrack.vo;

import com.lifetrack.entity.WalletAccount;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class WalletVO {
    private BigDecimal total;
    private List<WalletAccount> accounts;
}
