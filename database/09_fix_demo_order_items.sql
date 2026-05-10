-- ============================================================
-- Supermarket Management System - Demo Order Items Fix
-- Purpose: Add missing ORDER_ITEMS for demo delivery orders shown on customer side.
-- Safe to run multiple times.
-- ============================================================

SET FEEDBACK OFF
SET ECHO OFF

PROMPT ============================================================
PROMPT Demo order item fix started...
PROMPT ============================================================

DECLARE
    v_order_id NUMBER;
    v_count NUMBER;

    PROCEDURE add_item(p_product_id IN NUMBER, p_qty IN NUMBER) IS
        v_name products.product_name%TYPE;
        v_price products.price%TYPE;
        v_cost products.cost_price%TYPE;
        v_image products.cover_image%TYPE;
    BEGIN
        SELECT product_name, price, NVL(cost_price, 0), cover_image
          INTO v_name, v_price, v_cost, v_image
          FROM products
         WHERE product_id = p_product_id;

        INSERT INTO order_items (
            item_id, order_id, product_id, product_name, product_image,
            unit_price, cost_price, quantity, subtotal
        ) VALUES (
            seq_order_items.NEXTVAL, v_order_id, p_product_id, v_name, v_image,
            v_price, v_cost, p_qty, ROUND(v_price * p_qty, 2)
        );
    END;

    PROCEDURE ensure_items(
        p_order_no IN VARCHAR2,
        p_product_1 IN NUMBER,
        p_qty_1 IN NUMBER,
        p_product_2 IN NUMBER DEFAULT NULL,
        p_qty_2 IN NUMBER DEFAULT NULL,
        p_product_3 IN NUMBER DEFAULT NULL,
        p_qty_3 IN NUMBER DEFAULT NULL
    ) IS
    BEGIN
        BEGIN
            SELECT order_id INTO v_order_id FROM orders WHERE order_no = p_order_no;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RETURN;
        END;

        SELECT COUNT(*) INTO v_count FROM order_items WHERE order_id = v_order_id;
        IF v_count > 0 THEN
            RETURN;
        END IF;

        add_item(p_product_1, p_qty_1);
        IF p_product_2 IS NOT NULL AND p_qty_2 IS NOT NULL THEN
            add_item(p_product_2, p_qty_2);
        END IF;
        IF p_product_3 IS NOT NULL AND p_qty_3 IS NOT NULL THEN
            add_item(p_product_3, p_qty_3);
        END IF;
    END;
BEGIN
    ensure_items('SMCN20260510001', 1009, 4, 1011, 6, 1014, 1);
    ensure_items('SMCN20260510002', 1005, 2, 1007, 3, 1010, 4);
    ensure_items('SMCN20260510003', 1013, 1, 1015, 2, 1022, 3);
    ensure_items('SMCN20260510004', 1011, 8, 1002, 4, 1003, 6);
    ensure_items('SMCN20260510005', 1017, 2, 1006, 2, 1016, 1);
    ensure_items('SMCN20260510006', 1020, 2, 1021, 3, 1019, 1);
    ensure_items('SMCN20260510007', 1024, 1, 1026, 6, 1023, 2);
    ensure_items('SMCN20260510008', 1018, 1, 1019, 1, 1021, 2);

    ensure_items('SMCN20260510009', 1009, 6, 1011, 8, 1002, 3);
    ensure_items('SMCN20260510010', 1005, 3, 1007, 4, 1010, 4);
    ensure_items('SMCN20260510011', 1013, 1, 1015, 2, 1016, 2);
    ensure_items('SMCN20260510012', 1011, 6, 1003, 6, 1014, 1);
    ensure_items('SMCN20260510013', 1017, 3, 1006, 2, 1022, 2);
    ensure_items('SMCN20260510014', 1020, 2, 1021, 2, 1018, 1);
    ensure_items('SMCN20260510015', 1024, 1, 1026, 6, 1027, 6);
    ensure_items('SMCN20260510016', 1018, 1, 1019, 1, 1021, 2);

    COMMIT;
END;
/

COMMIT;

PROMPT ============================================================
PROMPT Demo order item fix completed.
PROMPT ============================================================
