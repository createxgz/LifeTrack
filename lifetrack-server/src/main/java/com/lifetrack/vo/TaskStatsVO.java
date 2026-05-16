package com.lifetrack.vo;

import lombok.Data;

import java.util.Map;

@Data
public class TaskStatsVO {
    private Integer totalTasks;
    private Integer activeTasks;
    private Integer todayCheckins;
    private Integer todayTotal;
    private Double todayRate;
    private Double weekRate;
    private Double monthRate;
    private Map<String, Integer> weekCheckins;
}
