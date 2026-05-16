-- V4__add_wallet_accounts.sql
-- 钱包账户表 + 账单记录分类索引

-- 钱包账户表
CREATE TABLE wallet_accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(50) NOT NULL COMMENT '账户名称',
    amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '金额',
    account_type VARCHAR(20) COMMENT '账户类型：bank/alipay/wechat/cash/investment/other',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包账户表';

-- ledger_records 添加分类索引（加速按分类筛选和迁移查询）
ALTER TABLE ledger_records ADD INDEX idx_category (category_id);
