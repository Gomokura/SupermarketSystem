-- ============================================================
-- 超市管理系统 · 补全缺失列
-- 文件：10_add_missing_columns.sql
-- 在 08/09 之后执行
-- ============================================================

-- ORDERS 表补列（发货/取消/完成时间 + 配送员 + 支付方式字段规范化）
ALTER TABLE ORDERS ADD ship_time DATE;
ALTER TABLE ORDERS ADD cancel_time DATE;
ALTER TABLE ORDERS ADD complete_time DATE;
ALTER TABLE ORDERS ADD delivery_person_id NUMBER;
ALTER TABLE ORDERS ADD pay_method VARCHAR2(30);

-- ORDERS 状态值统一为英文（如有历史数据需同步更新）
-- pending=待支付 / paid=待发货 / shipped=已发货 / completed=已完成 / cancelled=已取消

-- ADMINS 表补列（兼容 last_login_time）
-- 注：migration 09 建表时字段名为 last_login_time，实体映射字段为 lastLoginTime
-- 如果已存在 last_login 列，执行：
-- ALTER TABLE ADMINS RENAME COLUMN last_login TO last_login_time;

-- PRODUCTS 表：cover_image 列（如果 image_url 需迁移）
ALTER TABLE PRODUCTS ADD cover_image VARCHAR2(255);
ALTER TABLE PRODUCTS ADD avg_rating NUMBER(3,1) DEFAULT 0;
ALTER TABLE PRODUCTS ADD update_time DATE;
ALTER TABLE PRODUCTS ADD description VARCHAR2(2000);

-- USERS 表：update_time
ALTER TABLE USERS ADD update_time DATE;

-- ORDER_ITEMS 表：product_image 列
ALTER TABLE ORDER_ITEMS ADD product_image VARCHAR2(255);

-- AFTER_SALES：handler_id（处理人）
-- 注：表中已有 reject_reason，entity 用 adminRemark 映射此列

COMMIT;
