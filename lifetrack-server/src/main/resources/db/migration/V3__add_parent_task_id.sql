-- V3__add_parent_task_id.sql
-- 任务表添加父子任务层级支持

ALTER TABLE tasks
    ADD COLUMN parent_task_id BIGINT NULL COMMENT '父任务ID，NULL表示顶层任务' AFTER id,
    ADD INDEX idx_parent_task (parent_task_id);
