package com.lifetrack.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifetrack.common.exception.BusinessException;
import com.lifetrack.dto.CreateLedgerRecordDTO;
import com.lifetrack.dto.UpdateLedgerRecordDTO;
import com.lifetrack.entity.LedgerCategory;
import com.lifetrack.entity.LedgerRecord;
import com.lifetrack.mapper.LedgerCategoryMapper;
import com.lifetrack.mapper.LedgerRecordMapper;
import com.lifetrack.vo.LedgerRecordVO;
import com.lifetrack.vo.LedgerStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerRecordMapper ledgerRecordMapper;
    private final LedgerCategoryMapper ledgerCategoryMapper;

    // ========== 收支记录 CRUD ==========

    public LedgerRecordVO addRecord(Long userId, CreateLedgerRecordDTO dto) {
        LedgerRecord record = new LedgerRecord();
        record.setUserId(userId);
        record.setType(dto.getType());
        record.setAmount(dto.getAmount());
        record.setCategoryId(dto.getCategoryId());
        record.setPayment(dto.getPayment());
        record.setNote(dto.getNote());
        record.setRecordDate(dto.getRecordDate() != null ? dto.getRecordDate() : LocalDate.now());
        ledgerRecordMapper.insert(record);
        return toVO(record);
    }

    public Map<String, Object> getRecords(Long userId, LocalDate date, Long categoryId, Integer type,
                                          Integer page, Integer size) {
        LambdaQueryWrapper<LedgerRecord> wrapper = new LambdaQueryWrapper<LedgerRecord>()
                .eq(LedgerRecord::getUserId, userId)
                .orderByDesc(LedgerRecord::getRecordDate)
                .orderByDesc(LedgerRecord::getCreatedAt);

        if (date != null) {
            wrapper.eq(LedgerRecord::getRecordDate, date);
        }
        if (categoryId != null) {
            wrapper.eq(LedgerRecord::getCategoryId, categoryId);
        }
        if (type != null) {
            wrapper.eq(LedgerRecord::getType, type);
        }

        Page<LedgerRecord> pageResult = ledgerRecordMapper.selectPage(
                new Page<>(page != null ? page : 1, size != null ? size : 10), wrapper);

        List<LedgerRecordVO> vos = pageResult.getRecords().stream()
                .map(this::toVO).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", vos);
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getCurrent());
        result.put("size", pageResult.getSize());
        return result;
    }

    public LedgerRecordVO updateRecord(Long userId, Long id, UpdateLedgerRecordDTO dto) {
        LedgerRecord record = ledgerRecordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException(404, "记录不存在");
        }
        if (dto.getType() != null) record.setType(dto.getType());
        if (dto.getAmount() != null) record.setAmount(dto.getAmount());
        if (dto.getCategoryId() != null) record.setCategoryId(dto.getCategoryId());
        if (dto.getPayment() != null) record.setPayment(dto.getPayment());
        if (dto.getNote() != null) record.setNote(dto.getNote());
        if (dto.getRecordDate() != null) record.setRecordDate(dto.getRecordDate());
        ledgerRecordMapper.updateById(record);
        return toVO(record);
    }

    public void deleteRecord(Long userId, Long id) {
        LedgerRecord record = ledgerRecordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException(404, "记录不存在");
        }
        ledgerRecordMapper.deleteById(id);
    }

    // ========== 分类 ==========

    public List<LedgerCategory> getCategories(Long userId) {
        return ledgerCategoryMapper.selectList(
                new LambdaQueryWrapper<LedgerCategory>()
                        .and(w -> w.isNull(LedgerCategory::getUserId).or().eq(LedgerCategory::getUserId, userId))
                        .orderByAsc(LedgerCategory::getType)
                        .orderByAsc(LedgerCategory::getId));
    }

    // ========== 统计 ==========

    public LedgerStatsVO getStats(Long userId, Integer year, Integer month) {
        int y = year != null ? year : LocalDate.now().getYear();
        int m = month != null ? month : LocalDate.now().getMonthValue();
        LocalDate start = LocalDate.of(y, m, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        List<LedgerRecord> records = ledgerRecordMapper.selectList(
                new LambdaQueryWrapper<LedgerRecord>()
                        .eq(LedgerRecord::getUserId, userId)
                        .ge(LedgerRecord::getRecordDate, start)
                        .le(LedgerRecord::getRecordDate, end));

        // Collect all category IDs
        Set<Long> catIds = records.stream().map(LedgerRecord::getCategoryId).collect(Collectors.toSet());
        Map<Long, LedgerCategory> catMap = new HashMap<>();
        if (!catIds.isEmpty()) {
            List<LedgerCategory> cats = ledgerCategoryMapper.selectBatchIds(catIds);
            for (LedgerCategory c : cats) catMap.put(c.getId(), c);
        }

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        Map<Long, BigDecimal> incomeByCat = new LinkedHashMap<>();
        Map<Long, BigDecimal> expenseByCat = new LinkedHashMap<>();

        for (LedgerRecord r : records) {
            if (r.getType() == 1) { // 收入
                totalIncome = totalIncome.add(r.getAmount());
                incomeByCat.merge(r.getCategoryId(), r.getAmount(), BigDecimal::add);
            } else { // 支出
                totalExpense = totalExpense.add(r.getAmount());
                expenseByCat.merge(r.getCategoryId(), r.getAmount(), BigDecimal::add);
            }
        }

        LedgerStatsVO stats = new LedgerStatsVO();
        stats.setTotalIncome(totalIncome);
        stats.setTotalExpense(totalExpense);
        stats.setBalance(totalIncome.subtract(totalExpense));

        stats.setIncomeCategories(buildCategoryItems(incomeByCat, catMap, totalIncome));
        stats.setExpenseCategories(buildCategoryItems(expenseByCat, catMap, totalExpense));

        return stats;
    }

    // ========== CSV 导出 ==========

    public String exportCSV(Long userId, Integer year, Integer month) {
        int y = year != null ? year : LocalDate.now().getYear();
        int m = month != null ? month : LocalDate.now().getMonthValue();
        LocalDate start = LocalDate.of(y, m, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        List<LedgerRecord> records = ledgerRecordMapper.selectList(
                new LambdaQueryWrapper<LedgerRecord>()
                        .eq(LedgerRecord::getUserId, userId)
                        .ge(LedgerRecord::getRecordDate, start)
                        .le(LedgerRecord::getRecordDate, end)
                        .orderByAsc(LedgerRecord::getRecordDate));

        Set<Long> catIds = records.stream().map(LedgerRecord::getCategoryId).collect(Collectors.toSet());
        Map<Long, String> catNameMap = new HashMap<>();
        if (!catIds.isEmpty()) {
            ledgerCategoryMapper.selectBatchIds(catIds)
                    .forEach(c -> catNameMap.put(c.getId(), c.getName()));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("日期,类型,分类,金额,支付方式,备注\n");
        for (LedgerRecord r : records) {
            sb.append(r.getRecordDate()).append(",");
            sb.append(r.getType() == 1 ? "收入" : "支出").append(",");
            sb.append(escapeCSV(catNameMap.getOrDefault(r.getCategoryId(), ""))).append(",");
            sb.append(r.getAmount()).append(",");
            sb.append(r.getPayment() != null ? r.getPayment() : "").append(",");
            sb.append(escapeCSV(r.getNote() != null ? r.getNote() : "")).append("\n");
        }
        return sb.toString();
    }

    // ========== 内部 ==========

    private LedgerRecordVO toVO(LedgerRecord r) {
        LedgerRecordVO vo = new LedgerRecordVO();
        vo.setId(r.getId());
        vo.setType(r.getType());
        vo.setAmount(r.getAmount());
        vo.setCategoryId(r.getCategoryId());
        vo.setPayment(r.getPayment());
        vo.setNote(r.getNote());
        vo.setRecordDate(r.getRecordDate());
        vo.setCreatedAt(r.getCreatedAt());

        // Join category
        if (r.getCategoryId() != null) {
            LedgerCategory cat = ledgerCategoryMapper.selectById(r.getCategoryId());
            if (cat != null) {
                vo.setCategoryName(cat.getName());
                vo.setCategoryIcon(cat.getIcon());
            }
        }
        return vo;
    }

    private List<LedgerStatsVO.CategoryItem> buildCategoryItems(Map<Long, BigDecimal> catAmounts,
                                                                  Map<Long, LedgerCategory> catMap,
                                                                  BigDecimal total) {
        return catAmounts.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(e -> {
                    LedgerStatsVO.CategoryItem item = new LedgerStatsVO.CategoryItem();
                    item.setCategoryId(e.getKey());
                    LedgerCategory cat = catMap.get(e.getKey());
                    item.setCategoryName(cat != null ? cat.getName() : "未知");
                    item.setCategoryIcon(cat != null ? cat.getIcon() : null);
                    item.setAmount(e.getValue());
                    if (total.compareTo(BigDecimal.ZERO) > 0) {
                        item.setPercentage(e.getValue().multiply(new BigDecimal("100"))
                                .divide(total, 1, RoundingMode.HALF_UP));
                    } else {
                        item.setPercentage(BigDecimal.ZERO);
                    }
                    return item;
                })
                .collect(Collectors.toList());
    }

    private String escapeCSV(String val) {
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}
