-- V1__init_tables.sql
-- LifeTrack 数据库初始化脚本

-- 用户表
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    birthday DATE,
    gender TINYINT,
    height DECIMAL(5,2),
    target_weight DECIMAL(5,2),
    notify_email VARCHAR(100),
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 任务表
CREATE TABLE tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(100) NOT NULL COMMENT '任务名称',
    description TEXT COMMENT '任务描述',
    repeat_type TINYINT NOT NULL COMMENT '0=一次性 1=每日 2=每周 3=每月 4=自定义',
    cron_expr VARCHAR(50) COMMENT 'Quartz Cron表达式',
    remind_time TIME COMMENT '提醒时间',
    start_date DATE NOT NULL,
    end_date DATE COMMENT 'NULL表示永久',
    status TINYINT DEFAULT 1 COMMENT '0=暂停 1=进行中 2=已完成',
    streak_days INT DEFAULT 0 COMMENT '当前连续天数',
    max_streak_days INT DEFAULT 0 COMMENT '历史最长连续天数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    INDEX idx_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- 打卡记录表
CREATE TABLE task_checkins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    checkin_date DATE NOT NULL,
    note VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_task_date (task_id, checkin_date),
    INDEX idx_user_date (user_id, checkin_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡记录表';

-- 提醒配置表
CREATE TABLE task_reminders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    remind_time TIME NOT NULL COMMENT '提醒时间',
    is_enabled TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提醒配置表';

-- 体重记录表
CREATE TABLE weight_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    weight_kg DECIMAL(5,2) NOT NULL,
    bmi DECIMAL(5,2),
    body_fat_rate DECIMAL(5,2),
    muscle_mass DECIMAL(5,2),
    record_date DATE NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_date (user_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体重记录表';

-- 饮食记录表
CREATE TABLE diet_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    meal_type VARCHAR(20) COMMENT 'BREAKFAST/LUNCH/DINNER/SNACK',
    food_id BIGINT,
    quantity DECIMAL(10,2),
    calories DECIMAL(10,2),
    protein DECIMAL(10,2),
    fat DECIMAL(10,2),
    carbs DECIMAL(10,2),
    record_time DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_time (user_id, record_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='饮食记录表';

-- 食物热量库
CREATE TABLE foods (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '食物名称',
    unit VARCHAR(20) NOT NULL COMMENT '计量单位',
    calories_per_unit DECIMAL(10,2) NOT NULL COMMENT '每单位热量(kcal)',
    protein DECIMAL(10,2) DEFAULT 0 COMMENT '蛋白质(g)',
    fat DECIMAL(10,2) DEFAULT 0 COMMENT '脂肪(g)',
    carbs DECIMAL(10,2) DEFAULT 0 COMMENT '碳水化合物(g)',
    category VARCHAR(50) COMMENT '分类',
    is_default TINYINT DEFAULT 1 COMMENT '是否系统默认数据',
    created_by BIGINT COMMENT '创建者用户ID（自定义食物）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    INDEX idx_name (name),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食物热量库';

-- 账单记录表
CREATE TABLE ledger_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type TINYINT NOT NULL COMMENT '0=支出 1=收入',
    amount DECIMAL(10,2) NOT NULL COMMENT '金额（元）',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    payment VARCHAR(20) COMMENT '支付方式：cash/alipay/wechat/card',
    note VARCHAR(200),
    record_date DATE NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    INDEX idx_user_date (user_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单记录表';

-- 账单分类表
CREATE TABLE ledger_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT 'NULL表示系统默认分类',
    name VARCHAR(50) NOT NULL,
    type TINYINT NOT NULL COMMENT '0=支出 1=收入',
    icon VARCHAR(50),
    color VARCHAR(20),
    is_default TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单分类表';

-- 预算表
CREATE TABLE ledger_budgets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    monthly_limit DECIMAL(10,2) NOT NULL,
    year INT NOT NULL,
    month INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_category_month (user_id, category_id, year, month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预算表';

-- 插入默认账单分类
INSERT INTO ledger_categories (user_id, name, type, icon, is_default) VALUES
(NULL, '工资', 1, 'Wallet', 1),
(NULL, '兼职', 1, 'Money', 1),
(NULL, '投资', 1, 'TrendCharts', 1),
(NULL, '其他收入', 1, 'More', 1),
(NULL, '餐饮', 0, 'Food', 1),
(NULL, '购物', 0, 'ShoppingBag', 1),
(NULL, '交通', 0, 'Car', 1),
(NULL, '娱乐', 0, 'Game', 1),
(NULL, '医疗', 0, 'FirstAidKit', 1),
(NULL, '居家', 0, 'House', 1),
(NULL, '其他支出', 0, 'More', 1);

-- 插入默认食物数据
INSERT INTO foods (name, unit, calories_per_unit, protein, fat, carbs, category, is_default) VALUES
('米饭', '100g', 116, 2.6, 0.3, 25.9, '主食', 1),
('馒头', '100g', 223, 7.0, 1.1, 44.2, '主食', 1),
('面条(煮)', '100g', 110, 3.5, 0.3, 22.0, '主食', 1),
('全麦面包', '100g', 246, 10.4, 3.3, 42.1, '主食', 1),
('鸡胸肉', '100g', 133, 31.0, 1.2, 0.0, '肉类', 1),
('鸡蛋', '1个(50g)', 72, 6.5, 4.9, 0.6, '蛋奶', 1),
('牛奶(全脂)', '100ml', 61, 3.0, 3.2, 4.8, '蛋奶', 1),
('苹果', '1个(200g)', 104, 0.4, 0.4, 27.6, '水果', 1),
('香蕉', '1根(120g)', 105, 1.4, 0.2, 26.4, '水果', 1),
('西兰花', '100g', 34, 2.8, 0.4, 6.6, '蔬菜', 1),
('番茄', '100g', 18, 0.9, 0.2, 3.9, '蔬菜', 1),
('牛肉(瘦)', '100g', 125, 22.0, 4.2, 0.0, '肉类', 1),
('猪肉(瘦)', '100g', 143, 20.3, 6.2, 0.0, '肉类', 1),
('三文鱼', '100g', 139, 20.4, 6.1, 0.0, '肉类', 1);
