package com.lifetrack.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserProfileVO {
    private Long id;
    private String nickname;
    private String avatar;
    private Integer gender;
    private LocalDate birthday;
    private BigDecimal height;
    private BigDecimal targetWeight;
    private String email;
    private String notifyEmail;
    private LocalDateTime createdAt;
}
