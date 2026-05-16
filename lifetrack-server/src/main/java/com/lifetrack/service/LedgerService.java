package com.lifetrack.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifetrack.common.exception.BusinessException;
import com.lifetrack.dto.CreateBudgetDTO;
import com.lifetrack.dto.CreateLedgerRecordDTO;
import com.lifetrack.dto.UpdateBudgetDTO;
import com.lifetrack.dto.UpdateLedgerRecordDTO;
import com.lifetrack.dto.CreateCategoryDTO;
import com.lifetrack.dto.CreateWalletAccountDTO;
import com.lifetrack.dto.UpdateCategoryDTO;
import com.lifetrack.dto.UpdateWalletAccountDTO;
import com.lifetrack.entity.LedgerBudget;
import com.lifetrack.entity.LedgerCategory;
import com.lifetrack.entity.LedgerRecord;
import com.lifetrack.entity.WalletAccount;
import com.lifetrack.mapper.LedgerBudgetMapper;
import com.lifetrack.mapper.LedgerCategoryMapper;
import com.lifetrack.mapper.LedgerRecordMapper;
import com.lifetrack.mapper.WalletAccountMapper;
import com.lifetrack.vo.BudgetVO;
import com.lifetrack.vo.LedgerRecordVO;
import com.lifetrack.vo.LedgerStatsVO;
import com.lifetrack.vo.WalletVO;
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
    private final WalletAccountMapper walletAccountMapper;
    private final LedgerBudgetMapper ledgerBudgetMapper;

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

    // ========== 钱包账户 CRUD ==========

    public WalletVO getWallets(Long userId) {
        List<WalletAccount> accounts = walletAccountMapper.selectList(
                new LambdaQueryWrapper<WalletAccount>()
                        .eq(WalletAccount::getUserId, userId)
                        .orderByAsc(WalletAccount::getSortOrder)
                        .orderByAsc(WalletAccount::getId));

        BigDecimal total = accounts.stream()
                .map(WalletAccount::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        WalletVO vo = new WalletVO();
        vo.setTotal(total);
        vo.setAccounts(accounts);
        return vo;
    }

    public WalletAccount addWallet(Long userId, CreateWalletAccountDTO dto) {
        WalletAccount account = new WalletAccount();
        account.setUserId(userId);
        account.setName(dto.getName());
        account.setAmount(dto.getAmount());
        account.setAccountType(dto.getAccountType());
        account.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        walletAccountMapper.insert(account);
        return account;
    }

    public WalletAccount updateWallet(Long userId, Long id, UpdateWalletAccountDTO dto) {
        WalletAccount account = walletAccountMapper.selectById(id);
        if (account == null || !account.getUserId().equals(userId)) {
            throw new BusinessException(404, "钱包账户不存在");
        }
        if (dto.getName() != null) account.setName(dto.getName());
        if (dto.getAmount() != null) account.setAmount(dto.getAmount());
        if (dto.getAccountType() != null) account.setAccountType(dto.getAccountType());
        if (dto.getSortOrder() != null) account.setSortOrder(dto.getSortOrder());
        walletAccountMapper.updateById(account);
        return account;
    }

    public void deleteWallet(Long userId, Long id) {
        WalletAccount account = walletAccountMapper.selectById(id);
        if (account == null || !account.getUserId().equals(userId)) {
            throw new BusinessException(404, "钱包账户不存在");
        }
        walletAccountMapper.deleteById(id);
    }

    // ========== 自定义分类 CRUD ==========

    public LedgerCategory addCategory(Long userId, CreateCategoryDTO dto) {
        LedgerCategory cat = new LedgerCategory();
        cat.setUserId(userId);
        cat.setName(dto.getName());
        cat.setType(dto.getType());
        cat.setIcon(dto.getIcon());
        cat.setColor(dto.getColor());
        cat.setIsDefault(0);
        ledgerCategoryMapper.insert(cat);
        return cat;
    }

    public LedgerCategory updateCategory(Long userId, Long id, UpdateCategoryDTO dto) {
        LedgerCategory cat = ledgerCategoryMapper.selectById(id);
        if (cat == null) {
            throw new BusinessException(404, "分类不存在");
        }
        if (cat.getIsDefault() != null && cat.getIsDefault() == 1) {
            throw new BusinessException(403, "系统默认分类不可修改");
        }
        if (!userId.equals(cat.getUserId())) {
            throw new BusinessException(403, "只能修改自己的分类");
        }
        if (dto.getName() != null) cat.setName(dto.getName());
        if (dto.getIcon() != null) cat.setIcon(dto.getIcon());
        if (dto.getColor() != null) cat.setColor(dto.getColor());
        ledgerCategoryMapper.updateById(cat);
        return cat;
    }

    public void deleteCategory(Long userId, Long id, Long migrateToId) {
        LedgerCategory cat = ledgerCategoryMapper.selectById(id);
        if (cat == null) {
            throw new BusinessException(404, "分类不存在");
        }
        if (cat.getIsDefault() != null && cat.getIsDefault() == 1) {
            throw new BusinessException(403, "系统默认分类不可删除");
        }
        if (!userId.equals(cat.getUserId())) {
            throw new BusinessException(403, "只能删除自己的分类");
        }

        // Count records under this category
        Long count = ledgerRecordMapper.selectCount(
                new LambdaQueryWrapper<LedgerRecord>()
                        .eq(LedgerRecord::getCategoryId, id));
        if (count > 0) {
            if (migrateToId == null) {
                throw new BusinessException(400, "该分类下有 " + count + " 条收支记录，请先选择迁移目标分类（migrateTo）");
            }
            // Verify target category exists
            LedgerCategory targetCat = ledgerCategoryMapper.selectById(migrateToId);
            if (targetCat == null) {
                throw new BusinessException(404, "目标分类不存在");
            }
            // Migrate records
            List<LedgerRecord> records = ledgerRecordMapper.selectList(
                    new LambdaQueryWrapper<LedgerRecord>()
                            .eq(LedgerRecord::getCategoryId, id));
            for (LedgerRecord r : records) {
                r.setCategoryId(migrateToId);
                ledgerRecordMapper.updateById(r);
            }
        }
        ledgerCategoryMapper.deleteById(id);
    }

    // ========== 预算 CRUD ==========

    public List<BudgetVO> getBudgets(Long userId, Integer year, Integer month) {
        int y = year != null ? year : LocalDate.now().getYear();
        int m = month != null ? month : LocalDate.now().getMonthValue();
        LocalDate start = LocalDate.of(y, m, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        List<LedgerBudget> budgets = ledgerBudgetMapper.selectList(
                new LambdaQueryWrapper<LedgerBudget>()
                        .eq(LedgerBudget::getUserId, userId)
                        .eq(LedgerBudget::getYear, y)
                        .eq(LedgerBudget::getMonth, m));

        // Get actual spending per category for this month
        List<LedgerRecord> records = ledgerRecordMapper.selectList(
                new LambdaQueryWrapper<LedgerRecord>()
                        .eq(LedgerRecord::getUserId, userId)
                        .eq(LedgerRecord::getType, 0) // 支出 only
                        .ge(LedgerRecord::getRecordDate, start)
                        .le(LedgerRecord::getRecordDate, end));

        Map<Long, BigDecimal> spentByCat = new HashMap<>();
        for (LedgerRecord r : records) {
            spentByCat.merge(r.getCategoryId(), r.getAmount(), BigDecimal::add);
        }

        List<BudgetVO> vos = new ArrayList<>();
        for (LedgerBudget b : budgets) {
            BudgetVO vo = new BudgetVO();
            vo.setId(b.getId());
            vo.setCategoryId(b.getCategoryId());
            vo.setMonthlyLimit(b.getMonthlyLimit());
            vo.setYear(b.getYear());
            vo.setMonth(b.getMonth());

            LedgerCategory cat = ledgerCategoryMapper.selectById(b.getCategoryId());
            if (cat != null) {
                vo.setCategoryName(cat.getName());
                vo.setCategoryIcon(cat.getIcon());
            }

            BigDecimal spent = spentByCat.getOrDefault(b.getCategoryId(), BigDecimal.ZERO);
            vo.setSpent(spent);
            if (b.getMonthlyLimit().compareTo(BigDecimal.ZERO) > 0) {
                vo.setPercentage(spent.multiply(new BigDecimal("100"))
                        .divide(b.getMonthlyLimit(), 1, RoundingMode.HALF_UP));
            } else {
                vo.setPercentage(BigDecimal.ZERO);
            }
            vos.add(vo);
        }
        return vos;
    }

    public BudgetVO saveBudget(Long userId, CreateBudgetDTO dto) {
        // Upsert: check if budget already exists for this user/category/year/month
        LedgerBudget existing = ledgerBudgetMapper.selectOne(
                new LambdaQueryWrapper<LedgerBudget>()
                        .eq(LedgerBudget::getUserId, userId)
                        .eq(LedgerBudget::getCategoryId, dto.getCategoryId())
                        .eq(LedgerBudget::getYear, dto.getYear())
                        .eq(LedgerBudget::getMonth, dto.getMonth()));

        LedgerBudget budget;
        if (existing != null) {
            existing.setMonthlyLimit(dto.getMonthlyLimit());
            ledgerBudgetMapper.updateById(existing);
            budget = existing;
        } else {
            budget = new LedgerBudget();
            budget.setUserId(userId);
            budget.setCategoryId(dto.getCategoryId());
            budget.setMonthlyLimit(dto.getMonthlyLimit());
            budget.setYear(dto.getYear());
            budget.setMonth(dto.getMonth());
            ledgerBudgetMapper.insert(budget);
        }

        BudgetVO vo = new BudgetVO();
        vo.setId(budget.getId());
        vo.setCategoryId(budget.getCategoryId());
        vo.setMonthlyLimit(budget.getMonthlyLimit());
        vo.setYear(budget.getYear());
        vo.setMonth(budget.getMonth());

        LedgerCategory cat = ledgerCategoryMapper.selectById(budget.getCategoryId());
        if (cat != null) {
            vo.setCategoryName(cat.getName());
            vo.setCategoryIcon(cat.getIcon());
        }
        return vo;
    }

    public BudgetVO updateBudget(Long userId, Long id, UpdateBudgetDTO dto) {
        LedgerBudget budget = ledgerBudgetMapper.selectById(id);
        if (budget == null || !budget.getUserId().equals(userId)) {
            throw new BusinessException(404, "预算不存在");
        }
        if (dto.getMonthlyLimit() != null) {
            budget.setMonthlyLimit(dto.getMonthlyLimit());
        }
        ledgerBudgetMapper.updateById(budget);

        BudgetVO vo = new BudgetVO();
        vo.setId(budget.getId());
        vo.setCategoryId(budget.getCategoryId());
        vo.setMonthlyLimit(budget.getMonthlyLimit());
        vo.setYear(budget.getYear());
        vo.setMonth(budget.getMonth());

        LedgerCategory cat = ledgerCategoryMapper.selectById(budget.getCategoryId());
        if (cat != null) {
            vo.setCategoryName(cat.getName());
            vo.setCategoryIcon(cat.getIcon());
        }
        return vo;
    }

    public void deleteBudget(Long userId, Long id) {
        LedgerBudget budget = ledgerBudgetMapper.selectById(id);
        if (budget == null || !budget.getUserId().equals(userId)) {
            throw new BusinessException(404, "预算不存在");
        }
        ledgerBudgetMapper.deleteById(id);
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
