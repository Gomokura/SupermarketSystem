-- ============================================================
-- Supermarket Management System - Demo Data Script
-- File: 02_demo_data.sql (Execute after 01_schema.sql)
-- Prerequisite: 01_schema.sql must be executed first
-- Description: Supplementary demo data, safe to run multiple times
-- ============================================================

SET FEEDBACK OFF
SET ECHO OFF

PROMPT ============================================================
PROMPT Demo data script started...
PROMPT ============================================================

-- Clean up any existing demo data (idempotent)
-- Delete order: child tables first, parent tables last

-- 1. Delete delivery_tasks first (child of orders - no subquery to avoid FK quirks)
BEGIN
  EXECUTE IMMEDIATE 'DELETE FROM delivery_tasks';
  COMMIT;
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

-- 2. Delete after_sales (child of order_items + orders - direct delete, no subquery)
BEGIN
  EXECUTE IMMEDIATE 'DELETE FROM after_sales WHERE as_no IN (''AS202604280001'',''AS202605030001'')';
  COMMIT;
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

-- 3. Delete reviews (child of order_items + orders)
BEGIN
  EXECUTE IMMEDIATE 'DELETE FROM reviews';
  COMMIT;
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

-- 4. Delete order_items (child of orders - direct delete by known order_nos)
BEGIN
  EXECUTE IMMEDIATE 'DELETE FROM order_items WHERE order_id IN (
    SELECT order_id FROM orders WHERE order_no IN (''SM202604200001'',''SM202605010001'',''SM202605050001'')
  )';
  COMMIT;
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

-- 5. Delete order_status_logs (child of orders)
BEGIN
  EXECUTE IMMEDIATE 'DELETE FROM order_status_logs WHERE order_id IN (
    SELECT order_id FROM orders WHERE order_no IN (''SM202604200001'',''SM202605010001'',''SM202605050001'')
  )';
  COMMIT;
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

-- 6. Delete orders (parent of order_items, order_status_logs, delivery_tasks)
BEGIN
  EXECUTE IMMEDIATE 'DELETE FROM orders WHERE order_no IN (''SM202604200001'',''SM202605010001'',''SM202605050001'')';
  COMMIT;
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

-- 7. Delete addresses (child of users)
BEGIN
  EXECUTE IMMEDIATE 'DELETE FROM addresses WHERE user_id IN (SELECT user_id FROM users WHERE username LIKE ''13800%'')';
  COMMIT;
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

-- 8. Delete user_coupons (child of users)
BEGIN
  EXECUTE IMMEDIATE 'DELETE FROM user_coupons';
  COMMIT;
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

-- 9. Delete messages (child of users)
BEGIN
  EXECUTE IMMEDIATE 'DELETE FROM messages WHERE user_id IN (SELECT user_id FROM users WHERE username LIKE ''13800%'')';
  COMMIT;
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

-- 10. Delete points_logs (child of users)
BEGIN
  EXECUTE IMMEDIATE 'DELETE FROM points_logs WHERE user_id IN (SELECT user_id FROM users WHERE username LIKE ''13800%'')';
  COMMIT;
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

-- 11. Delete favorites (child of users)
BEGIN
  EXECUTE IMMEDIATE 'DELETE FROM favorites WHERE user_id IN (SELECT user_id FROM users WHERE username LIKE ''13800%'')';
  COMMIT;
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

-- 12. Delete cart (child of users)
BEGIN
  EXECUTE IMMEDIATE 'DELETE FROM cart WHERE user_id IN (SELECT user_id FROM users WHERE username LIKE ''13800%'')';
  COMMIT;
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

-- 13. Delete cashier_record_items (child of cashier_records)
BEGIN
  EXECUTE IMMEDIATE 'DELETE FROM cashier_record_items';
  COMMIT;
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

-- 14. Delete cashier_records (child of cashier_shifts)
BEGIN
  EXECUTE IMMEDIATE 'DELETE FROM cashier_records';
  COMMIT;
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

-- 15. Delete cashier_shifts (top-level cashier table)
BEGIN
  EXECUTE IMMEDIATE 'DELETE FROM cashier_shifts';
  COMMIT;
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

COMMIT;

-- ============================================================
-- 1. Delivery Addresses (for User Zhang San)
-- ============================================================
BEGIN
    FOR r IN (SELECT user_id FROM users WHERE username = '13800138001') LOOP
        INSERT INTO addresses (address_id, user_id, receiver_name, phone, province, city, district, detail, is_default)
        VALUES (seq_addresses.NEXTVAL, r.user_id, 'Zhang San', '13800138001', 'Guangdong', 'Guangzhou', 'Tianhe', 'SCNU Computer Science Dept', 1);

        INSERT INTO addresses (address_id, user_id, receiver_name, phone, province, city, district, detail, is_default)
        VALUES (seq_addresses.NEXTVAL, r.user_id, 'Zhang San', '13800138001', 'Guangdong', 'Guangzhou', 'Baiyun', 'Gaozeng Village XX', 0);

        INSERT INTO addresses (address_id, user_id, receiver_name, phone, province, city, district, detail, is_default)
        VALUES (seq_addresses.NEXTVAL, r.user_id, 'Li Si (Agent)', '13800138002', 'Guangdong', 'Foshan', 'Nanhai', 'Guicheng Community Gate Guard', 0);
    END LOOP;
END;
/

-- ============================================================
-- 2. Shopping Cart Records
-- ============================================================
DECLARE
    v_uid1 NUMBER;
    v_uid2 NUMBER;
BEGIN
    BEGIN
        SELECT user_id INTO v_uid1 FROM users WHERE username = '13800138001';
    EXCEPTION WHEN NO_DATA_FOUND THEN v_uid1 := NULL;
    END;

    BEGIN
        SELECT user_id INTO v_uid2 FROM users WHERE username = '13800138002';
    EXCEPTION WHEN NO_DATA_FOUND THEN v_uid2 := NULL;
    END;

    IF v_uid1 IS NOT NULL THEN
        INSERT INTO cart (cart_id, user_id, product_id, quantity, is_checked)
        VALUES (seq_cart.NEXTVAL, v_uid1, 1000, 2, 1);

        INSERT INTO cart (cart_id, user_id, product_id, quantity, is_checked)
        VALUES (seq_cart.NEXTVAL, v_uid1, 1001, 3, 1);

        INSERT INTO cart (cart_id, user_id, product_id, quantity, is_checked)
        VALUES (seq_cart.NEXTVAL, v_uid1, 1002, 1, 1);

        INSERT INTO cart (cart_id, user_id, product_id, quantity, is_checked)
        VALUES (seq_cart.NEXTVAL, v_uid1, 1006, 1, 0);
    END IF;

    IF v_uid2 IS NOT NULL THEN
        INSERT INTO cart (cart_id, user_id, product_id, quantity, is_checked)
        VALUES (seq_cart.NEXTVAL, v_uid2, 1007, 2, 1);
    END IF;
END;
/

-- ============================================================
-- 3. Order Flow (Order 1: Completed Order with Review)
-- ============================================================
DECLARE
    v_user_id NUMBER;
    v_addr_id NUMBER;
    v_order_id NUMBER;
    v_coupon_id NUMBER;
BEGIN
    BEGIN
        SELECT user_id INTO v_user_id FROM users WHERE username = '13800138001';
    EXCEPTION WHEN NO_DATA_FOUND THEN
        raise_application_error(-20001, 'User 13800138001 not found');
    END;

    BEGIN
        SELECT address_id INTO v_addr_id FROM addresses WHERE user_id = v_user_id AND is_default = 1 AND ROWNUM = 1;
    EXCEPTION WHEN NO_DATA_FOUND THEN
        raise_application_error(-20001, 'Default address for user not found');
    END;

    BEGIN
        SELECT coupon_id INTO v_coupon_id FROM coupons WHERE coupon_name = 'NewUser 50-10' AND ROWNUM = 1;
    EXCEPTION WHEN NO_DATA_FOUND THEN v_coupon_id := NULL;
    END;

    INSERT INTO orders (
        order_id, order_no, user_id, source, address_id, receiver_snapshot,
        total_amount, discount_amount, coupon_discount, points_deduct_amount,
        freight_amount, pay_amount, pay_method, coupon_id, uc_id, points_used,
        delivery_time_slot, remark, status, pay_time, ship_time, pickup_time, deliver_time, confirm_time, complete_time
    ) VALUES (
        seq_orders.NEXTVAL, 'SM202604200001', v_user_id, 'ONLINE', v_addr_id,
        'Zhang San 13800138001 Guangdong Guangzhou Tianhe SCNU',
        15.00, 10.00, 10.00, 0, 0.00, 5.00, 'MOCK', v_coupon_id, NULL, 0,
        'Tomorrow AM 9:00-12:00', 'Do not use locker please', 'COMPLETED',
        SYSTIMESTAMP - INTERVAL '30' DAY,
        SYSTIMESTAMP - INTERVAL '30' DAY + INTERVAL '1' HOUR,
        SYSTIMESTAMP - INTERVAL '29' DAY + INTERVAL '2' HOUR,
        SYSTIMESTAMP - INTERVAL '29' DAY + INTERVAL '3' HOUR,
        SYSTIMESTAMP - INTERVAL '28' DAY,
        SYSTIMESTAMP - INTERVAL '28' DAY
    );
    v_order_id := seq_orders.CURRVAL;

    IF v_coupon_id IS NOT NULL THEN
        INSERT INTO user_coupons (uc_id, user_id, coupon_id, status, get_time, use_time, order_id)
        VALUES (seq_user_coupons.NEXTVAL, v_user_id, v_coupon_id, 'used',
                SYSTIMESTAMP - INTERVAL '30' DAY, SYSTIMESTAMP - INTERVAL '30' DAY, v_order_id);
    END IF;

    UPDATE orders SET total_amount = 17.50, coupon_discount = 10.00, pay_amount = 7.50
    WHERE order_id = v_order_id;

    INSERT INTO order_items (item_id, order_id, product_id, product_name, unit_price, cost_price, quantity, subtotal)
    VALUES (seq_order_items.NEXTVAL, v_order_id, 1000, 'Coca-Cola 330ml', 3.50, 2.00, 2, 7.00);

    INSERT INTO order_items (item_id, order_id, product_id, product_name, unit_price, cost_price, quantity, subtotal)
    VALUES (seq_order_items.NEXTVAL, v_order_id, 1001, 'Nongfu Spring 550ml', 2.00, 1.00, 3, 6.00);

    INSERT INTO order_items (item_id, order_id, product_id, product_name, unit_price, cost_price, quantity, subtotal)
    VALUES (seq_order_items.NEXTVAL, v_order_id, 1002, 'Master Kong Instant Noodles', 4.50, 2.50, 1, 4.50);

    INSERT INTO order_status_logs (log_id, order_id, from_status, to_status, operator_type, operator_id, operator_name, remark)
    VALUES (seq_order_status_logs.NEXTVAL, v_order_id, NULL, 'PENDING_PAY', 'SYSTEM', NULL, 'System', 'Order created');

    INSERT INTO order_status_logs (log_id, order_id, from_status, to_status, operator_type, operator_id, operator_name, remark)
    VALUES (seq_order_status_logs.NEXTVAL, v_order_id, 'PENDING_PAY', 'PAID', 'USER', v_user_id, 'Zhang San', 'Payment success');

    INSERT INTO order_status_logs (log_id, order_id, from_status, to_status, operator_type, operator_id, operator_name, remark)
    VALUES (seq_order_status_logs.NEXTVAL, v_order_id, 'PAID', 'PENDING_SHIP', 'SYSTEM', NULL, 'System', 'Waiting to ship');

    INSERT INTO order_status_logs (log_id, order_id, from_status, to_status, operator_type, operator_id, operator_name, remark)
    VALUES (seq_order_status_logs.NEXTVAL, v_order_id, 'PENDING_SHIP', 'SHIPPING', 'ADMIN', 2, 'Manager Zhang', 'Shipped');

    INSERT INTO order_status_logs (log_id, order_id, from_status, to_status, operator_type, operator_id, operator_name, remark)
    VALUES (seq_order_status_logs.NEXTVAL, v_order_id, 'SHIPPING', 'COMPLETED', 'USER', v_user_id, 'Zhang San', 'Order received');
END;
/

-- ============================================================
-- 4. Order 2: PAID (Waiting to Ship)
-- ============================================================
DECLARE
    v_user_id NUMBER;
    v_addr_id NUMBER;
    v_order_id NUMBER;
BEGIN
    BEGIN
        SELECT user_id INTO v_user_id FROM users WHERE username = '13800138002';
    EXCEPTION WHEN NO_DATA_FOUND THEN
        raise_application_error(-20001, 'User 13800138002 not found');
    END;

    BEGIN
        SELECT address_id INTO v_addr_id FROM addresses WHERE user_id = v_user_id AND ROWNUM = 1;
    EXCEPTION WHEN NO_DATA_FOUND THEN v_addr_id := NULL;
    END;

    INSERT INTO orders (
        order_id, order_no, user_id, source, address_id, receiver_snapshot,
        total_amount, discount_amount, pay_amount, pay_method,
        delivery_time_slot, status, pay_time
    ) VALUES (
        seq_orders.NEXTVAL, 'SM202605010001', v_user_id, 'ONLINE', v_addr_id,
        'Li Si 13800138002 Guangdong Guangzhou Tianhe SCNU South Gate',
        62.80, 0, 62.80, 'MOCK',
        'Tomorrow PM 14:00-18:00', 'PAID',
        SYSTIMESTAMP - INTERVAL '1' DAY
    );
    v_order_id := seq_orders.CURRVAL;

    INSERT INTO order_items (item_id, order_id, product_id, product_name, unit_price, cost_price, quantity, subtotal)
    VALUES (seq_order_items.NEXTVAL, v_order_id, 1005, 'Head Shoulders Shampoo 400ml', 29.90, 15.00, 1, 29.90);

    INSERT INTO order_items (item_id, order_id, product_id, product_name, unit_price, cost_price, quantity, subtotal)
    VALUES (seq_order_items.NEXTVAL, v_order_id, 1006, 'Colgate Toothpaste 120g', 12.00, 6.00, 1, 12.00);

    INSERT INTO order_items (item_id, order_id, product_id, product_name, unit_price, cost_price, quantity, subtotal)
    VALUES (seq_order_items.NEXTVAL, v_order_id, 1007, 'Lays Chips Original 104g', 6.50, 3.00, 3, 19.50);

    UPDATE users SET points = points + FLOOR(62.80 * 10) WHERE user_id = v_user_id;

    INSERT INTO points_logs (log_id, user_id, change_amount, balance_after, reason, ref_id)
    VALUES (seq_points_logs.NEXTVAL, v_user_id, FLOOR(62.80 * 10),
            (SELECT points FROM users WHERE user_id = v_user_id),
            'ORDER_REWARD', v_order_id);
END;
/

-- ============================================================
-- 5. Order 3: SHIPPING
-- ============================================================
DECLARE
    v_user_id NUMBER;
    v_addr_id NUMBER;
    v_order_id NUMBER;
BEGIN
    BEGIN
        SELECT user_id INTO v_user_id FROM users WHERE username = '13800138003';
    EXCEPTION WHEN NO_DATA_FOUND THEN
        raise_application_error(-20001, 'User 13800138003 not found');
    END;

    BEGIN
        SELECT address_id INTO v_addr_id FROM addresses WHERE user_id = v_user_id AND ROWNUM = 1;
    EXCEPTION WHEN NO_DATA_FOUND THEN v_addr_id := NULL;
    END;

    INSERT INTO orders (
        order_id, order_no, user_id, source, address_id, receiver_snapshot,
        total_amount, discount_amount, pay_amount, pay_method,
        status, pay_time, ship_time
    ) VALUES (
        seq_orders.NEXTVAL, 'SM202605050001', v_user_id, 'ONLINE', v_addr_id,
        'Wang Wu 13800138003 Guangdong Foshan Nanhai Guicheng Community',
        16.90, 0, 16.90, 'MOCK',
        'SHIPPING',
        SYSTIMESTAMP - INTERVAL '2' HOUR,
        SYSTIMESTAMP - INTERVAL '1' HOUR
    );
    v_order_id := seq_orders.CURRVAL;

    INSERT INTO order_items (item_id, order_id, product_id, product_name, unit_price, cost_price, quantity, subtotal)
    VALUES (seq_order_items.NEXTVAL, v_order_id, 1007, 'Oreo Biscuits 388g', 16.90, 8.00, 1, 16.90);

    INSERT INTO delivery_tasks (task_id, order_id, courier_id, status, assign_time, pickup_time)
    VALUES (seq_delivery_tasks.NEXTVAL, v_order_id, 1, 'PICKED_UP',
            SYSTIMESTAMP - INTERVAL '1' HOUR, SYSTIMESTAMP - INTERVAL '30' MINUTE);

    UPDATE delivery_persons SET total_delivery_count = total_delivery_count + 1 WHERE courier_id = 1;
END;
/

-- ============================================================
-- 6. After-Sales Records
-- ============================================================
DECLARE
    v_user_id1 NUMBER;
    v_user_id2 NUMBER;
    v_order_id1 NUMBER;
    v_order_id2 NUMBER;
    v_item_id1 NUMBER;
    v_item_id2 NUMBER;
BEGIN
    BEGIN
        SELECT user_id INTO v_user_id1 FROM users WHERE username = '13800138001';
    EXCEPTION WHEN NO_DATA_FOUND THEN v_user_id1 := NULL;
    END;

    BEGIN
        SELECT order_id INTO v_order_id1 FROM orders WHERE order_no = 'SM202604200001' AND ROWNUM = 1;
    EXCEPTION WHEN NO_DATA_FOUND THEN v_order_id1 := NULL;
    END;

    IF v_order_id1 IS NOT NULL THEN
        BEGIN
            SELECT item_id INTO v_item_id1 FROM order_items WHERE order_id = v_order_id1 AND ROWNUM = 1;
        EXCEPTION WHEN NO_DATA_FOUND THEN v_item_id1 := NULL;
        END;

        IF v_item_id1 IS NOT NULL AND v_user_id1 IS NOT NULL THEN
            INSERT INTO after_sales (
                as_id, as_no, order_id, item_id, user_id, as_type, reason,
                refund_amount, status, handler_id, admin_remark, handle_time
            ) VALUES (
                seq_after_sales.NEXTVAL, 'AS202604280001', v_order_id1, v_item_id1,
                v_user_id1, 'REFUND',
                'Package damaged upon arrival, requesting refund',
                3.50, 'COMPLETED', 6, 'Refunded to original payment account', SYSTIMESTAMP - INTERVAL '22' DAY
            );
        END IF;
    END IF;

    BEGIN
        SELECT order_id INTO v_order_id2 FROM orders WHERE order_no = 'SM202605010001' AND ROWNUM = 1;
    EXCEPTION WHEN NO_DATA_FOUND THEN v_order_id2 := NULL;
    END;

    BEGIN
        SELECT user_id INTO v_user_id2 FROM users WHERE username = '13800138002';
    EXCEPTION WHEN NO_DATA_FOUND THEN v_user_id2 := NULL;
    END;

    IF v_order_id2 IS NOT NULL AND v_user_id2 IS NOT NULL THEN
        BEGIN
            SELECT item_id INTO v_item_id2 FROM order_items WHERE order_id = v_order_id2 AND product_id = 1005 AND ROWNUM = 1;
        EXCEPTION WHEN NO_DATA_FOUND THEN v_item_id2 := NULL;
        END;

        IF v_item_id2 IS NOT NULL THEN
            INSERT INTO after_sales (
                as_id, as_no, order_id, item_id, user_id, as_type, reason,
                refund_amount, status, create_time
            ) VALUES (
                seq_after_sales.NEXTVAL, 'AS202605030001', v_order_id2, v_item_id2,
                v_user_id2, 'REFUND',
                'Product does not match description, requesting refund',
                29.90, 'PENDING', SYSTIMESTAMP - INTERVAL '2' DAY
            );
        END IF;
    END IF;
END;
/

-- ============================================================
-- 7. Product Reviews
-- ============================================================
DECLARE
    v_user_id NUMBER;
    v_order_id NUMBER;
    v_item_id1 NUMBER;
    v_item_id2 NUMBER;
BEGIN
    BEGIN
        SELECT user_id INTO v_user_id FROM users WHERE username = '13800138001';
    EXCEPTION WHEN NO_DATA_FOUND THEN v_user_id := NULL;
    END;

    BEGIN
        SELECT order_id INTO v_order_id FROM orders WHERE order_no = 'SM202604200001' AND ROWNUM = 1;
    EXCEPTION WHEN NO_DATA_FOUND THEN v_order_id := NULL;
    END;

    IF v_order_id IS NOT NULL THEN
        BEGIN
            SELECT item_id INTO v_item_id1 FROM order_items WHERE order_id = v_order_id AND ROWNUM = 1;
        EXCEPTION WHEN NO_DATA_FOUND THEN v_item_id1 := NULL;
        END;

        BEGIN
            SELECT item_id INTO v_item_id2 FROM order_items WHERE order_id = v_order_id AND product_id = 1001 AND ROWNUM = 1;
        EXCEPTION WHEN NO_DATA_FOUND THEN v_item_id2 := NULL;
        END;
    END IF;

    IF v_item_id1 IS NOT NULL AND v_user_id IS NOT NULL THEN
        INSERT INTO reviews (
            review_id, order_id, order_item_id, product_id, user_id, rating, content, tags,
            is_anonymous, reply, reply_time
        ) VALUES (
            seq_reviews.NEXTVAL, v_order_id, v_item_id1, 1000, v_user_id, 5,
            'Very refreshing, even better chilled. Fast delivery too, will repurchase',
            'Tasty,FastDelivery,ValueForMoney',
            0,
            'Thank you for your support, happy shopping', SYSTIMESTAMP - INTERVAL '27' DAY
        );
    END IF;

    IF v_item_id2 IS NOT NULL AND v_user_id IS NOT NULL THEN
        INSERT INTO reviews (
            review_id, order_id, order_item_id, product_id, user_id, rating, content, tags, is_anonymous
        ) VALUES (
            seq_reviews.NEXTVAL, v_order_id, v_item_id2, 1001, v_user_id, 4,
            'Clean taste, sweet and refreshing, great for daily drinking. A bit pricey though',
            'PureTaste,GoodQuality,SlightlyExpensive',
            0
        );
    END IF;

    UPDATE products SET avg_rating = 4.8, review_count = review_count + 2 WHERE product_id = 1000;
    UPDATE products SET avg_rating = 4.9, review_count = review_count + 1 WHERE product_id = 1001;
END;
/

-- ============================================================
-- 8. Points Records
-- ============================================================
DECLARE
    v_user_id NUMBER;
BEGIN
    FOR r IN (SELECT user_id FROM users WHERE username = '13800138003') LOOP
        INSERT INTO points_logs (log_id, user_id, change_amount, balance_after, reason)
        VALUES (seq_points_logs.NEXTVAL, r.user_id, 100, 100, 'REGISTER_GIFT');
    END LOOP;

    FOR r IN (SELECT user_id FROM users WHERE username = '13800138001') LOOP
        INSERT INTO points_logs (log_id, user_id, change_amount, balance_after, reason, operator_id)
        VALUES (seq_points_logs.NEXTVAL, r.user_id, -100, 420, 'ADMIN_ADJUST', 2);
    END LOOP;
END;
/

-- ============================================================
-- 9. System Messages
-- ============================================================
DECLARE
    v_order_id NUMBER;
BEGIN
    BEGIN
        SELECT order_id INTO v_order_id FROM orders WHERE order_no = 'SM202604200001' AND ROWNUM = 1;
    EXCEPTION WHEN NO_DATA_FOUND THEN v_order_id := NULL;
    END;

    FOR r IN (SELECT user_id FROM users WHERE username = '13800138001') LOOP
        INSERT INTO messages (message_id, user_id, title, content, msg_type, ref_id)
        VALUES (seq_messages.NEXTVAL, r.user_id,
                'Your order has been shipped',
                'Your order SM202604200001 has been shipped, expected tomorrow. Please keep phone accessible',
                'ORDER', v_order_id);

        INSERT INTO messages (message_id, user_id, title, content, msg_type)
        VALUES (seq_messages.NEXTVAL, r.user_id,
                'Coupon expiring soon',
                'Your 10% off coupon expires in 3 days. Use it before it expires',
                'COUPON');

        INSERT INTO messages (message_id, user_id, title, content, msg_type)
        VALUES (seq_messages.NEXTVAL, r.user_id,
                'Your points have been adjusted',
                'Your points have been adjusted: -100 points, reason: admin manual adjustment',
                'SYSTEM');
    END LOOP;
END;
/

-- ============================================================
-- 10. Favorites
-- ============================================================
DECLARE
    v_uid1 NUMBER;
    v_uid2 NUMBER;
BEGIN
    BEGIN
        SELECT user_id INTO v_uid1 FROM users WHERE username = '13800138001';
    EXCEPTION WHEN NO_DATA_FOUND THEN v_uid1 := NULL;
    END;

    BEGIN
        SELECT user_id INTO v_uid2 FROM users WHERE username = '13800138002';
    EXCEPTION WHEN NO_DATA_FOUND THEN v_uid2 := NULL;
    END;

    IF v_uid1 IS NOT NULL THEN
        INSERT INTO favorites (fav_id, user_id, product_id)
        VALUES (seq_favorites.NEXTVAL, v_uid1, 1005);

        INSERT INTO favorites (fav_id, user_id, product_id)
        VALUES (seq_favorites.NEXTVAL, v_uid1, 1006);

        INSERT INTO favorites (fav_id, user_id, product_id)
        VALUES (seq_favorites.NEXTVAL, v_uid1, 1007);
    END IF;

    IF v_uid2 IS NOT NULL THEN
        INSERT INTO favorites (fav_id, user_id, product_id)
        VALUES (seq_favorites.NEXTVAL, v_uid2, 1000);
    END IF;
END;
/

-- ============================================================
-- 11. Inventory Logs
-- ============================================================
BEGIN
    INSERT INTO inventory_logs (log_id, product_id, change_amount, balance_after, log_type, ref_id, remark, operator_id)
    VALUES (seq_inventory_logs.NEXTVAL, 1000, 500, 500, 'PURCHASE_IN', NULL, 'Initial purchase stock in', 4);

    INSERT INTO inventory_logs (log_id, product_id, change_amount, balance_after, log_type, ref_id, remark, operator_id)
    VALUES (seq_inventory_logs.NEXTVAL, 1001, 1000, 1000, 'PURCHASE_IN', NULL, 'Initial purchase stock in', 4);

    UPDATE products SET stock = stock - 2 WHERE product_id = 1000;
    INSERT INTO inventory_logs (log_id, product_id, change_amount, balance_after, log_type, ref_id, remark)
    VALUES (seq_inventory_logs.NEXTVAL, 1000, -2, 498, 'ORDER_OUT', NULL, 'Order SM202604200001 stock out');

    UPDATE products SET stock = stock - 3 WHERE product_id = 1001;
    INSERT INTO inventory_logs (log_id, product_id, change_amount, balance_after, log_type, ref_id, remark)
    VALUES (seq_inventory_logs.NEXTVAL, 1001, -3, 997, 'ORDER_OUT', NULL, 'Order SM202604200001 stock out');

    UPDATE products SET stock = stock - 5 WHERE product_id = 1002;
    INSERT INTO inventory_logs (log_id, product_id, change_amount, balance_after, log_type, ref_id, remark, operator_id)
    VALUES (seq_inventory_logs.NEXTVAL, 1002, -5, 295, 'DAMAGE', NULL, 'Some products expired and damaged', 4);
END;
/

-- ============================================================
-- 12. Delivery Tasks
-- ============================================================
BEGIN
    NULL;
END;
/

-- ============================================================
-- 13. Cashier Records
-- ============================================================
DECLARE
    v_shift_id NUMBER;
    v_cashier_id NUMBER;
    v_record1_id NUMBER;
    v_member_id NUMBER;
    v_record2_id NUMBER;
BEGIN
    BEGIN
        SELECT admin_id INTO v_cashier_id FROM admin_users WHERE username = 'cashier01';
    EXCEPTION WHEN NO_DATA_FOUND THEN
        raise_application_error(-20001, 'Cashier admin user not found');
    END;

    INSERT INTO cashier_shifts (shift_id, cashier_id, start_cash, start_time, status)
    VALUES (seq_cashier_shifts.NEXTVAL, v_cashier_id, 500.00, SYSTIMESTAMP - INTERVAL '4' HOUR, 'OPEN');
    v_shift_id := seq_cashier_shifts.CURRVAL;

    INSERT INTO cashier_records (
        record_id, shift_id, total_amount, discount_amount, pay_amount, pay_method,
        received_amount, change_amount, cashier_id
    ) VALUES (
        seq_cashier_records.NEXTVAL, v_shift_id, 23.40, 0, 23.40, 'CASH',
        25.00, 1.60, v_cashier_id
    );
    v_record1_id := seq_cashier_records.CURRVAL;

    INSERT INTO cashier_record_items (item_id, record_id, product_id, product_name, unit_price, quantity, subtotal)
    VALUES (seq_cashier_record_items.NEXTVAL, v_record1_id, 1000, 'Coca-Cola 330ml', 3.50, 2, 7.00);

    INSERT INTO cashier_record_items (item_id, record_id, product_id, product_name, unit_price, quantity, subtotal)
    VALUES (seq_cashier_record_items.NEXTVAL, v_record1_id, 1001, 'Nongfu Spring 550ml', 2.00, 3, 6.00);

    INSERT INTO cashier_record_items (item_id, record_id, product_id, product_name, unit_price, quantity, subtotal)
    VALUES (seq_cashier_record_items.NEXTVAL, v_record1_id, 1007, 'Lays Chips Original 104g', 6.50, 1, 6.50);

    INSERT INTO cashier_record_items (item_id, record_id, product_id, product_name, unit_price, quantity, subtotal)
    VALUES (seq_cashier_record_items.NEXTVAL, v_record1_id, 1002, 'Master Kong Instant Noodles', 4.50, 1, 3.90);

    BEGIN
        SELECT user_id INTO v_member_id FROM users WHERE username = '13800138001';
    EXCEPTION WHEN NO_DATA_FOUND THEN v_member_id := NULL;
    END;

    IF v_member_id IS NOT NULL THEN
        INSERT INTO cashier_records (
            record_id, shift_id, user_id, member_phone, total_amount, pay_amount, pay_method,
            received_amount, change_amount, cashier_id
        ) VALUES (
            seq_cashier_records.NEXTVAL, v_shift_id, v_member_id, '13800138001', 12.00, 12.00, 'CASH',
            20.00, 8.00, v_cashier_id
        );
        v_record2_id := seq_cashier_records.CURRVAL;

        INSERT INTO cashier_record_items (item_id, record_id, product_id, product_name, unit_price, quantity, subtotal)
        VALUES (seq_cashier_record_items.NEXTVAL, v_record2_id, 1006, 'Colgate Toothpaste 120g', 12.00, 1, 12.00);
    END IF;

    UPDATE cashier_shifts SET
        total_cash_amount = total_cash_amount + 23.40 + 12.00,
        total_order_count = total_order_count + 2
    WHERE shift_id = v_shift_id;
END;
/

COMMIT;

PROMPT ============================================================
PROMPT Demo data script completed!
PROMPT Contents:
PROMPT   - 3 delivery addresses
PROMPT   - 5 cart records
PROMPT   - 3 orders (Completed/Paid/Shipping)
PROMPT   - 2 after-sales records
PROMPT   - 2 product reviews
PROMPT   - Points change records
PROMPT   - System messages
PROMPT   - 5 favorites
PROMPT   - Inventory logs
PROMPT   - 1 delivery task
PROMPT   - 2 cashier records
PROMPT ============================================================
