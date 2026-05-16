package com.lifetrack.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class DashboardCalendarVO {

    private List<CalendarDay> days;

    @Data
    public static class CalendarDay {
        private LocalDate date;
        private int checkinCount;
        private boolean hasIncome;
        private boolean hasExpense;
    }
}
