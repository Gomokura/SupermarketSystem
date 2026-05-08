-- ============================================================
-- Supermarket Management System - Utilities Script
-- File: 03_utilities.sql (Execute after 01_schema.sql)
-- Prerequisite: 01_schema.sql must be executed first
-- Description: Utility scripts for database maintenance and extension
-- ============================================================

SET FEEDBACK OFF
SET ECHO OFF

PROMPT ============================================================
PROMPT Utilities script started...
PROMPT ============================================================

-- ============================================================
-- PART 1: Stocktake Tables (Simplified Version)
-- For modules: W-10 to W-14
-- Description: Simplified stocktake tables for actual business use
-- ============================================================

CREATE TABLE stocktake_tasks (
    task_id     NUMBER PRIMARY KEY,
    scope       VARCHAR2(20)  DEFAULT 'ALL',
    category_id NUMBER,
    status      VARCHAR2(20)  DEFAULT 'PENDING',
    creator_id  NUMBER,
    create_time DATE          DEFAULT SYSDATE,
    complete_time DATE
);
CREATE SEQUENCE seq_stocktake_tasks START WITH 1 INCREMENT BY 1 NOCACHE;
COMMENT ON TABLE stocktake_tasks IS 'Stocktake task main table';
COMMENT ON COLUMN stocktake_tasks.scope IS 'Scope: ALL or CATEGORY';
COMMENT ON COLUMN stocktake_tasks.status IS 'Status: PENDING, IN_PROGRESS, COMPLETED';

CREATE TABLE stocktake_items (
    id           NUMBER PRIMARY KEY,
    task_id      NUMBER NOT NULL,
    product_id   NUMBER NOT NULL,
    sku_id       NUMBER,
    book_stock   NUMBER NOT NULL,
    actual_stock NUMBER,
    difference   NUMBER,
    diff_reason  VARCHAR2(200),
    CONSTRAINT fk_si_task    FOREIGN KEY (task_id)  REFERENCES stocktake_tasks(task_id),
    CONSTRAINT fk_si_product FOREIGN KEY (product_id) REFERENCES products(product_id)
);
CREATE SEQUENCE seq_stocktake_items START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE INDEX idx_si_task ON stocktake_items(task_id);
COMMENT ON TABLE stocktake_items IS 'Stocktake detail items table';

-- ============================================================
-- PART 2: Database Cleanup Scripts
-- Description: Scripts for resetting the database before fresh install
-- ============================================================

-- Drop all tables and sequences (run before 01_schema.sql to reset)
/*
DECLARE
  PROCEDURE drop_if_exists(p_name VARCHAR2) IS
  BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE ' || p_name || ' CASCADE CONSTRAINTS PURGE';
  EXCEPTION WHEN OTHERS THEN NULL;
  END;
BEGIN
  drop_if_exists('STOCKTAKE_ITEMS');
  drop_if_exists('STOCKTAKE_TASKS');
  drop_if_exists('AUDIT_LOGS');
  drop_if_exists('CASHIER_RECORD_ITEMS');
  drop_if_exists('CASHIER_RECORDS');
  drop_if_exists('CASHIER_SHIFTS');
  drop_if_exists('DAMAGE_RECORDS');
  drop_if_exists('INVENTORY_CHECK_ITEMS');
  drop_if_exists('INVENTORY_CHECKS');
  drop_if_exists('PURCHASE_ORDER_ITEMS');
  drop_if_exists('PURCHASE_ORDERS');
  drop_if_exists('INVENTORY_LOGS');
  drop_if_exists('DELIVERY_TASKS');
  drop_if_exists('DELIVERY_PERSONS');
  drop_if_exists('BANNERS');
  drop_if_exists('ACTIVITY_PRODUCTS');
  drop_if_exists('ACTIVITIES');
  drop_if_exists('FULL_REDUCE_RULES');
  drop_if_exists('USER_COUPONS');
  drop_if_exists('COUPONS');
  drop_if_exists('FAVORITES');
  drop_if_exists('POINTS_LOGS');
  drop_if_exists('MESSAGES');
  drop_if_exists('REVIEWS');
  drop_if_exists('AFTER_SALES');
  drop_if_exists('ORDER_STATUS_LOGS');
  drop_if_exists('ORDER_ITEMS');
  drop_if_exists('ORDERS');
  drop_if_exists('CART');
  drop_if_exists('ADDRESSES');
  drop_if_exists('PRODUCT_IMAGES');
  drop_if_exists('PRODUCT_SKUS');
  drop_if_exists('PRODUCTS');
  drop_if_exists('SUPPLIERS');
  drop_if_exists('BRANDS');
  drop_if_exists('CATEGORIES');
  drop_if_exists('ADMIN_USERS');
  drop_if_exists('USERS');
END;
/

DECLARE
  PROCEDURE drop_seq(p_name VARCHAR2) IS
  BEGIN
    EXECUTE IMMEDIATE 'DROP SEQUENCE ' || p_name;
  EXCEPTION WHEN OTHERS THEN NULL;
  END;
BEGIN
  drop_seq('SEQ_STOCKTAKE_TASKS'); drop_seq('SEQ_STOCKTAKE_ITEMS');
  drop_seq('SEQ_USERS');          drop_seq('SEQ_ADMIN_USERS');
  drop_seq('SEQ_CATEGORIES');     drop_seq('SEQ_BRANDS');
  drop_seq('SEQ_SUPPLIERS');      drop_seq('SEQ_PRODUCTS');
  drop_seq('SEQ_PRODUCT_SKUS');  drop_seq('SEQ_PRODUCT_IMAGES');
  drop_seq('SEQ_ADDRESSES');      drop_seq('SEQ_CART');
  drop_seq('SEQ_ORDERS');         drop_seq('SEQ_ORDER_ITEMS');
  drop_seq('SEQ_ORDER_STATUS_LOGS'); drop_seq('SEQ_AFTER_SALES');
  drop_seq('SEQ_REVIEWS');        drop_seq('SEQ_MESSAGES');
  drop_seq('SEQ_POINTS_LOGS');   drop_seq('SEQ_FAVORITES');
  drop_seq('SEQ_COUPONS');        drop_seq('SEQ_USER_COUPONS');
  drop_seq('SEQ_ACTIVITIES');     drop_seq('SEQ_ACTIVITY_PRODUCTS');
  drop_seq('SEQ_FULL_REDUCE_RULES'); drop_seq('SEQ_BANNERS');
  drop_seq('SEQ_INVENTORY_LOGS'); drop_seq('SEQ_PURCHASE_ORDERS');
  drop_seq('SEQ_PO_ITEMS');       drop_seq('SEQ_INVENTORY_CHECKS');
  drop_seq('SEQ_CHECK_ITEMS');    drop_seq('SEQ_DAMAGE_RECORDS');
  drop_seq('SEQ_DELIVERY_PERSONS'); drop_seq('SEQ_DELIVERY_TASKS');
  drop_seq('SEQ_CASHIER_SHIFTS'); drop_seq('SEQ_CASHIER_RECORDS');
  drop_seq('SEQ_CASHIER_RECORD_ITEMS'); drop_seq('SEQ_AUDIT_LOGS');
END;
/

PROMPT Database has been fully cleaned. You can now run 01_schema.sql
*/

-- ============================================================
-- PART 3: Sequence Adjustment Tools
-- ============================================================

-- Reset product sequence ID after data migration
-- Uncomment to run when needed:
/*
BEGIN
  EXECUTE IMMEDIATE 'ALTER SEQUENCE seq_products INCREMENT BY ' ||
    (SELECT NVL(MAX(product_id), 1000) - 1000 FROM products);
END;
/

BEGIN
  EXECUTE IMMEDIATE 'ALTER SEQUENCE seq_users INCREMENT BY ' ||
    (SELECT NVL(MAX(user_id), 1000) - 1000 FROM users);
END;
/

BEGIN
  EXECUTE IMMEDIATE 'ALTER SEQUENCE seq_admin_users INCREMENT BY ' ||
    (SELECT NVL(MAX(admin_id), 1) - 1 FROM admin_users);
END;
/

PROMPT Sequences have been reset
*/

-- Restore soft-deleted products:
-- UPDATE products SET is_deleted = 0 WHERE is_deleted = 1;
-- COMMIT;

-- Reset coupon issued/used counts:
-- UPDATE coupons SET issued_count = 0, used_count = 0;
-- UPDATE user_coupons SET status = 'expired';
-- COMMIT;

COMMIT;

PROMPT ============================================================
PROMPT Utilities script completed!
PROMPT ============================================================
PROMPT Usage:
PROMPT   PART 1: Stocktake tables - run directly
PROMPT   PART 2: Cleanup scripts - uncomment to run
PROMPT   PART 3: Sequence tools - uncomment to run
PROMPT ============================================================