package com.lifetrack.controller;

import com.lifetrack.common.Result;
import com.lifetrack.dto.CreateLedgerRecordDTO;
import com.lifetrack.dto.UpdateLedgerRecordDTO;
import com.lifetrack.entity.LedgerCategory;
import com.lifetrack.service.LedgerService;
import com.lifetrack.vo.LedgerRecordVO;
import com.lifetrack.vo.LedgerStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "账本管理")
@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    @Operation(summary = "添加收支记录")
    @PostMapping("/records")
    public Result<LedgerRecordVO> addRecord(@RequestBody @Valid CreateLedgerRecordDTO dto,
                                            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(ledgerService.addRecord(userId, dto));
    }

    @Operation(summary = "收支记录列表")
    @GetMapping("/records")
    public Result<Map<String, Object>> getRecords(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(ledgerService.getRecords(userId, date, categoryId, type, page, size));
    }

    @Operation(summary = "更新收支记录")
    @PutMapping("/records/{id}")
    public Result<LedgerRecordVO> updateRecord(@PathVariable Long id,
                                               @RequestBody UpdateLedgerRecordDTO dto,
                                               Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(ledgerService.updateRecord(userId, id, dto));
    }

    @Operation(summary = "删除收支记录")
    @DeleteMapping("/records/{id}")
    public Result<Void> deleteRecord(@PathVariable Long id,
                                     Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        ledgerService.deleteRecord(userId, id);
        return Result.success();
    }

    @Operation(summary = "分类列表")
    @GetMapping("/categories")
    public Result<List<LedgerCategory>> getCategories(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(ledgerService.getCategories(userId));
    }

    @Operation(summary = "月度统计")
    @GetMapping("/stats")
    public Result<LedgerStatsVO> getStats(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(ledgerService.getStats(userId, year, month));
    }

    @Operation(summary = "CSV 导出")
    @GetMapping("/stats/export")
    public Result<String> exportCSV(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(ledgerService.exportCSV(userId, year, month));
    }
}
