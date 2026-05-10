-- ============================================================
-- 中文收银端演示数据
-- 说明：
-- 1. 可重复执行，已存在的 POSCN 演示订单会自动跳过
-- 2. 为 cashier01 补充历史班次、历史订单、收银流水和中文明细
-- 3. 如 cashier01 没有开班中的班次，会创建一个当前开班班次
-- ============================================================

SET DEFINE OFF;

DECLARE
    v_cashier_id NUMBER;
    v_shift_id NUMBER;
    v_open_count NUMBER;
    v_demo_count NUMBER;
    v_more_count NUMBER;

    PROCEDURE add_pos_order(
        p_order_no      IN VARCHAR2,
        p_user_phone    IN VARCHAR2,
        p_pay_method    IN VARCHAR2,
        p_received      IN NUMBER,
        p_status        IN VARCHAR2,
        p_created       IN TIMESTAMP,
        p_barcode_1     IN VARCHAR2,
        p_qty_1         IN NUMBER,
        p_barcode_2     IN VARCHAR2,
        p_qty_2         IN NUMBER
    ) IS
        v_exists NUMBER;
        v_order_id NUMBER;
        v_record_id NUMBER;
        v_user_id NUMBER;
        v_receiver_snapshot VARCHAR2(500);
        v_total NUMBER(12,2) := 0;
        v_change NUMBER(12,2) := 0;
        v_product_id NUMBER;
        v_product_name VARCHAR2(100);
        v_price NUMBER(10,2);

        PROCEDURE load_product(p_barcode IN VARCHAR2, p_qty IN NUMBER) IS
        BEGIN
            IF p_barcode IS NULL OR p_qty IS NULL OR p_qty <= 0 THEN
                RETURN;
            END IF;
            SELECT product_id, product_name, price
              INTO v_product_id, v_product_name, v_price
              FROM products
             WHERE barcode = p_barcode
               AND ROWNUM = 1;
            v_total := v_total + ROUND(v_price * p_qty, 2);
        END;

        PROCEDURE insert_item(p_barcode IN VARCHAR2, p_qty IN NUMBER) IS
        BEGIN
            IF p_barcode IS NULL OR p_qty IS NULL OR p_qty <= 0 THEN
                RETURN;
            END IF;
            SELECT product_id, product_name, price
              INTO v_product_id, v_product_name, v_price
              FROM products
             WHERE barcode = p_barcode
               AND ROWNUM = 1;

            INSERT INTO order_items (
                item_id, order_id, product_id, product_name, unit_price,
                quantity, subtotal
            ) VALUES (
                seq_order_items.NEXTVAL, v_order_id, v_product_id, v_product_name, v_price,
                p_qty, ROUND(v_price * p_qty, 2)
            );

            INSERT INTO cashier_record_items (
                item_id, record_id, product_id, product_name, unit_price,
                quantity, subtotal
            ) VALUES (
                seq_cashier_record_items.NEXTVAL, v_record_id, v_product_id, v_product_name, v_price,
                p_qty, ROUND(v_price * p_qty, 2)
            );

            UPDATE products
               SET sales_count = NVL(sales_count, 0) + p_qty,
                   stock = CASE WHEN stock IS NOT NULL AND stock >= p_qty THEN stock - p_qty ELSE stock END
             WHERE product_id = v_product_id;
        END;
    BEGIN
        SELECT COUNT(*) INTO v_exists FROM orders WHERE order_no = p_order_no;
        IF v_exists > 0 THEN
            RETURN;
        END IF;

        BEGIN
            SELECT user_id,
                   NVL(nickname, NVL(real_name, username)) || ' ' || phone || ' 门店收银'
              INTO v_user_id, v_receiver_snapshot
              FROM users
             WHERE phone = p_user_phone
               AND ROWNUM = 1;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                SELECT user_id INTO v_user_id
                  FROM (SELECT user_id FROM users ORDER BY user_id)
                 WHERE ROWNUM = 1;
                v_receiver_snapshot := '散客 线下顾客 门店收银';
        END;

        load_product(p_barcode_1, p_qty_1);
        load_product(p_barcode_2, p_qty_2);
        v_change := CASE WHEN p_pay_method = 'CASH' THEN GREATEST(NVL(p_received, v_total) - v_total, 0) ELSE 0 END;

        v_order_id := seq_orders.NEXTVAL;
        INSERT INTO orders (
            order_id, order_no, user_id, source, receiver_snapshot,
            total_amount, discount_amount, coupon_discount, points_deduct_amount,
            freight_amount, pay_amount, pay_method, points_used, status,
            pay_time, complete_time, refund_time, create_time, update_time
        ) VALUES (
            v_order_id, p_order_no, v_user_id, 'CASHIER', v_receiver_snapshot,
            v_total, 0, 0, 0,
            0, v_total, p_pay_method, 0, p_status,
            p_created, CASE WHEN p_status = 'COMPLETED' THEN p_created ELSE NULL END,
            CASE WHEN p_status = 'REFUNDED' THEN p_created + NUMTODSINTERVAL(25, 'MINUTE') ELSE NULL END,
            p_created, p_created
        );

        v_record_id := seq_cashier_records.NEXTVAL;
        INSERT INTO cashier_records (
            record_id, shift_id, user_id, member_phone, total_amount, discount_amount,
            pay_amount, pay_method, received_amount, change_amount, cashier_id, create_time
        ) VALUES (
            v_record_id, v_shift_id, v_user_id, p_user_phone, v_total, 0,
            v_total, p_pay_method, p_received, v_change, v_cashier_id, p_created
        );

        insert_item(p_barcode_1, p_qty_1);
        insert_item(p_barcode_2, p_qty_2);

        INSERT INTO order_status_logs (
            log_id, order_id, from_status, to_status, operator_type, operator_id, remark, create_time
        ) VALUES (
            seq_order_status_logs.NEXTVAL, v_order_id, NULL, p_status, 'ADMIN', v_cashier_id,
            CASE WHEN p_status = 'REFUNDED' THEN '中文演示收银退款' ELSE '中文演示收银完成交易' END,
            p_created
        );

        UPDATE cashier_shifts
           SET total_order_count = NVL(total_order_count, 0) + 1,
               total_cash_amount = NVL(total_cash_amount, 0) + CASE WHEN p_pay_method = 'CASH' THEN v_total ELSE 0 END,
               total_mock_amount = NVL(total_mock_amount, 0) + CASE WHEN p_pay_method <> 'CASH' THEN v_total ELSE 0 END
         WHERE shift_id = v_shift_id;
    END;
BEGIN
    SELECT admin_id INTO v_cashier_id
      FROM admin_users
     WHERE username = 'cashier01'
       AND ROWNUM = 1;

    UPDATE users SET points = GREATEST(NVL(points, 0), 520) WHERE phone = '13800138001';
    UPDATE users SET points = GREATEST(NVL(points, 0), 1200) WHERE phone = '13800138002';
    UPDATE users SET points = GREATEST(NVL(points, 0), 300) WHERE phone = '13800138003';

    SELECT COUNT(*) INTO v_demo_count
      FROM orders
     WHERE order_no LIKE 'POSCN20260510%';

    IF v_demo_count < 4 THEN
        v_shift_id := seq_cashier_shifts.NEXTVAL;
        INSERT INTO cashier_shifts (
            shift_id, cashier_id, start_cash, end_cash, total_cash_amount,
            total_mock_amount, total_order_count, cash_diff, start_time, end_time, status
        ) VALUES (
            v_shift_id, v_cashier_id, 500, 657.50, 157.50,
            64.80, 0, 0, SYSTIMESTAMP - NUMTODSINTERVAL(1, 'DAY'),
            SYSTIMESTAMP - NUMTODSINTERVAL(20, 'HOUR'), 'CLOSED'
        );

        add_pos_order('POSCN20260510001', '13800138001', 'CASH', 50, 'COMPLETED',
            SYSTIMESTAMP - NUMTODSINTERVAL(23, 'HOUR'), '6901234500001', 4, '6901234500002', 6);
        add_pos_order('POSCN20260510002', '13800138002', 'ALIPAY', NULL, 'COMPLETED',
            SYSTIMESTAMP - NUMTODSINTERVAL(22, 'HOUR'), '6901234500005', 1, '6901234500008', 2);
        add_pos_order('POSCN20260510003', NULL, 'WECHAT', NULL, 'COMPLETED',
            SYSTIMESTAMP - NUMTODSINTERVAL(21, 'HOUR'), '6901234500006', 2, '6901234500007', 1);
        add_pos_order('POSCN20260510004', '13800138003', 'CASH', 100, 'REFUNDED',
            SYSTIMESTAMP - NUMTODSINTERVAL(20, 'HOUR'), '6901234500003', 3, '6901234500004', 5);

        UPDATE cashier_shifts
           SET end_cash = start_cash + NVL(total_cash_amount, 0),
               cash_diff = 0
         WHERE shift_id = v_shift_id;
    END IF;

    SELECT COUNT(*) INTO v_open_count
      FROM cashier_shifts
     WHERE cashier_id = v_cashier_id
       AND status = 'OPEN';

    IF v_open_count = 0 THEN
        INSERT INTO cashier_shifts (
            shift_id, cashier_id, start_cash, total_cash_amount,
            total_mock_amount, total_order_count, start_time, status
        ) VALUES (
            seq_cashier_shifts.NEXTVAL, v_cashier_id, 500, 0,
            0, 0, SYSTIMESTAMP - NUMTODSINTERVAL(40, 'MINUTE'), 'OPEN'
        );
    END IF;

    SELECT shift_id INTO v_shift_id
      FROM cashier_shifts
     WHERE cashier_id = v_cashier_id
       AND status = 'OPEN'
       AND ROWNUM = 1;

    SELECT COUNT(*) INTO v_more_count
      FROM orders
     WHERE order_no BETWEEN 'POSCN20260510005' AND 'POSCN20260510012';

    IF v_more_count < 8 THEN
        add_pos_order('POSCN20260510005', '13800138001', 'ALIPAY', NULL, 'COMPLETED',
            SYSTIMESTAMP - NUMTODSINTERVAL(115, 'MINUTE'), '6901234500001', 2, '6901234500008', 1);
        add_pos_order('POSCN20260510006', '13800138002', 'WECHAT', NULL, 'COMPLETED',
            SYSTIMESTAMP - NUMTODSINTERVAL(98, 'MINUTE'), '6901234500006', 1, '6901234500002', 4);
        add_pos_order('POSCN20260510007', NULL, 'CASH', 50, 'COMPLETED',
            SYSTIMESTAMP - NUMTODSINTERVAL(82, 'MINUTE'), '6901234500003', 2, '6901234500004', 2);
        add_pos_order('POSCN20260510008', '13800138003', 'ALIPAY', NULL, 'COMPLETED',
            SYSTIMESTAMP - NUMTODSINTERVAL(68, 'MINUTE'), '6901234500007', 1, '6901234500008', 3);
        add_pos_order('POSCN20260510009', '13800138001', 'CASH', 100, 'COMPLETED',
            SYSTIMESTAMP - NUMTODSINTERVAL(50, 'MINUTE'), '6901234500005', 2, '6901234500002', 5);
        add_pos_order('POSCN20260510010', NULL, 'WECHAT', NULL, 'COMPLETED',
            SYSTIMESTAMP - NUMTODSINTERVAL(36, 'MINUTE'), '6901234500001', 6, '6901234500003', 1);
        add_pos_order('POSCN20260510011', '13800138002', 'ALIPAY', NULL, 'COMPLETED',
            SYSTIMESTAMP - NUMTODSINTERVAL(22, 'MINUTE'), '6901234500006', 2, '6901234500007', 2);
        add_pos_order('POSCN20260510012', '13800138003', 'CASH', 80, 'COMPLETED',
            SYSTIMESTAMP - NUMTODSINTERVAL(8, 'MINUTE'), '6901234500008', 4, '6901234500004', 3);
    END IF;
END;
/

COMMIT;

PROMPT 中文收银端演示数据已写入，可登录 cashier01 / 123456 查看。

EXIT;
