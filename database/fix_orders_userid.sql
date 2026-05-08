-- 允许收银订单的 user_id 为 NULL（散客）
ALTER TABLE ORDERS MODIFY (user_id NULL);
COMMIT;
