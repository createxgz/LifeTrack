package com.lifetrack.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateProfileDTO {
    private String nickname;
    private String avatar;
    private Integer gender;
    private BigDecimal height;
    private BigDecimal targetWeight;
    private String notifyEmail;
}
