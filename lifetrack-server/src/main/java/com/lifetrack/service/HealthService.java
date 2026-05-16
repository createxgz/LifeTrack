package com.lifetrack.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifetrack.common.exception.BusinessException;
import com.lifetrack.dto.CreateDietRecordDTO;
import com.lifetrack.dto.CreateWeightRecordDTO;
import com.lifetrack.dto.UpdateWeightRecordDTO;
import com.lifetrack.entity.DietRecord;
import com.lifetrack.entity.Food;
import com.lifetrack.entity.WeightRecord;
import com.lifetrack.mapper.DietRecordMapper;
import com.lifetrack.mapper.FoodMapper;
import com.lifetrack.mapper.WeightRecordMapper;
import com.lifetrack.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HealthService {

    private final WeightRecordMapper weightRecordMapper;
    private final DietRecordMapper dietRecordMapper;
    private final FoodMapper foodMapper;

    // ========== 体重记录 ==========

    public WeightRecordVO addWeight(Long userId, CreateWeightRecordDTO dto) {
        WeightRecord record = new WeightRecord();
        record.setUserId(userId);
        record.setWeightKg(dto.getWeightKg());
        record.setBmi(dto.getBmi());
        record.setBodyFatRate(dto.getBodyFatRate());
        record.setMuscleMass(dto.getMuscleMass());
        record.setRecordDate(dto.getRecordDate() != null ? dto.getRecordDate() : LocalDate.now());
        record.setTimeSlot(dto.getTimeSlot());
        weightRecordMapper.insert(record);
        return toWeightVO(record);
    }

    public WeightRecordVO updateWeight(Long userId, Long id, UpdateWeightRecordDTO dto) {
        WeightRecord record = weightRecordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException(404, "记录不存在");
        }
        if (dto.getWeightKg() != null) record.setWeightKg(dto.getWeightKg());
        if (dto.getBmi() != null) record.setBmi(dto.getBmi());
        if (dto.getBodyFatRate() != null) record.setBodyFatRate(dto.getBodyFatRate());
        if (dto.getMuscleMass() != null) record.setMuscleMass(dto.getMuscleMass());
        if (dto.getRecordDate() != null) record.setRecordDate(dto.getRecordDate());
        if (dto.getTimeSlot() != null) record.setTimeSlot(dto.getTimeSlot());
        weightRecordMapper.updateById(record);
        return toWeightVO(record);
    }

    public void deleteWeight(Long userId, Long id) {
        WeightRecord record = weightRecordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException(404, "记录不存在");
        }
        weightRecordMapper.deleteById(id);
    }

    public Map<String, Object> getWeights(Long userId, Integer page, Integer size) {
        Page<WeightRecord> pageResult = weightRecordMapper.selectPage(
                new Page<>(page != null ? page : 1, size != null ? size : 10),
                new LambdaQueryWrapper<WeightRecord>()
                        .eq(WeightRecord::getUserId, userId)
                        .orderByDesc(WeightRecord::getRecordDate));

        List<WeightRecordVO> vos = pageResult.getRecords().stream()
                .map(this::toWeightVO).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", vos);
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getCurrent());
        result.put("size", pageResult.getSize());
        return result;
    }

    public List<WeightTrendVO> getWeightTrend(Long userId, Integer days) {
        int range = days != null ? days : 90;
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(range - 1);

        List<WeightRecord> records = weightRecordMapper.selectList(
                new LambdaQueryWrapper<WeightRecord>()
                        .eq(WeightRecord::getUserId, userId)
                        .ge(WeightRecord::getRecordDate, startDate)
                        .le(WeightRecord::getRecordDate, endDate)
                        .orderByAsc(WeightRecord::getRecordDate)
                        .orderByAsc(WeightRecord::getTimeSlot));

        // Build date->weight map (earliest record per day)
        Map<LocalDate, BigDecimal> dateMap = new LinkedHashMap<>();
        for (WeightRecord r : records) {
            dateMap.putIfAbsent(r.getRecordDate(), r.getWeightKg());
        }

        // Fill all days in range, carrying forward last known weight
        List<WeightTrendVO> trend = new ArrayList<>();
        BigDecimal lastWeight = null;
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            WeightTrendVO vo = new WeightTrendVO();
            vo.setDate(d);
            BigDecimal weight = dateMap.get(d);
            if (weight != null) {
                vo.setWeightKg(weight);
                if (lastWeight == null) {
                    vo.setDirection("stable");
                } else if (weight.compareTo(lastWeight) > 0) {
                    vo.setDirection("up");
                } else if (weight.compareTo(lastWeight) < 0) {
                    vo.setDirection("down");
                } else {
                    vo.setDirection("stable");
                }
                lastWeight = weight;
            } else {
                vo.setWeightKg(lastWeight);
                vo.setDirection(lastWeight != null ? "stable" : null);
            }
            trend.add(vo);
        }
        return trend;
    }

    // ========== 饮食记录 ==========

    public DietRecordVO addDiet(Long userId, CreateDietRecordDTO dto) {
        DietRecord record = new DietRecord();
        record.setUserId(userId);
        record.setMealType(dto.getMealType());
        record.setFoodId(dto.getFoodId());
        record.setQuantity(dto.getQuantity());
        record.setCalories(dto.getCalories());
        record.setProtein(dto.getProtein());
        record.setFat(dto.getFat());
        record.setCarbs(dto.getCarbs());
        record.setRecordTime(dto.getRecordTime() != null ? dto.getRecordTime() : LocalDateTime.now());
        dietRecordMapper.insert(record);

        return toDietVO(record);
    }

    public Map<String, Object> getDiets(Long userId, LocalDate date, Integer page, Integer size) {
        LambdaQueryWrapper<DietRecord> wrapper = new LambdaQueryWrapper<DietRecord>()
                .eq(DietRecord::getUserId, userId)
                .orderByDesc(DietRecord::getRecordTime);

        if (date != null) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            wrapper.ge(DietRecord::getRecordTime, start)
                   .le(DietRecord::getRecordTime, end);
        }

        Page<DietRecord> pageResult = dietRecordMapper.selectPage(
                new Page<>(page != null ? page : 1, size != null ? size : 10), wrapper);

        List<DietRecordVO> vos = pageResult.getRecords().stream()
                .map(this::toDietVO).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", vos);
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getCurrent());
        result.put("size", pageResult.getSize());
        return result;
    }

    public DietStatsVO getDietStats(Long userId, LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = targetDate.atTime(LocalTime.MAX);

        List<DietRecord> records = dietRecordMapper.selectList(
                new LambdaQueryWrapper<DietRecord>()
                        .eq(DietRecord::getUserId, userId)
                        .ge(DietRecord::getRecordTime, start)
                        .le(DietRecord::getRecordTime, end));

        DietStatsVO stats = new DietStatsVO();
        stats.setDate(targetDate);
        stats.setTotalCalories(sumField(records, DietRecord::getCalories));
        stats.setTotalProtein(sumField(records, DietRecord::getProtein));
        stats.setTotalFat(sumField(records, DietRecord::getFat));
        stats.setTotalCarbs(sumField(records, DietRecord::getCarbs));
        return stats;
    }

    // ========== 食物库 ==========

    public List<Food> searchFoods(String name, String category) {
        LambdaQueryWrapper<Food> wrapper = new LambdaQueryWrapper<Food>()
                .orderByAsc(Food::getName);
        if (name != null && !name.isBlank()) {
            wrapper.like(Food::getName, name);
        }
        if (category != null && !category.isBlank()) {
            wrapper.eq(Food::getCategory, category);
        }
        return foodMapper.selectList(wrapper);
    }

    // ========== 内部方法 ==========

    private WeightRecordVO toWeightVO(WeightRecord r) {
        WeightRecordVO vo = new WeightRecordVO();
        vo.setId(r.getId());
        vo.setWeightKg(r.getWeightKg());
        vo.setBmi(r.getBmi());
        vo.setBodyFatRate(r.getBodyFatRate());
        vo.setMuscleMass(r.getMuscleMass());
        vo.setRecordDate(r.getRecordDate());
        vo.setTimeSlot(r.getTimeSlot());
        vo.setCreatedAt(r.getCreatedAt());
        return vo;
    }

    private DietRecordVO toDietVO(DietRecord r) {
        DietRecordVO vo = new DietRecordVO();
        vo.setId(r.getId());
        vo.setMealType(r.getMealType());
        vo.setFoodId(r.getFoodId());
        vo.setQuantity(r.getQuantity());
        vo.setCalories(r.getCalories());
        vo.setProtein(r.getProtein());
        vo.setFat(r.getFat());
        vo.setCarbs(r.getCarbs());
        vo.setRecordTime(r.getRecordTime());
        vo.setCreatedAt(r.getCreatedAt());

        // Join food info if foodId is present
        if (r.getFoodId() != null) {
            Food food = foodMapper.selectById(r.getFoodId());
            if (food != null) {
                vo.setFoodName(food.getName());
                vo.setUnit(food.getUnit());
            }
        }
        return vo;
    }

    private BigDecimal sumField(List<DietRecord> records, java.util.function.Function<DietRecord, BigDecimal> getter) {
        return records.stream()
                .map(getter)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
