package com.lifetrack.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifetrack.common.exception.BusinessException;
import com.lifetrack.dto.CreateTaskDTO;
import com.lifetrack.dto.UpdateTaskDTO;
import com.lifetrack.entity.Task;
import com.lifetrack.entity.TaskCheckin;
import com.lifetrack.mapper.TaskCheckinMapper;
import com.lifetrack.mapper.TaskMapper;
import com.lifetrack.vo.CheckinRecordVO;
import com.lifetrack.vo.TaskStatsVO;
import com.lifetrack.vo.TaskVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskMapper taskMapper;
    private final TaskCheckinMapper checkinMapper;

    public TaskVO createTask(Long userId, CreateTaskDTO dto) {
        Task task = new Task();
        task.setUserId(userId);
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setRepeatType(dto.getRepeatType());
        task.setCronExpr(dto.getCronExpr());
        task.setRemindTime(dto.getRemindTime());
        task.setStartDate(dto.getStartDate());
        task.setEndDate(dto.getEndDate());
        task.setStatus(1);
        taskMapper.insert(task);
        return toVO(task, userId);
    }

    public TaskVO updateTask(Long userId, Long taskId, UpdateTaskDTO dto) {
        Task task = getOwnedTask(userId, taskId);
        if (dto.getTitle() != null) task.setTitle(dto.getTitle());
        if (dto.getDescription() != null) task.setDescription(dto.getDescription());
        if (dto.getRepeatType() != null) task.setRepeatType(dto.getRepeatType());
        if (dto.getCronExpr() != null) task.setCronExpr(dto.getCronExpr());
        if (dto.getRemindTime() != null) task.setRemindTime(dto.getRemindTime());
        if (dto.getStartDate() != null) task.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) task.setEndDate(dto.getEndDate());
        if (dto.getStatus() != null) task.setStatus(dto.getStatus());
        taskMapper.updateById(task);
        return toVO(task, userId);
    }

    public void deleteTask(Long userId, Long taskId) {
        getOwnedTask(userId, taskId);
        taskMapper.deleteById(taskId);
    }

    public Map<String, Object> getTasks(Long userId, Integer status, Integer repeatType, Integer page, Integer size) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, userId)
                .orderByDesc(Task::getCreatedAt);

        if (status != null) wrapper.eq(Task::getStatus, status);
        if (repeatType != null) wrapper.eq(Task::getRepeatType, repeatType);

        Page<Task> pageResult = taskMapper.selectPage(
                new Page<>(page != null ? page : 1, size != null ? size : 10), wrapper);

        LocalDate today = LocalDate.now();
        List<TaskVO> vos = pageResult.getRecords().stream()
                .map(t -> toVO(t, userId))
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", vos);
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getCurrent());
        result.put("size", pageResult.getSize());
        return result;
    }

    public TaskVO getTaskDetail(Long userId, Long taskId) {
        Task task = getOwnedTask(userId, taskId);
        return toVO(task, userId);
    }

    @Transactional
    public void checkin(Long userId, Long taskId, String note, LocalDate targetDate) {
        Task task = getOwnedTask(userId, taskId);

        LocalDate checkinDate = targetDate != null ? targetDate : LocalDate.now();

        // 不允许未来打卡
        if (checkinDate.isAfter(LocalDate.now())) {
            throw new BusinessException("不能提前打卡");
        }

        // 补签限制：3天内
        long daysDiff = ChronoUnit.DAYS.between(checkinDate, LocalDate.now());
        if (daysDiff > 3) {
            throw new BusinessException("仅支持3天内补签");
        }

        // 检查任务是否在有效期内
        if (checkinDate.isBefore(task.getStartDate())) {
            throw new BusinessException("该日期任务尚未开始");
        }
        if (task.getEndDate() != null && checkinDate.isAfter(task.getEndDate())) {
            throw new BusinessException("该日期任务已结束");
        }

        // 检查是否已打卡
        TaskCheckin existing = checkinMapper.selectOne(new LambdaQueryWrapper<TaskCheckin>()
                .eq(TaskCheckin::getTaskId, taskId)
                .eq(TaskCheckin::getCheckinDate, checkinDate));
        if (existing != null) {
            throw new BusinessException("该日期已打卡");
        }

        // 创建打卡记录
        TaskCheckin checkin = new TaskCheckin();
        checkin.setTaskId(taskId);
        checkin.setUserId(userId);
        checkin.setCheckinDate(checkinDate);
        checkin.setNote(note);
        checkinMapper.insert(checkin);

        // 重新计算连续天数
        updateStreak(task);
    }

    public List<CheckinRecordVO> getCheckinRecords(Long userId, Long taskId) {
        getOwnedTask(userId, taskId);
        List<TaskCheckin> records = checkinMapper.selectList(
                new LambdaQueryWrapper<TaskCheckin>()
                        .eq(TaskCheckin::getTaskId, taskId)
                        .orderByDesc(TaskCheckin::getCheckinDate));

        return records.stream().map(r -> {
            CheckinRecordVO vo = new CheckinRecordVO();
            vo.setId(r.getId());
            vo.setTaskId(r.getTaskId());
            vo.setCheckinDate(r.getCheckinDate());
            vo.setNote(r.getNote());
            vo.setCreatedAt(r.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());
    }

    public TaskStatsVO getStats(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate monthStart = today.withDayOfMonth(1);

        // All active tasks
        List<Task> allTasks = taskMapper.selectList(
                new LambdaQueryWrapper<Task>()
                        .eq(Task::getUserId, userId)
                        .eq(Task::getStatus, 1));
        int totalTasks = allTasks.size();

        // Today's checkins
        List<TaskCheckin> todayCheckins = checkinMapper.selectList(
                new LambdaQueryWrapper<TaskCheckin>()
                        .eq(TaskCheckin::getUserId, userId)
                        .eq(TaskCheckin::getCheckinDate, today));
        int todayCount = todayCheckins.size();

        // Week checkins
        List<TaskCheckin> weekCheckins = checkinMapper.selectList(
                new LambdaQueryWrapper<TaskCheckin>()
                        .eq(TaskCheckin::getUserId, userId)
                        .ge(TaskCheckin::getCheckinDate, weekStart)
                        .le(TaskCheckin::getCheckinDate, today));

        // Month checkins
        List<TaskCheckin> monthCheckins = checkinMapper.selectList(
                new LambdaQueryWrapper<TaskCheckin>()
                        .eq(TaskCheckin::getUserId, userId)
                        .ge(TaskCheckin::getCheckinDate, monthStart)
                        .le(TaskCheckin::getCheckinDate, today));

        int daysThisWeek = (int) ChronoUnit.DAYS.between(weekStart, today) + 1;
        int daysThisMonth = today.getDayOfMonth();

        int weekTotalSlots = totalTasks * daysThisWeek;
        int monthTotalSlots = totalTasks * daysThisMonth;

        TaskStatsVO vo = new TaskStatsVO();
        vo.setTotalTasks(totalTasks);
        vo.setActiveTasks(totalTasks);
        vo.setTodayCheckins(todayCount);
        vo.setTodayTotal(totalTasks);
        vo.setTodayRate(totalTasks > 0 ? (double) todayCount / totalTasks : 0);
        vo.setWeekRate(weekTotalSlots > 0 ? (double) weekCheckins.size() / weekTotalSlots : 0);
        vo.setMonthRate(monthTotalSlots > 0 ? (double) monthCheckins.size() / monthTotalSlots : 0);

        Map<String, Integer> weekMap = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = weekStart.plusDays(i);
            String key = d.toString();
            int count = (int) weekCheckins.stream().filter(c -> c.getCheckinDate().equals(d)).count();
            weekMap.put(key, count);
        }
        vo.setWeekCheckins(weekMap);

        return vo;
    }

    private Task getOwnedTask(Long userId, Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(404, "任务不存在");
        }
        if (!task.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作该任务");
        }
        return task;
    }

    private void updateStreak(Task task) {
        LocalDate today = LocalDate.now();
        List<TaskCheckin> records = checkinMapper.selectList(
                new LambdaQueryWrapper<TaskCheckin>()
                        .eq(TaskCheckin::getTaskId, task.getId())
                        .orderByDesc(TaskCheckin::getCheckinDate));

        int streak = 0;
        LocalDate expected = today;
        for (TaskCheckin r : records) {
            if (r.getCheckinDate().equals(expected)) {
                streak++;
                expected = expected.minusDays(1);
            } else if (r.getCheckinDate().isBefore(expected)) {
                break;
            }
        }

        task.setStreakDays(streak);
        if (streak > task.getMaxStreakDays()) {
            task.setMaxStreakDays(streak);
        }
        taskMapper.updateById(task);
    }

    private TaskVO toVO(Task task, Long userId) {
        TaskVO vo = new TaskVO();
        vo.setId(task.getId());
        vo.setUserId(task.getUserId());
        vo.setTitle(task.getTitle());
        vo.setDescription(task.getDescription());
        vo.setRepeatType(task.getRepeatType());
        vo.setCronExpr(task.getCronExpr());
        vo.setRemindTime(task.getRemindTime());
        vo.setStartDate(task.getStartDate());
        vo.setEndDate(task.getEndDate());
        vo.setStatus(task.getStatus());
        vo.setStreakDays(task.getStreakDays());
        vo.setMaxStreakDays(task.getMaxStreakDays());
        vo.setCreatedAt(task.getCreatedAt());

        // Check if checked in today
        TaskCheckin todayCheckin = checkinMapper.selectOne(
                new LambdaQueryWrapper<TaskCheckin>()
                        .eq(TaskCheckin::getTaskId, task.getId())
                        .eq(TaskCheckin::getCheckinDate, LocalDate.now()));
        vo.setCheckedToday(todayCheckin != null);

        // Calculate week rate
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Long weekCount = checkinMapper.selectCount(
                new LambdaQueryWrapper<TaskCheckin>()
                        .eq(TaskCheckin::getTaskId, task.getId())
                        .ge(TaskCheckin::getCheckinDate, weekStart)
                        .le(TaskCheckin::getCheckinDate, today));
        int daysThisWeek = (int) ChronoUnit.DAYS.between(weekStart, today) + 1;
        vo.setWeekRate(daysThisWeek > 0 ? (double) weekCount / daysThisWeek : 0);

        return vo;
    }
}
