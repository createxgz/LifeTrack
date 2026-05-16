package com.lifetrack.controller;

import com.lifetrack.common.Result;
import com.lifetrack.dto.CreateDietRecordDTO;
import com.lifetrack.dto.CreateWeightRecordDTO;
import com.lifetrack.dto.UpdateWeightRecordDTO;
import com.lifetrack.entity.Food;
import com.lifetrack.service.HealthService;
import com.lifetrack.vo.DietRecordVO;
import com.lifetrack.vo.DietStatsVO;
import com.lifetrack.vo.WeightRecordVO;
import com.lifetrack.vo.WeightTrendVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "健康记录")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HealthController {

    private final HealthService healthService;

    // ========== 体重记录 ==========

    @Operation(summary = "添加体重记录")
    @PostMapping("/health/weights")
    public Result<WeightRecordVO> addWeight(@RequestBody @Valid CreateWeightRecordDTO dto,
                                            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(healthService.addWeight(userId, dto));
    }

    @Operation(summary = "体重记录列表")
    @GetMapping("/health/weights")
    public Result<Map<String, Object>> getWeights(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(healthService.getWeights(userId, page, size));
    }

    @Operation(summary = "更新体重记录")
    @PutMapping("/health/weights/{id}")
    public Result<WeightRecordVO> updateWeight(@PathVariable Long id,
                                               @RequestBody UpdateWeightRecordDTO dto,
                                               Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(healthService.updateWeight(userId, id, dto));
    }

    @Operation(summary = "删除体重记录")
    @DeleteMapping("/health/weights/{id}")
    public Result<Void> deleteWeight(@PathVariable Long id,
                                     Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        healthService.deleteWeight(userId, id);
        return Result.success();
    }

    @Operation(summary = "体重趋势")
    @GetMapping("/health/weights/trend")
    public Result<List<WeightTrendVO>> getWeightTrend(
            @RequestParam(required = false, defaultValue = "90") Integer days,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(healthService.getWeightTrend(userId, days));
    }

    // ========== 饮食记录 ==========

    @Operation(summary = "添加饮食记录")
    @PostMapping("/health/diets")
    public Result<DietRecordVO> addDiet(@RequestBody CreateDietRecordDTO dto,
                                        Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(healthService.addDiet(userId, dto));
    }

    @Operation(summary = "饮食记录列表")
    @GetMapping("/health/diets")
    public Result<Map<String, Object>> getDiets(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(healthService.getDiets(userId, date, page, size));
    }

    @Operation(summary = "饮食统计")
    @GetMapping("/health/diets/stats")
    public Result<DietStatsVO> getDietStats(
            @RequestParam(required = false) LocalDate date,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(healthService.getDietStats(userId, date));
    }

    // ========== 食物库 ==========

    @Operation(summary = "食物库查询")
    @GetMapping("/foods")
    public Result<List<Food>> searchFoods(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category) {
        return Result.success(healthService.searchFoods(name, category));
    }
}
