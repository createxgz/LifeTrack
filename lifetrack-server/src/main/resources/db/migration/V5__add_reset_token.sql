ALTER TABLE users
  ADD COLUMN reset_token VARCHAR(100) NULL COMMENT '密码重置Token',
  ADD COLUMN reset_token_exp DATETIME NULL COMMENT '重置Token过期时间',
  ADD INDEX idx_reset_token (reset_token);
