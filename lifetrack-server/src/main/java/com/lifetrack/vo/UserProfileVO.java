package com.lifetrack.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserProfileVO {
    private Long id;
    private String nickname;
    private String avatar;
    private BigDecimal height;
    private BigDecimal targetWeight;
    private String email;
    private String notifyEmail;
}
