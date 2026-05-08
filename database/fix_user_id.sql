-- 1. 先删除外键约束（如果存在）
DECLARE
  v_cnt NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_cnt FROM user_constraints
  WHERE constraint_name = 'ORDERS_USER_FK' AND table_name = 'ORDERS';
  IF v_cnt > 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ORDERS DROP CONSTRAINT ORDERS_USER_FK';
    DBMS_OUTPUT.PUT_LINE('已删除外键约束 ORDERS_USER_FK');
  ELSE
    DBMS_OUTPUT.PUT_LINE('外键约束 ORDERS_USER_FK 不存在，跳过');
  END IF;
END;
/

-- 2. 将 user_id 改为允许 NULL
ALTER TABLE ORDERS MODIFY (user_id NULL);
COMMIT;

-- 3. 验证
SELECT column_name, nullable FROM user_tab_columns
WHERE table_name = 'ORDERS' AND column_name = 'USER_ID';
