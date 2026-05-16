package com.lifetrack.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class DashboardOverviewVO {

    // Task stats
    private int totalTasks;
    private int todayCheckins;
    private double todayRate;
    private Map<String, Integer> weekCheckins;

    // Weight
    private BigDecimal latestWeight;
    private String weightDirection;
    private List<WeightTrendVO> weightMiniTrend;

    // Diet
    private BigDecimal todayCalories;
    private BigDecimal todayProtein;

    // Ledger
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpense;
    private BigDecimal balance;
    private BigDecimal monthlyBudget;
    private BigDecimal budgetRemaining;
    private BigDecimal budgetUsagePercent;
}
