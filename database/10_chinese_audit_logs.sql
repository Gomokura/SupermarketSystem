-- ============================================================
-- Supermarket Management System - Chinese Audit Log Demo Data
-- Purpose: Fill admin audit log page with readable Chinese operation records.
-- Safe to run multiple times.
-- ============================================================

SET FEEDBACK OFF
SET ECHO OFF

PROMPT ============================================================
PROMPT Chinese audit log data started...
PROMPT ============================================================

DECLARE
    v_admin_id NUMBER;
    v_exists NUMBER;

    PROCEDURE ensure_log(
        p_admin_id IN NUMBER,
        p_admin_name IN VARCHAR2,
        p_module IN VARCHAR2,
        p_action IN VARCHAR2,
        p_target_table IN VARCHAR2,
        p_target_id IN NUMBER,
        p_before IN CLOB,
        p_after IN CLOB,
        p_ip IN VARCHAR2,
        p_minutes_ago IN NUMBER
    ) IS
    BEGIN
        SELECT COUNT(*) INTO v_exists
          FROM audit_logs
         WHERE module = p_module
           AND action = p_action
           AND target_id = p_target_id
           AND create_time >= SYSTIMESTAMP - NUMTODSINTERVAL(2, 'DAY');

        IF v_exists = 0 THEN
            INSERT INTO audit_logs (
                log_id, admin_id, admin_name, module, action,
                target_table, target_id, before_data, after_data,
                ip_address, create_time
            ) VALUES (
                seq_audit_logs.NEXTVAL, p_admin_id, p_admin_name, p_module, p_action,
                p_target_table, p_target_id, p_before, p_after,
                p_ip, SYSTIMESTAMP - NUMTODSINTERVAL(p_minutes_ago, 'MINUTE')
            );
        END IF;
    END;
BEGIN
    SELECT admin_id INTO v_admin_id FROM admin_users WHERE username = 'admin' AND ROWNUM = 1;

    ensure_log(v_admin_id, '超级管理员', 'PRODUCT', 'CREATE', 'PRODUCTS', 1027,
        NULL,
        '{"商品名称":"雪花勇闯天涯 500ml","分类":"啤酒","售价":5.90,"库存":420,"状态":"上架"}',
        '127.0.0.1', 8);

    ensure_log(v_admin_id, '超级管理员', 'PRODUCT', 'UPDATE', 'PRODUCTS', 1013,
        '{"售价":82.90,"库存":96}',
        '{"售价":79.90,"库存":120,"备注":"周末坚果活动调价"}',
        '127.0.0.1', 18);

    ensure_log(v_admin_id, '超级管理员', 'INVENTORY', 'UPDATE', 'PRODUCTS', 1020,
        '{"库存":88}',
        '{"库存":140,"变动原因":"供应商到货入库"}',
        '127.0.0.1', 33);

    ensure_log(v_admin_id, '超级管理员', 'ORDER', 'STATUS_CHANGE', 'ORDERS', 35,
        '{"订单状态":"待发货"}',
        '{"订单状态":"配送中","配送员":"张晨"}',
        '127.0.0.1', 51);

    ensure_log(v_admin_id, '超级管理员', 'USER', 'STATUS_CHANGE', 'USERS', 1,
        '{"会员等级":"普通","积分":0}',
        '{"会员等级":"普通","积分":520,"备注":"初始化演示积分"}',
        '127.0.0.1', 74);

    ensure_log(v_admin_id, '超级管理员', 'COUPON', 'CREATE', 'COUPONS', 1,
        NULL,
        '{"优惠券":"新人满50减10","门槛":50,"面值":10,"状态":"启用"}',
        '127.0.0.1', 96);

    ensure_log(v_admin_id, '超级管理员', 'PROMOTION', 'CREATE', 'ACTIVITIES', 1,
        NULL,
        '{"活动名称":"周末生鲜满99减20","活动类型":"满减","状态":"进行中"}',
        '127.0.0.1', 125);

    ensure_log(v_admin_id, '超级管理员', 'AFTER_SALE', 'UPDATE', 'AFTER_SALES', 1,
        '{"处理状态":"待审核"}',
        '{"处理状态":"已通过","处理意见":"商品破损，安排退款"}',
        '127.0.0.1', 155);

    ensure_log(v_admin_id, '超级管理员', 'SYSTEM', 'UPDATE', 'SYSTEM_CONFIG', 1,
        '{"配送时段":"默认"}',
        '{"配送时段":"今日18:00-21:00","备注":"统一演示配送时段"}',
        '127.0.0.1', 190);

    ensure_log(v_admin_id, '超级管理员', 'ORDER', 'UPDATE', 'ORDERS', 39,
        '{"配送异常":null}',
        '{"配送异常":"客户电话无人接听，已留言等待重新配送"}',
        '127.0.0.1', 230);

    COMMIT;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('No admin user found. Please run base demo data first.');
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('Audit log data insert error: ' || SQLERRM);
END;
/

COMMIT;

PROMPT ============================================================
PROMPT Chinese audit log data completed.
PROMPT ============================================================
