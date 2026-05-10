-- ============================================================
-- Supermarket Management System - Chinese Admin Demo Data
-- File: 05_chinese_admin_delivery_promotion_data.sql
-- Purpose: Enrich admin delivery and promotion pages with Chinese demo data
-- Safe to run multiple times: checks names/order numbers/phones before insert
-- ============================================================

SET FEEDBACK OFF
SET ECHO OFF

PROMPT ============================================================
PROMPT Chinese admin delivery/promotion data started...
PROMPT ============================================================

-- ============================================================
-- 1. Chinese couriers for admin delivery assignment
-- ============================================================
DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM delivery_persons WHERE phone = '13910001001';
    IF v_count = 0 THEN
        INSERT INTO delivery_persons (courier_id, real_name, phone, password, total_delivery_count, status, create_time)
        VALUES (seq_delivery_persons.NEXTVAL, '陈晨', '13910001001', 'courier123', 86, 'active', SYSTIMESTAMP - NUMTODSINTERVAL(180, 'DAY'));
    END IF;

    SELECT COUNT(*) INTO v_count FROM delivery_persons WHERE phone = '13910001002';
    IF v_count = 0 THEN
        INSERT INTO delivery_persons (courier_id, real_name, phone, password, total_delivery_count, status, create_time)
        VALUES (seq_delivery_persons.NEXTVAL, '周敏', '13910001002', 'courier123', 132, 'active', SYSTIMESTAMP - NUMTODSINTERVAL(220, 'DAY'));
    END IF;

    SELECT COUNT(*) INTO v_count FROM delivery_persons WHERE phone = '13910001003';
    IF v_count = 0 THEN
        INSERT INTO delivery_persons (courier_id, real_name, phone, password, total_delivery_count, status, create_time)
        VALUES (seq_delivery_persons.NEXTVAL, '刘海峰', '13910001003', 'courier123', 54, 'active', SYSTIMESTAMP - NUMTODSINTERVAL(95, 'DAY'));
    END IF;

    SELECT COUNT(*) INTO v_count FROM delivery_persons WHERE phone = '13910001004';
    IF v_count = 0 THEN
        INSERT INTO delivery_persons (courier_id, real_name, phone, password, total_delivery_count, status, create_time)
        VALUES (seq_delivery_persons.NEXTVAL, '赵雅琴', '13910001004', 'courier123', 27, 'active', SYSTIMESTAMP - NUMTODSINTERVAL(45, 'DAY'));
    END IF;

    COMMIT;
END;
/

-- ============================================================
-- 2. Chinese promotion activities
-- ============================================================
DECLARE
    v_count NUMBER;
    v_activity_id NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM activities WHERE activity_name = '周末生鲜满99减20';
    IF v_count = 0 THEN
        INSERT INTO activities (activity_id, activity_name, activity_type, scope_type, start_time, end_time, status, create_time)
        VALUES (seq_activities.NEXTVAL, '周末生鲜满99减20', 'FULL_REDUCE', 'ALL',
                CAST(TRUNC(SYSDATE) - 1 AS TIMESTAMP),
                CAST(TRUNC(SYSDATE) + 14 AS TIMESTAMP) + NUMTODSINTERVAL(86399, 'SECOND'),
                'active', SYSTIMESTAMP)
        RETURNING activity_id INTO v_activity_id;
        INSERT INTO full_reduce_rules (rule_id, activity_id, threshold, reduce_amount, sort_order)
        VALUES (seq_full_reduce_rules.NEXTVAL, v_activity_id, 99, 20, 1);
    END IF;

    SELECT COUNT(*) INTO v_count FROM activities WHERE activity_name = '会员日全场满199减35';
    IF v_count = 0 THEN
        INSERT INTO activities (activity_id, activity_name, activity_type, scope_type, start_time, end_time, status, create_time)
        VALUES (seq_activities.NEXTVAL, '会员日全场满199减35', 'FULL_REDUCE', 'ALL',
                CAST(TRUNC(SYSDATE) AS TIMESTAMP),
                CAST(TRUNC(SYSDATE) + 30 AS TIMESTAMP) + NUMTODSINTERVAL(86399, 'SECOND'),
                'active', SYSTIMESTAMP)
        RETURNING activity_id INTO v_activity_id;
        INSERT INTO full_reduce_rules (rule_id, activity_id, threshold, reduce_amount, sort_order)
        VALUES (seq_full_reduce_rules.NEXTVAL, v_activity_id, 199, 35, 1);
    END IF;

    SELECT COUNT(*) INTO v_count FROM activities WHERE activity_name = '家庭清洁用品第二件半价';
    IF v_count = 0 THEN
        INSERT INTO activities (activity_id, activity_name, activity_type, scope_type, start_time, end_time, status, create_time)
        VALUES (seq_activities.NEXTVAL, '家庭清洁用品第二件半价', 'DISCOUNT', 'CATEGORY',
                CAST(TRUNC(SYSDATE) - 3 AS TIMESTAMP),
                CAST(TRUNC(SYSDATE) + 21 AS TIMESTAMP) + NUMTODSINTERVAL(86399, 'SECOND'),
                'active', SYSTIMESTAMP);
    END IF;

    SELECT COUNT(*) INTO v_count FROM activities WHERE activity_name = '早餐牛奶面包组合价';
    IF v_count = 0 THEN
        INSERT INTO activities (activity_id, activity_name, activity_type, scope_type, start_time, end_time, status, create_time)
        VALUES (seq_activities.NEXTVAL, '早餐牛奶面包组合价', 'DISCOUNT', 'ALL',
                CAST(TRUNC(SYSDATE) AS TIMESTAMP),
                CAST(TRUNC(SYSDATE) + 10 AS TIMESTAMP) + NUMTODSINTERVAL(86399, 'SECOND'),
                'active', SYSTIMESTAMP);
    END IF;

    SELECT COUNT(*) INTO v_count FROM activities WHERE activity_name = '进口零食尝鲜折扣';
    IF v_count = 0 THEN
        INSERT INTO activities (activity_id, activity_name, activity_type, scope_type, start_time, end_time, status, create_time)
        VALUES (seq_activities.NEXTVAL, '进口零食尝鲜折扣', 'DISCOUNT', 'CATEGORY',
                CAST(TRUNC(SYSDATE) - 2 AS TIMESTAMP),
                CAST(TRUNC(SYSDATE) + 18 AS TIMESTAMP) + NUMTODSINTERVAL(86399, 'SECOND'),
                'active', SYSTIMESTAMP);
    END IF;

    SELECT COUNT(*) INTO v_count FROM activities WHERE activity_name = '下月预热：粮油囤货节';
    IF v_count = 0 THEN
        INSERT INTO activities (activity_id, activity_name, activity_type, scope_type, start_time, end_time, status, create_time)
        VALUES (seq_activities.NEXTVAL, '下月预热：粮油囤货节', 'FULL_REDUCE', 'ALL',
                CAST(TRUNC(SYSDATE) + 7 AS TIMESTAMP),
                CAST(TRUNC(SYSDATE) + 35 AS TIMESTAMP) + NUMTODSINTERVAL(86399, 'SECOND'),
                'inactive', SYSTIMESTAMP)
        RETURNING activity_id INTO v_activity_id;
        INSERT INTO full_reduce_rules (rule_id, activity_id, threshold, reduce_amount, sort_order)
        VALUES (seq_full_reduce_rules.NEXTVAL, v_activity_id, 299, 50, 1);
    END IF;

    COMMIT;
END;
/

-- ============================================================
-- 3. Chinese orders and delivery tasks for admin delivery page
-- ============================================================
DECLARE
    v_user_id NUMBER;
    v_order_id NUMBER;
    v_courier_1 NUMBER;
    v_courier_2 NUMBER;
    v_courier_3 NUMBER;
    v_courier_4 NUMBER;
    v_count NUMBER;

    PROCEDURE ensure_delivery_order(
        p_order_no IN VARCHAR2,
        p_receiver_snapshot IN VARCHAR2,
        p_amount IN NUMBER,
        p_order_status IN VARCHAR2,
        p_task_status IN VARCHAR2,
        p_courier_id IN NUMBER,
        p_assign_offset IN VARCHAR2,
        p_pickup_offset IN VARCHAR2,
        p_deliver_offset IN VARCHAR2,
        p_fail_reason IN VARCHAR2
    ) IS
    BEGIN
        SELECT COUNT(*) INTO v_count FROM orders WHERE order_no = p_order_no;
        IF v_count = 0 THEN
            INSERT INTO orders (
                order_id, order_no, user_id, source, receiver_snapshot,
                total_amount, discount_amount, coupon_discount, points_deduct_amount,
                freight_amount, pay_amount, pay_method, points_used,
                delivery_time_slot, remark, status, pay_time, ship_time, create_time, update_time
            ) VALUES (
                seq_orders.NEXTVAL, p_order_no, v_user_id, 'ONLINE', p_receiver_snapshot,
                p_amount, 0, 0, 0,
                0, p_amount, 'MOCK', 0,
                '今日 18:00-21:00', '中文演示配送订单', p_order_status,
                SYSTIMESTAMP - NUMTODSINTERVAL(8, 'HOUR'), SYSTIMESTAMP - NUMTODSINTERVAL(6, 'HOUR'),
                SYSTIMESTAMP - NUMTODSINTERVAL(9, 'HOUR'), SYSTIMESTAMP - NUMTODSINTERVAL(1, 'HOUR')
            )
            RETURNING order_id INTO v_order_id;
        ELSE
            SELECT order_id INTO v_order_id FROM orders WHERE order_no = p_order_no;
        END IF;

        SELECT COUNT(*) INTO v_count FROM delivery_tasks WHERE order_id = v_order_id;
        IF v_count = 0 THEN
            INSERT INTO delivery_tasks (
                task_id, order_id, courier_id, status, fail_reason,
                assign_time, pickup_time, deliver_time
            ) VALUES (
                seq_delivery_tasks.NEXTVAL, v_order_id, p_courier_id, p_task_status, p_fail_reason,
                SYSTIMESTAMP - TO_DSINTERVAL(p_assign_offset),
                CASE WHEN p_pickup_offset IS NULL THEN NULL ELSE SYSTIMESTAMP - TO_DSINTERVAL(p_pickup_offset) END,
                CASE WHEN p_deliver_offset IS NULL THEN NULL ELSE SYSTIMESTAMP - TO_DSINTERVAL(p_deliver_offset) END
            );
        END IF;
    END;
BEGIN
    SELECT user_id INTO v_user_id FROM (SELECT user_id FROM users ORDER BY user_id) WHERE ROWNUM = 1;

    SELECT courier_id INTO v_courier_1 FROM delivery_persons WHERE phone = '13910001001';
    SELECT courier_id INTO v_courier_2 FROM delivery_persons WHERE phone = '13910001002';
    SELECT courier_id INTO v_courier_3 FROM delivery_persons WHERE phone = '13910001003';
    SELECT courier_id INTO v_courier_4 FROM delivery_persons WHERE phone = '13910001004';

    ensure_delivery_order('SMCN20260510001', '林小雨 13810002001 广东省广州市天河区体育西路88号幸福花园3栋1201', 128.60, 'PENDING_SHIP', 'ASSIGNED', v_courier_1, '0 01:20:00', NULL, NULL, NULL);
    ensure_delivery_order('SMCN20260510002', '何明轩 13810002002 广东省广州市越秀区东风中路268号时代广场A座902', 76.30, 'SHIPPING', 'PICKED_UP', v_courier_2, '0 02:10:00', '0 01:35:00', NULL, NULL);
    ensure_delivery_order('SMCN20260510003', '苏婉婷 13810002003 广东省广州市海珠区江南西路45号丽景苑6栋502', 214.90, 'COMPLETED', 'DELIVERED', v_courier_3, '0 05:30:00', '0 04:50:00', '0 03:20:00', NULL);
    ensure_delivery_order('SMCN20260510004', '周启航 13810002004 广东省广州市白云区云城西路199号云尚公寓B座1510', 58.80, 'SHIPPING', 'FAILED', v_courier_4, '0 04:00:00', '0 03:15:00', NULL, '客户电话暂时无法接通，已留言等待重新配送');
    ensure_delivery_order('SMCN20260510005', '陈思琪 13810002005 广东省广州市番禺区大学城外环西路230号学生公寓9栋', 96.50, 'PENDING_SHIP', 'ASSIGNED', v_courier_1, '0 00:45:00', NULL, NULL, NULL);
    ensure_delivery_order('SMCN20260510006', '黄俊杰 13810002006 广东省佛山市南海区桂城街道灯湖西路20号保利花园', 183.20, 'SHIPPING', 'PICKED_UP', v_courier_2, '0 03:10:00', '0 02:40:00', NULL, NULL);
    ensure_delivery_order('SMCN20260510007', '赵雅雯 13810002007 广东省深圳市南山区科技园科苑路15号科兴科学园B2栋', 145.70, 'COMPLETED', 'DELIVERED', v_courier_3, '1 02:00:00', '1 01:30:00', '1 00:20:00', NULL);
    ensure_delivery_order('SMCN20260510008', '许家宁 13810002008 广东省广州市荔湾区中山八路23号富力广场2栋808', 67.40, 'SHIPPING', 'FAILED', v_courier_4, '0 06:00:00', '0 05:10:00', NULL, '小区临时管控，需改约明日上午配送');

    COMMIT;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('No users/couriers found. Please run base demo data first.');
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('Chinese delivery data insert error: ' || SQLERRM);
END;
/

COMMIT;

PROMPT ============================================================
PROMPT Chinese admin delivery/promotion data completed.
PROMPT ============================================================
