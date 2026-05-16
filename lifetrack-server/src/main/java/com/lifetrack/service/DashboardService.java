package com.lifetrack.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifetrack.entity.*;
import com.lifetrack.mapper.*;
import com.lifetrack.vo.DashboardCalendarVO;
import com.lifetrack.vo.DashboardOverviewVO;
import com.lifetrack.vo.WeightTrendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TaskMapper taskMapper;
    private final TaskCheckinMapper checkinMapper;
    private final WeightRecordMapper weightRecordMapper;
    private final DietRecordMapper dietRecordMapper;
    private final LedgerRecordMapper ledgerRecordMapper;
    private final LedgerBudgetMapper ledgerBudgetMapper;

    public DashboardOverviewVO getOverview(Long userId) {
        LocalDate today = LocalDate.now();

        DashboardOverviewVO vo = new DashboardOverviewVO();

        // Task stats
        List<Task> activeTasks = taskMapper.selectList(
                new LambdaQueryWrapper<Task>()
                        .eq(Task::getUserId, userId)
                        .eq(Task::getStatus, 1));
        int totalTasks = activeTasks.size();
        vo.setTotalTasks(totalTasks);

        List<TaskCheckin> todayCheckins = checkinMapper.selectList(
                new LambdaQueryWrapper<TaskCheckin>()
                        .eq(TaskCheckin::getUserId, userId)
                        .eq(TaskCheckin::getCheckinDate, today));
        int todayCount = todayCheckins.size();
        vo.setTodayCheckins(todayCount);
        vo.setTodayRate(totalTasks > 0 ? (double) todayCount / totalTasks : 0);

        // Week checkins map
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<TaskCheckin> weekCheckins = checkinMapper.selectList(
                new LambdaQueryWrapper<TaskCheckin>()
                        .eq(TaskCheckin::getUserId, userId)
                        .ge(TaskCheckin::getCheckinDate, weekStart)
                        .le(TaskCheckin::getCheckinDate, today));
        Map<String, Integer> weekMap = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = weekStart.plusDays(i);
            String key = d.toString();
            int count = (int) weekCheckins.stream().filter(c -> c.getCheckinDate().equals(d)).count();
            weekMap.put(key, count);
        }
        vo.setWeekCheckins(weekMap);

        // Weight — latest record
        WeightRecord latestWeight = weightRecordMapper.selectOne(
                new LambdaQueryWrapper<WeightRecord>()
                        .eq(WeightRecord::getUserId, userId)
                        .orderByDesc(WeightRecord::getRecordDate)
                        .orderByDesc(WeightRecord::getTimeSlot)
                        .last("LIMIT 1"));
        if (latestWeight != null) {
            vo.setLatestWeight(latestWeight.getWeightKg());
        }

        // Weight mini trend (last 7 days)
        LocalDate trendStart = today.minusDays(6);
        List<WeightRecord> weekWeights = weightRecordMapper.selectList(
                new LambdaQueryWrapper<WeightRecord>()
                        .eq(WeightRecord::getUserId, userId)
                        .ge(WeightRecord::getRecordDate, trendStart)
                        .le(WeightRecord::getRecordDate, today)
                        .orderByAsc(WeightRecord::getRecordDate)
                        .orderByAsc(WeightRecord::getTimeSlot));

        Map<LocalDate, BigDecimal> dateWeightMap = new LinkedHashMap<>();
        for (WeightRecord r : weekWeights) {
            dateWeightMap.putIfAbsent(r.getRecordDate(), r.getWeightKg());
        }

        List<WeightTrendVO> miniTrend = new ArrayList<>();
        BigDecimal lastW = null;
        for (LocalDate d = trendStart; !d.isAfter(today); d = d.plusDays(1)) {
            WeightTrendVO tv = new WeightTrendVO();
            tv.setDate(d);
            BigDecimal w = dateWeightMap.get(d);
            if (w != null) {
                tv.setWeightKg(w);
                if (lastW == null) tv.setDirection("stable");
                else if (w.compareTo(lastW) > 0) tv.setDirection("up");
                else if (w.compareTo(lastW) < 0) tv.setDirection("down");
                else tv.setDirection("stable");
                lastW = w;
            } else {
                tv.setWeightKg(lastW);
                tv.setDirection(lastW != null ? "stable" : null);
            }
            miniTrend.add(tv);
        }
        vo.setWeightMiniTrend(miniTrend);
        if (lastW != null && latestWeight != null) {
            if (latestWeight.getWeightKg().compareTo(lastW) > 0) vo.setWeightDirection("up");
            else if (latestWeight.getWeightKg().compareTo(lastW) < 0) vo.setWeightDirection("down");
            else vo.setWeightDirection("stable");
        }

        // Diet — today
        List<DietRecord> todayDiets = dietRecordMapper.selectList(
                new LambdaQueryWrapper<DietRecord>()
                        .eq(DietRecord::getUserId, userId)
                        .ge(DietRecord::getRecordTime, today.atStartOfDay())
                        .le(DietRecord::getRecordTime, today.plusDays(1).atStartOfDay()));
        BigDecimal todayCal = BigDecimal.ZERO;
        BigDecimal todayProt = BigDecimal.ZERO;
        for (DietRecord dr : todayDiets) {
            if (dr.getCalories() != null) todayCal = todayCal.add(dr.getCalories());
            if (dr.getProtein() != null) todayProt = todayProt.add(dr.getProtein());
        }
        vo.setTodayCalories(todayCal);
        vo.setTodayProtein(todayProt);

        // Ledger — this month
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());
        List<LedgerRecord> monthRecords = ledgerRecordMapper.selectList(
                new LambdaQueryWrapper<LedgerRecord>()
                        .eq(LedgerRecord::getUserId, userId)
                        .ge(LedgerRecord::getRecordDate, monthStart)
                        .le(LedgerRecord::getRecordDate, monthEnd));
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        for (LedgerRecord lr : monthRecords) {
            if (lr.getType() == 1) income = income.add(lr.getAmount());
            else expense = expense.add(lr.getAmount());
        }
        vo.setMonthlyIncome(income);
        vo.setMonthlyExpense(expense);
        vo.setBalance(income.subtract(expense));

        // Budget — total for this month
        List<LedgerBudget> budgets = ledgerBudgetMapper.selectList(
                new LambdaQueryWrapper<LedgerBudget>()
                        .eq(LedgerBudget::getUserId, userId)
                        .eq(LedgerBudget::getYear, today.getYear())
                        .eq(LedgerBudget::getMonth, today.getMonthValue()));
        BigDecimal totalBudget = budgets.stream()
                .map(LedgerBudget::getMonthlyLimit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setMonthlyBudget(totalBudget);
        if (totalBudget.compareTo(BigDecimal.ZERO) > 0) {
            vo.setBudgetRemaining(totalBudget.subtract(expense));
            vo.setBudgetUsagePercent(expense.multiply(new BigDecimal("100"))
                    .divide(totalBudget, 1, java.math.RoundingMode.HALF_UP));
        } else {
            vo.setBudgetRemaining(null);
            vo.setBudgetUsagePercent(null);
        }

        return vo;
    }

    public DashboardCalendarVO getCalendar(Long userId, Integer year, Integer month) {
        int y = year != null ? year : LocalDate.now().getYear();
        int m = month != null ? month : LocalDate.now().getMonthValue();
        LocalDate start = LocalDate.of(y, m, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        // Checkins for the month
        List<TaskCheckin> checkins = checkinMapper.selectList(
                new LambdaQueryWrapper<TaskCheckin>()
                        .eq(TaskCheckin::getUserId, userId)
                        .ge(TaskCheckin::getCheckinDate, start)
                        .le(TaskCheckin::getCheckinDate, end));

        Map<LocalDate, Integer> checkinCount = new LinkedHashMap<>();
        for (TaskCheckin c : checkins) {
            checkinCount.merge(c.getCheckinDate(), 1, Integer::sum);
        }

        // Ledger records for the month
        List<LedgerRecord> ledgerRecords = ledgerRecordMapper.selectList(
                new LambdaQueryWrapper<LedgerRecord>()
                        .eq(LedgerRecord::getUserId, userId)
                        .ge(LedgerRecord::getRecordDate, start)
                        .le(LedgerRecord::getRecordDate, end));

        Set<LocalDate> incomeDays = new HashSet<>();
        Set<LocalDate> expenseDays = new HashSet<>();
        for (LedgerRecord lr : ledgerRecords) {
            if (lr.getType() == 1) incomeDays.add(lr.getRecordDate());
            else expenseDays.add(lr.getRecordDate());
        }

        List<DashboardCalendarVO.CalendarDay> days = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            DashboardCalendarVO.CalendarDay cd = new DashboardCalendarVO.CalendarDay();
            cd.setDate(d);
            cd.setCheckinCount(checkinCount.getOrDefault(d, 0));
            cd.setHasIncome(incomeDays.contains(d));
            cd.setHasExpense(expenseDays.contains(d));
            days.add(cd);
        }

        DashboardCalendarVO vo = new DashboardCalendarVO();
        vo.setDays(days);
        return vo;
    }
}
