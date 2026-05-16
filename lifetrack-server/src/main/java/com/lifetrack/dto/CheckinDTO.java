package com.lifetrack.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CheckinDTO {
    private String note;
    private LocalDate date;
}
