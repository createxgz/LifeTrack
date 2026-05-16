package com.lifetrack.controller;

import com.lifetrack.common.Result;
import com.lifetrack.dto.*;
import com.lifetrack.entity.LedgerCategory;
import com.lifetrack.entity.WalletAccount;
import com.lifetrack.service.LedgerService;
import com.lifetrack.vo.BudgetVO;
import com.lifetrack.vo.LedgerRecordVO;
import com.lifetrack.vo.LedgerStatsVO;
import com.lifetrack.vo.WalletVO;
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

    // ========== 钱包账户 ==========

    @Operation(summary = "获取钱包账户列表和总额")
    @GetMapping("/wallets")
    public Result<WalletVO> getWallets(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(ledgerService.getWallets(userId));
    }

    @Operation(summary = "添加钱包账户")
    @PostMapping("/wallets")
    public Result<WalletAccount> addWallet(@RequestBody @Valid CreateWalletAccountDTO dto,
                                           Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(ledgerService.addWallet(userId, dto));
    }

    @Operation(summary = "更新钱包账户")
    @PutMapping("/wallets/{id}")
    public Result<WalletAccount> updateWallet(@PathVariable Long id,
                                              @RequestBody UpdateWalletAccountDTO dto,
                                              Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(ledgerService.updateWallet(userId, id, dto));
    }

    @Operation(summary = "删除钱包账户")
    @DeleteMapping("/wallets/{id}")
    public Result<Void> deleteWallet(@PathVariable Long id,
                                     Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        ledgerService.deleteWallet(userId, id);
        return Result.success();
    }

    // ========== 自定义分类 CRUD ==========

    @Operation(summary = "添加自定义分类")
    @PostMapping("/categories")
    public Result<LedgerCategory> addCategory(@RequestBody @Valid CreateCategoryDTO dto,
                                              Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(ledgerService.addCategory(userId, dto));
    }

    @Operation(summary = "更新分类")
    @PutMapping("/categories/{id}")
    public Result<LedgerCategory> updateCategory(@PathVariable Long id,
                                                 @RequestBody UpdateCategoryDTO dto,
                                                 Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(ledgerService.updateCategory(userId, id, dto));
    }

    @Operation(summary = "删除分类（可迁移关联记录到目标分类）")
    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id,
                                       @RequestParam(required = false) Long migrateTo,
                                       Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        ledgerService.deleteCategory(userId, id, migrateTo);
        return Result.success();
    }

    // ========== 预算管理 ==========

    @Operation(summary = "获取月度预算列表（含实际支出和进度）")
    @GetMapping("/budgets")
    public Result<List<BudgetVO>> getBudgets(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(ledgerService.getBudgets(userId, year, month));
    }

    @Operation(summary = "创建或更新预算（同一分类同月覆盖）")
    @PostMapping("/budgets")
    public Result<BudgetVO> saveBudget(@RequestBody @Valid CreateBudgetDTO dto,
                                        Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(ledgerService.saveBudget(userId, dto));
    }

    @Operation(summary = "更新预算")
    @PutMapping("/budgets/{id}")
    public Result<BudgetVO> updateBudget(@PathVariable Long id,
                                          @RequestBody UpdateBudgetDTO dto,
                                          Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(ledgerService.updateBudget(userId, id, dto));
    }

    @Operation(summary = "删除预算")
    @DeleteMapping("/budgets/{id}")
    public Result<Void> deleteBudget(@PathVariable Long id,
                                      Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        ledgerService.deleteBudget(userId, id);
        return Result.success();
    }
}
