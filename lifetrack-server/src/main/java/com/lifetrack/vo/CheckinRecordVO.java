package com.lifetrack.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CheckinRecordVO {
    private Long id;
    private Long taskId;
    private LocalDate checkinDate;
    private String note;
    private LocalDateTime createdAt;
}
