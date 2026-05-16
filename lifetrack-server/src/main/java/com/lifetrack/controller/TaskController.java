package com.lifetrack.controller;

import com.lifetrack.common.Result;
import com.lifetrack.dto.CheckinDTO;
import com.lifetrack.dto.CreateTaskDTO;
import com.lifetrack.dto.UpdateTaskDTO;
import com.lifetrack.service.TaskService;
import com.lifetrack.vo.CheckinRecordVO;
import com.lifetrack.vo.TaskStatsVO;
import com.lifetrack.vo.TaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "任务打卡")
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "创建任务")
    @PostMapping
    public Result<TaskVO> create(@RequestBody @Valid CreateTaskDTO dto,
                                 Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(taskService.createTask(userId, dto));
    }

    @Operation(summary = "更新任务")
    @PutMapping("/{id}")
    public Result<TaskVO> update(@PathVariable Long id,
                                 @RequestBody UpdateTaskDTO dto,
                                 Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(taskService.updateTask(userId, id, dto));
    }

    @Operation(summary = "删除任务")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        taskService.deleteTask(userId, id);
        return Result.success();
    }

    @Operation(summary = "任务列表")
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer repeatType,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(taskService.getTasks(userId, status, repeatType, page, size));
    }

    @Operation(summary = "任务详情")
    @GetMapping("/{id}")
    public Result<TaskVO> detail(@PathVariable Long id,
                                 Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(taskService.getTaskDetail(userId, id));
    }

    @Operation(summary = "打卡")
    @PostMapping("/{id}/checkin")
    public Result<Void> checkin(@PathVariable Long id,
                                @RequestBody CheckinDTO dto,
                                Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        taskService.checkin(userId, id, dto.getNote(), dto.getDate());
        return Result.success();
    }

    @Operation(summary = "打卡记录")
    @GetMapping("/{id}/records")
    public Result<List<CheckinRecordVO>> records(@PathVariable Long id,
                                                  Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(taskService.getCheckinRecords(userId, id));
    }

    @Operation(summary = "打卡统计")
    @GetMapping("/stats")
    public Result<TaskStatsVO> stats(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(taskService.getStats(userId));
    }
}
