-- ============================================================
-- Supermarket Management System - Chinese Courier Demo Data
-- Purpose: Fill courier terminal with synchronized order + delivery task data
-- Safe to run multiple times.
-- ============================================================

SET FEEDBACK OFF
SET ECHO OFF

PROMPT ============================================================
PROMPT Chinese courier demo data started...
PROMPT ============================================================

DECLARE
    v_user_id NUMBER;
    v_order_id NUMBER;
    v_courier_id NUMBER;
    v_count NUMBER;

    PROCEDURE ensure_task(
        p_order_no IN VARCHAR2,
        p_receiver_snapshot IN VARCHAR2,
        p_amount IN NUMBER,
        p_order_status IN VARCHAR2,
        p_task_status IN VARCHAR2,
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
                delivery_time_slot, remark, status, pay_time, ship_time,
                pickup_time, deliver_time, create_time, update_time
            ) VALUES (
                seq_orders.NEXTVAL, p_order_no, v_user_id, 'ONLINE', p_receiver_snapshot,
                p_amount, 0, 0, 0,
                0, p_amount, 'MOCK', 0,
                '今日 18:00-21:00', '配送员端中文演示订单', p_order_status,
                SYSTIMESTAMP - NUMTODSINTERVAL(7, 'HOUR'),
                SYSTIMESTAMP - NUMTODSINTERVAL(6, 'HOUR'),
                CASE WHEN p_pickup_offset IS NULL THEN NULL ELSE SYSTIMESTAMP - TO_DSINTERVAL(p_pickup_offset) END,
                CASE WHEN p_deliver_offset IS NULL THEN NULL ELSE SYSTIMESTAMP - TO_DSINTERVAL(p_deliver_offset) END,
                SYSTIMESTAMP - NUMTODSINTERVAL(8, 'HOUR'),
                SYSTIMESTAMP
            )
            RETURNING order_id INTO v_order_id;
        ELSE
            SELECT order_id INTO v_order_id FROM orders WHERE order_no = p_order_no;
            UPDATE orders
               SET user_id = v_user_id,
                   source = 'ONLINE',
                   receiver_snapshot = p_receiver_snapshot,
                   total_amount = p_amount,
                   pay_amount = p_amount,
                   status = p_order_status,
                   delivery_time_slot = '今日 18:00-21:00',
                   remark = '配送员端中文演示订单',
                   pickup_time = CASE WHEN p_pickup_offset IS NULL THEN NULL ELSE SYSTIMESTAMP - TO_DSINTERVAL(p_pickup_offset) END,
                   deliver_time = CASE WHEN p_deliver_offset IS NULL THEN NULL ELSE SYSTIMESTAMP - TO_DSINTERVAL(p_deliver_offset) END,
                   update_time = SYSTIMESTAMP
             WHERE order_id = v_order_id;
        END IF;

        SELECT COUNT(*) INTO v_count FROM delivery_tasks WHERE order_id = v_order_id;
        IF v_count = 0 THEN
            INSERT INTO delivery_tasks (
                task_id, order_id, courier_id, status, fail_reason,
                assign_time, pickup_time, deliver_time
            ) VALUES (
                seq_delivery_tasks.NEXTVAL, v_order_id, v_courier_id, p_task_status, p_fail_reason,
                SYSTIMESTAMP - TO_DSINTERVAL(p_assign_offset),
                CASE WHEN p_pickup_offset IS NULL THEN NULL ELSE SYSTIMESTAMP - TO_DSINTERVAL(p_pickup_offset) END,
                CASE WHEN p_deliver_offset IS NULL THEN NULL ELSE SYSTIMESTAMP - TO_DSINTERVAL(p_deliver_offset) END
            );
        ELSE
            UPDATE delivery_tasks
               SET courier_id = v_courier_id,
                   status = p_task_status,
                   fail_reason = p_fail_reason,
                   assign_time = SYSTIMESTAMP - TO_DSINTERVAL(p_assign_offset),
                   pickup_time = CASE WHEN p_pickup_offset IS NULL THEN NULL ELSE SYSTIMESTAMP - TO_DSINTERVAL(p_pickup_offset) END,
                   deliver_time = CASE WHEN p_deliver_offset IS NULL THEN NULL ELSE SYSTIMESTAMP - TO_DSINTERVAL(p_deliver_offset) END
             WHERE order_id = v_order_id;
        END IF;
    END;
BEGIN
    SELECT user_id INTO v_user_id FROM (SELECT user_id FROM users ORDER BY user_id) WHERE ROWNUM = 1;

    SELECT COUNT(*) INTO v_count FROM delivery_persons WHERE phone = '13900000001';
    IF v_count = 0 THEN
        INSERT INTO delivery_persons (courier_id, real_name, phone, password, total_delivery_count, status, create_time)
        VALUES (seq_delivery_persons.NEXTVAL, '张晨', '13900000001', '$2a$10$7EqJtq98hPqEX7fNZaFWoO4AuWJOqGV3LE0bL9ULUypzHd2rh8a8G', 18, 'active', SYSTIMESTAMP);
    ELSE
        UPDATE delivery_persons
           SET real_name = '张晨',
               total_delivery_count = GREATEST(NVL(total_delivery_count, 0), 18),
               status = CASE WHEN status = 'inactive' THEN 'active' ELSE status END
         WHERE phone = '13900000001';
    END IF;

    SELECT courier_id INTO v_courier_id FROM delivery_persons WHERE phone = '13900000001';

    ensure_task('SMCN20260510009', '林晓雨 13810003001 广东省广州市天河区体育西路88号幸福花园3栋1201', 128.60, 'PENDING_SHIP', 'ASSIGNED', '0 00:25:00', NULL, NULL, NULL);
    ensure_task('SMCN20260510010', '何明轩 13810003002 广东省广州市越秀区东风中路268号时代广场A座902', 76.30, 'PENDING_SHIP', 'ASSIGNED', '0 00:55:00', NULL, NULL, NULL);
    ensure_task('SMCN20260510011', '苏婉婷 13810003003 广东省广州市海珠区江南西路55号丽景苑6栋302', 214.90, 'PENDING_SHIP', 'ASSIGNED', '0 01:20:00', NULL, NULL, NULL);
    ensure_task('SMCN20260510012', '周启航 13810003004 广东省广州市白云区云城西路199号云尚公寓B座510', 58.80, 'SHIPPING', 'PICKED_UP', '0 02:10:00', '0 01:40:00', NULL, NULL);
    ensure_task('SMCN20260510013', '陈思琪 13810003005 广东省广州市番禺区大学城外环西路230号学生公寓9栋', 96.50, 'SHIPPING', 'PICKED_UP', '0 03:05:00', '0 02:25:00', NULL, NULL);
    ensure_task('SMCN20260510014', '黄俊杰 13810003006 广东省佛山市南海区桂城街道灯湖西路20号保利花园', 183.20, 'PENDING_RECEIVED', 'DELIVERED', '0 05:30:00', '0 04:50:00', '0 03:20:00', NULL);
    ensure_task('SMCN20260510015', '赵雅雯 13810003007 广东省深圳市南山区科技园科苑路15号科兴科学园B2栋', 145.70, 'PENDING_RECEIVED', 'DELIVERED', '1 02:00:00', '1 01:30:00', '1 00:20:00', NULL);
    ensure_task('SMCN20260510016', '许家宁 13810003008 广东省广州市荔湾区中山八路23号富力广场5栋808', 67.40, 'SHIPPING', 'FAILED', '0 06:00:00', '0 05:10:00', NULL, '客户电话无人接听，已留言等待重新配送');

    COMMIT;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('No users found. Please run base demo data first.');
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('Chinese courier data insert error: ' || SQLERRM);
END;
/

COMMIT;

PROMPT ============================================================
PROMPT Chinese courier demo data completed.
PROMPT ============================================================
