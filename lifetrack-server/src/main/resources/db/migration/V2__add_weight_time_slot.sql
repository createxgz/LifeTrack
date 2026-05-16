-- V2__add_weight_time_slot.sql
-- 体重记录添加早中晚时段字段

ALTER TABLE weight_records
    ADD COLUMN time_slot VARCHAR(10) NULL COMMENT 'MORNING/AFTERNOON/EVENING 早/中/晚' AFTER record_date;
