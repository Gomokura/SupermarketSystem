-- ============================================================
-- Fix: 移除 INVENTORY_LOGS.OPERATOR_ID 外键约束
-- 原因：OPERATOR_ID 应指向 ADMIN_USERS 表，但 createOrder 传入的是 userId（USERS 表），
--       导致 ORA-02291 违反约束。下单是用户行为，operatorId = userId，
--       但 FK_IL_OPERATOR 要求 operatorId 必须在 ADMIN_USERS 中。
-- ============================================================

-- 检查约束是否存在
SELECT constraint_name FROM user_constraints
WHERE table_name = 'INVENTORY_LOGS' AND constraint_name = 'FK_IL_OPERATOR';

-- 如果约束存在则删除
ALTER TABLE INVENTORY_LOGS DROP CONSTRAINT FK_IL_OPERATOR;

-- 重新添加外键，指向正确的用户表
-- OPERATOR_ID 可以是 USERS 表的 user_id（顾客下单）或 ADMIN_USERS 表的 admin_id（管理员操作）
-- 改为允许 NULL（与业务逻辑一致：有些操作不需要记录操作员）
ALTER TABLE INVENTORY_LOGS MODIFY OPERATOR_ID NUMBER NULL;

-- 确认结果
SELECT column_name, data_type, nullable
FROM user_tab_columns
WHERE table_name = 'INVENTORY_LOGS' AND column_name = 'OPERATOR_ID';
