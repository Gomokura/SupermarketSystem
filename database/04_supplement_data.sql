-- ============================================================
-- Supermarket Management System - Supplementary Test Data
-- File: 04_supplement_data.sql
-- Purpose: Add more test data to enrich tables with few records
-- ============================================================

SET FEEDBACK OFF
SET ECHO OFF

PROMPT ============================================================
PROMPT Supplementary data script started...
PROMPT ============================================================

-- ============================================================
-- 1. Add More Suppliers (to Suppliers table)
-- ============================================================
BEGIN
    -- Supplier 1
    INSERT INTO suppliers (supplier_id, supplier_name, contact, phone, email, address, payment_period, status)
    VALUES (seq_suppliers.NEXTVAL, 'Coca-Cola Bottling Co.', 'Mr. Wang', '020-38888888', 'supplier@cocacola.cn', 'No.100 South Ring Road, Guangzhou', 30, 'active');
    
    -- Supplier 2
    INSERT INTO suppliers (supplier_id, supplier_name, contact, phone, email, address, payment_period, status)
    VALUES (seq_suppliers.NEXTVAL, 'Nestlé China', 'Ms. Zhang', '021-68888888', 'sales@nestle.cn', 'No.500 Huaihai Road, Shanghai', 45, 'active');
    
    -- Supplier 3
    INSERT INTO suppliers (supplier_id, supplier_name, contact, phone, email, address, payment_period, status)
    VALUES (seq_suppliers.NEXTVAL, 'Procter & Gamble', 'Mr. Li', '010-58888888', 'business@pg.cn', 'No.1 Chang An Street, Beijing', 60, 'active');
    
    -- Supplier 4
    INSERT INTO suppliers (supplier_id, supplier_name, contact, phone, email, address, payment_period, status)
    VALUES (seq_suppliers.NEXTVAL, 'Danone Dairy Group', 'Ms. Liu', '0571-88888888', 'supply@danone.cn', 'No.200 Hangzhou Avenue, Hangzhou', 35, 'active');
    
    -- Supplier 5
    INSERT INTO suppliers (supplier_id, supplier_name, contact, phone, email, address, payment_period, status)
    VALUES (seq_suppliers.NEXTVAL, 'Unilever Foods', 'Mr. Chen', '0755-88888888', 'vendor@unilever.cn', 'No.300 Shennan Road, Shenzhen', 30, 'active');
    
    COMMIT;
EXCEPTION WHEN OTHERS THEN
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('Suppliers insert error: ' || SQLERRM);
END;
/

-- ============================================================
-- 2. Add More Brands (to Brands table)
-- ============================================================
BEGIN
    INSERT INTO brands (brand_id, brand_name, logo_url, description, sort_order, status)
    VALUES (seq_brands.NEXTVAL, 'Coca-Cola', 'https://via.placeholder.com/150?text=Coca-Cola', 'The World''s Leading Beverage Brand', 1, 'active');
    
    INSERT INTO brands (brand_id, brand_name, logo_url, description, sort_order, status)
    VALUES (seq_brands.NEXTVAL, 'Sprite', 'https://via.placeholder.com/150?text=Sprite', 'Lemon-Lime Flavored Soda', 2, 'active');
    
    INSERT INTO brands (brand_id, brand_name, logo_url, description, sort_order, status)
    VALUES (seq_brands.NEXTVAL, 'Fanta', 'https://via.placeholder.com/150?text=Fanta', 'Colorful Fruity Soft Drinks', 3, 'active');
    
    INSERT INTO brands (brand_id, brand_name, logo_url, description, sort_order, status)
    VALUES (seq_brands.NEXTVAL, 'Pepsi', 'https://via.placeholder.com/150?text=Pepsi', 'American Cola Brand', 4, 'active');
    
    INSERT INTO brands (brand_id, brand_name, logo_url, description, sort_order, status)
    VALUES (seq_brands.NEXTVAL, 'Head Shoulders', 'https://via.placeholder.com/150?text=HeadShoulders', 'Premium Shampoo Brand', 5, 'active');
    
    INSERT INTO brands (brand_id, brand_name, logo_url, description, sort_order, status)
    VALUES (seq_brands.NEXTVAL, 'Colgate', 'https://via.placeholder.com/150?text=Colgate', 'Leading Toothpaste Brand', 6, 'active');
    
    COMMIT;
EXCEPTION WHEN OTHERS THEN
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('Brands insert error: ' || SQLERRM);
END;
/

-- ============================================================
-- 3. Add More Banners (to Banners table)
-- ============================================================
BEGIN
    INSERT INTO banners (banner_id, image_url, link_type, link_target, sort_order, status)
    VALUES (seq_banners.NEXTVAL, 'https://via.placeholder.com/1920x600?text=Summer+Sale', 'CATEGORY', '1', 1, 'active');
    
    INSERT INTO banners (banner_id, image_url, link_type, link_target, sort_order, status)
    VALUES (seq_banners.NEXTVAL, 'https://via.placeholder.com/1920x600?text=Flash+Deals', 'ACTIVITY', '1', 2, 'active');
    
    INSERT INTO banners (banner_id, image_url, link_type, link_target, sort_order, status)
    VALUES (seq_banners.NEXTVAL, 'https://via.placeholder.com/1920x600?text=New+Arrivals', 'NONE', '', 3, 'active');
    
    INSERT INTO banners (banner_id, image_url, link_type, link_target, sort_order, status)
    VALUES (seq_banners.NEXTVAL, 'https://via.placeholder.com/1920x600?text=Members+Only', 'ACTIVITY', '2', 4, 'active');
    
    COMMIT;
EXCEPTION WHEN OTHERS THEN
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('Banners insert error: ' || SQLERRM);
END;
/

-- ============================================================
-- 4. Add More Coupons (to Coupons table)
-- ============================================================
BEGIN
    INSERT INTO coupons (coupon_id, coupon_name, coupon_code, coupon_type, discount_value, min_purchase_amount, max_discount_amount, 
                         valid_start_date, valid_end_date, max_issue_count, issued_count, used_count, status)
    VALUES (seq_coupons.NEXTVAL, 'Supermarket Anniversary 100-15', 'ANNIV2024', 'FIXED', 15.00, 100.00, 50.00, 
            TRUNC(SYSDATE), TRUNC(SYSDATE) + 30, 500, 250, 120, 'active');
    
    INSERT INTO coupons (coupon_id, coupon_name, coupon_code, coupon_type, discount_value, min_purchase_amount, max_discount_amount, 
                         valid_start_date, valid_end_date, max_issue_count, issued_count, used_count, status)
    VALUES (seq_coupons.NEXTVAL, 'Member Exclusive 50-5', 'MEMBER2024', 'FIXED', 5.00, 50.00, 10.00, 
            TRUNC(SYSDATE), TRUNC(SYSDATE) + 60, 1000, 300, 150, 'active');
    
    INSERT INTO coupons (coupon_id, coupon_name, coupon_code, coupon_type, discount_value, min_purchase_amount, max_discount_amount, 
                         valid_start_date, valid_end_date, max_issue_count, issued_count, used_count, status)
    VALUES (seq_coupons.NEXTVAL, 'Summer Beverages 20% OFF', 'SUMMER2024', 'PERCENTAGE', 20.00, 30.00, 20.00, 
            TRUNC(SYSDATE), TRUNC(SYSDATE) + 45, 300, 180, 80, 'active');
    
    INSERT INTO coupons (coupon_id, coupon_name, coupon_code, coupon_type, discount_value, min_purchase_amount, max_discount_amount, 
                         valid_start_date, valid_end_date, max_issue_count, issued_count, used_count, status)
    VALUES (seq_coupons.NEXTVAL, 'Household Products 200-30', 'HOME2024', 'FIXED', 30.00, 200.00, 50.00, 
            TRUNC(SYSDATE), TRUNC(SYSDATE) + 90, 200, 100, 45, 'active');
    
    COMMIT;
EXCEPTION WHEN OTHERS THEN
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('Coupons insert error: ' || SQLERRM);
END;
/

-- ============================================================
-- 5. Add More Promotions/Activities (to Promotions table)
-- ============================================================
BEGIN
    INSERT INTO promotions (activity_id, activity_name, description, activity_type, discount_value, min_purchase_amount, valid_start_date, valid_end_date, status)
    VALUES (seq_promotions.NEXTVAL, 'Buy 2 Get 1 Free Beverages', 'Beverages promotion', 'FULL_DISCOUNT', 100.00, 0.00, TRUNC(SYSDATE), TRUNC(SYSDATE) + 30, 'active');
    
    INSERT INTO promotions (activity_id, activity_name, description, activity_type, discount_value, min_purchase_amount, valid_start_date, valid_end_date, status)
    VALUES (seq_promotions.NEXTVAL, '10% OFF All Daily Essentials', 'Household items discount', 'PERCENTAGE', 10.00, 0.00, TRUNC(SYSDATE), TRUNC(SYSDATE) + 45, 'active');
    
    INSERT INTO promotions (activity_id, activity_name, description, activity_type, discount_value, min_purchase_amount, valid_start_date, valid_end_date, status)
    VALUES (seq_promotions.NEXTVAL, 'Fresh Produce Weekend Deal', 'Fresh items promotion', 'PERCENTAGE', 15.00, 30.00, TRUNC(SYSDATE), TRUNC(SYSDATE) + 14, 'active');
    
    COMMIT;
EXCEPTION WHEN OTHERS THEN
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('Promotions insert error: ' || SQLERRM);
END;
/

-- ============================================================
-- 6. Add More Admin Accounts (if needed)
-- ============================================================
BEGIN
    INSERT INTO admins (admin_id, username, password_hash, real_name, phone, role, status, last_login)
    VALUES (seq_admins.NEXTVAL, 'admin_product', '$2a$10$abcdefghijklmnopqrstuvwxyz1234567890', 'Product Manager Li', '13900001111', 'PRODUCT_MANAGER', 'active', SYSTIMESTAMP - INTERVAL '1' DAY);
    
    INSERT INTO admins (admin_id, username, password_hash, real_name, phone, role, status, last_login)
    VALUES (seq_admins.NEXTVAL, 'admin_order', '$2a$10$abcdefghijklmnopqrstuvwxyz1234567890', 'Order Manager Wang', '13900002222', 'ORDER_MANAGER', 'active', SYSTIMESTAMP - INTERVAL '2' DAY);
    
    INSERT INTO admins (admin_id, username, password_hash, real_name, phone, role, status, last_login)
    VALUES (seq_admins.NEXTVAL, 'admin_finance', '$2a$10$abcdefghijklmnopqrstuvwxyz1234567890', 'Finance Officer Zhang', '13900003333', 'FINANCE_ADMIN', 'active', SYSTIMESTAMP - INTERVAL '3' DAY);
    
    COMMIT;
EXCEPTION WHEN OTHERS THEN
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('Admins insert error: ' || SQLERRM);
END;
/

-- ============================================================
-- 7. Add More Couriers (Delivery Personnel)
-- ============================================================
BEGIN
    INSERT INTO delivery_persons (courier_id, courier_name, phone, id_card_number, password_hash, status, today_delivery_count, total_delivery_count, join_date)
    VALUES (seq_delivery_persons.NEXTVAL, 'Chen Wu', '13700001111', '445201199212211234', '$2a$10$hash', 'active', 2, 145, TRUNC(SYSDATE) - 180);
    
    INSERT INTO delivery_persons (courier_id, courier_name, phone, id_card_number, password_hash, status, today_delivery_count, total_delivery_count, join_date)
    VALUES (seq_delivery_persons.NEXTVAL, 'Zhou Liu', '13700002222', '445201199312212234', '$2a$10$hash', 'active', 3, 167, TRUNC(SYSDATE) - 150);
    
    INSERT INTO delivery_persons (courier_id, courier_name, phone, id_card_number, password_hash, status, today_delivery_count, total_delivery_count, join_date)
    VALUES (seq_delivery_persons.NEXTVAL, 'Xu Yang', '13700003333', '445201199412212334', '$2a$10$hash', 'active', 1, 89, TRUNC(SYSDATE) - 90);
    
    COMMIT;
EXCEPTION WHEN OTHERS THEN
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('Delivery Persons insert error: ' || SQLERRM);
END;
/

-- ============================================================
-- 8. Add More Products with different categories and SKUs
-- ============================================================
BEGIN
    -- Beverage products
    INSERT INTO products (product_id, product_name, category_id, brand_id, barcode, cost_price, price, stock, image_url, description, avg_rating, review_count, sales_count, status)
    VALUES (seq_products.NEXTVAL, 'Mountain Dew 330ml', 2, NULL, '627634201234', 1.50, 3.80, 500, 'https://via.placeholder.com/300?text=MountainDew', 'Citrus flavored energy drink', 4.5, 12, 45, 'active');
    
    INSERT INTO products (product_id, product_name, category_id, brand_id, barcode, cost_price, price, stock, image_url, description, avg_rating, review_count, sales_count, status)
    VALUES (seq_products.NEXTVAL, 'Red Bull 250ml', 2, NULL, '627634201235', 8.00, 15.90, 200, 'https://via.placeholder.com/300?text=RedBull', 'Energy drink', 4.7, 25, 89, 'active');
    
    -- Snack products
    INSERT INTO products (product_id, product_name, category_id, brand_id, barcode, cost_price, price, stock, image_url, description, avg_rating, review_count, sales_count, status)
    VALUES (seq_products.NEXTVAL, 'Doritos Nachos Chips 170g', 3, NULL, '627634201236', 4.50, 11.90, 300, 'https://via.placeholder.com/300?text=Doritos', 'Spicy nachos flavor chips', 4.6, 18, 67, 'active');
    
    INSERT INTO products (product_id, product_name, category_id, brand_id, barcode, cost_price, price, stock, image_url, description, avg_rating, review_count, sales_count, status)
    VALUES (seq_products.NEXTVAL, 'Pringles Original 160g', 3, NULL, '627634201237', 5.00, 12.50, 250, 'https://via.placeholder.com/300?text=Pringles', 'Crispy stacked chips', 4.8, 32, 98, 'active');
    
    -- Personal care products
    INSERT INTO products (product_id, product_name, category_id, brand_id, barcode, cost_price, price, stock, image_url, description, avg_rating, review_count, sales_count, status)
    VALUES (seq_products.NEXTVAL, 'Dove Body Lotion 400ml', 5, NULL, '627634201238', 12.00, 28.90, 150, 'https://via.placeholder.com/300?text=DoveLotion', 'Moisturizing body lotion', 4.7, 22, 56, 'active');
    
    INSERT INTO products (product_id, product_name, category_id, brand_id, barcode, cost_price, price, stock, image_url, description, avg_rating, review_count, sales_count, status)
    VALUES (seq_products.NEXTVAL, 'Listerine Mouthwash 500ml', 5, NULL, '627634201239', 8.50, 19.90, 180, 'https://via.placeholder.com/300?text=Listerine', 'Antiseptic mouthwash', 4.6, 15, 42, 'active');
    
    -- Dairy products
    INSERT INTO products (product_id, product_name, category_id, brand_id, barcode, cost_price, price, stock, image_url, description, avg_rating, review_count, sales_count, status)
    VALUES (seq_products.NEXTVAL, 'Yili Yogurt 250ml x4', 4, NULL, '627634201240', 6.00, 14.90, 300, 'https://via.placeholder.com/300?text=YiliYogurt', 'Fresh yogurt', 4.8, 35, 120, 'active');
    
    INSERT INTO products (product_id, product_name, category_id, brand_id, barcode, cost_price, price, stock, image_url, description, avg_rating, review_count, sales_count, status)
    VALUES (seq_products.NEXTVAL, 'Organic Milk 1L', 4, NULL, '627634201241', 3.50, 8.50, 200, 'https://via.placeholder.com/300?text=OrganicMilk', 'Pure organic milk', 4.9, 28, 95, 'active');
    
    COMMIT;
EXCEPTION WHEN OTHERS THEN
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('Products insert error: ' || SQLERRM);
END;
/

-- ============================================================
-- 9. Add Product SKUs (Size/Color variants)
-- ============================================================
BEGIN
    INSERT INTO product_skus (sku_id, product_id, sku_name, price_adjustment, stock, status)
    VALUES (seq_product_skus.NEXTVAL, 1000, '330ml Single', 0, 500, 'active');
    
    INSERT INTO product_skus (sku_id, product_id, sku_name, price_adjustment, stock, status)
    VALUES (seq_product_skus.NEXTVAL, 1000, '6 Pack Bundle', 1.50, 200, 'active');
    
    INSERT INTO product_skus (sku_id, product_id, sku_name, price_adjustment, stock, status)
    VALUES (seq_product_skus.NEXTVAL, 1001, '550ml Single', 0, 400, 'active');
    
    INSERT INTO product_skus (sku_id, product_id, sku_name, price_adjustment, stock, status)
    VALUES (seq_product_skus.NEXTVAL, 1001, '24 Pack Box', 3.00, 100, 'active');
    
    COMMIT;
EXCEPTION WHEN OTHERS THEN
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('Product SKUs insert error: ' || SQLERRM);
END;
/

-- ============================================================
-- 10. Add More Customer Reviews (Evaluations)
-- ============================================================
DECLARE
    v_user_id NUMBER;
BEGIN
    BEGIN
        SELECT user_id INTO v_user_id FROM users WHERE username = '13800138002';
    EXCEPTION WHEN NO_DATA_FOUND THEN v_user_id := NULL;
    END;
    
    IF v_user_id IS NOT NULL THEN
        INSERT INTO reviews (review_id, product_id, user_id, rating, content, is_anonymous, create_time)
        VALUES (seq_reviews.NEXTVAL, 1005, v_user_id, 5, 'Great quality shampoo, hair feels very clean', 0, SYSTIMESTAMP - INTERVAL '5' DAY);
        
        INSERT INTO reviews (review_id, product_id, user_id, rating, content, is_anonymous, create_time)
        VALUES (seq_reviews.NEXTVAL, 1006, v_user_id, 4, 'Good toothpaste, whitening effect noticeable', 0, SYSTIMESTAMP - INTERVAL '4' DAY);
        
        INSERT INTO reviews (review_id, product_id, user_id, rating, content, is_anonymous, create_time)
        VALUES (seq_reviews.NEXTVAL, 1007, v_user_id, 5, 'Crispy and delicious, perfect snack', 0, SYSTIMESTAMP - INTERVAL '3' DAY);
    END IF;
    
    COMMIT;
EXCEPTION WHEN OTHERS THEN
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('Reviews insert error: ' || SQLERRM);
END;
/

-- ============================================================
-- 11. Add Purchase Orders (for warehouse management)
-- ============================================================
BEGIN
    INSERT INTO purchase_orders (po_id, po_no, supplier_id, total_amount, status, order_date, expected_delivery_date, received_date, remark)
    VALUES (seq_purchase_orders.NEXTVAL, 'PO202405001', 1, 5000.00, 'RECEIVED', TRUNC(SYSDATE) - 30, TRUNC(SYSDATE) - 20, TRUNC(SYSDATE) - 18, 'Beverages bulk order');
    
    INSERT INTO purchase_orders (po_id, po_no, supplier_id, total_amount, status, order_date, expected_delivery_date, received_date, remark)
    VALUES (seq_purchase_orders.NEXTVAL, 'PO202405002', 2, 3500.00, 'RECEIVED', TRUNC(SYSDATE) - 25, TRUNC(SYSDATE) - 15, TRUNC(SYSDATE) - 14, 'Snacks and confectionery');
    
    INSERT INTO purchase_orders (po_id, po_no, supplier_id, total_amount, status, order_date, expected_delivery_date, remark)
    VALUES (seq_purchase_orders.NEXTVAL, 'PO202405003', 3, 2800.00, 'PENDING', TRUNC(SYSDATE) - 5, TRUNC(SYSDATE) + 5, 'Personal care products');
    
    COMMIT;
EXCEPTION WHEN OTHERS THEN
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('Purchase Orders insert error: ' || SQLERRM);
END;
/

-- ============================================================
-- 12. Add Damage Records (Warehouse damage tracking)
-- ============================================================
BEGIN
    INSERT INTO damage_records (damage_id, product_id, quantity, reason, remark, operator_id, create_time)
    VALUES (seq_damage_records.NEXTVAL, 1000, 5, 'BROKEN', 'Bottle burst during shelf arrangement', 3, SYSTIMESTAMP - INTERVAL '10' DAY);
    
    INSERT INTO damage_records (damage_id, product_id, quantity, reason, remark, operator_id, create_time)
    VALUES (seq_damage_records.NEXTVAL, 1005, 2, 'EXPIRY', 'Expired products, removed from stock', 4, SYSTIMESTAMP - INTERVAL '8' DAY);
    
    INSERT INTO damage_records (damage_id, product_id, quantity, reason, remark, operator_id, create_time)
    VALUES (seq_damage_records.NEXTVAL, 1007, 3, 'BROKEN', 'Package damaged by forklift', 5, SYSTIMESTAMP - INTERVAL '5' DAY);
    
    COMMIT;
EXCEPTION WHEN OTHERS THEN
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('Damage Records insert error: ' || SQLERRM);
END;
/

-- ============================================================
-- 13. Add More Seckill Activities
-- ============================================================
BEGIN
    INSERT INTO seckill_activities (activity_id, activity_name, start_time, end_time, status)
    VALUES (seq_seckill_activities.NEXTVAL, 'Noon Flash Sale 11:00-13:00', TRUNC(SYSDATE) + INTERVAL '11' HOUR, TRUNC(SYSDATE) + INTERVAL '13' HOUR, 'active');
    
    INSERT INTO seckill_activities (activity_id, activity_name, start_time, end_time, status)
    VALUES (seq_seckill_activities.NEXTVAL, 'Evening Flash Sale 19:00-21:00', TRUNC(SYSDATE) + INTERVAL '19' HOUR, TRUNC(SYSDATE) + INTERVAL '21' HOUR, 'active');
    
    INSERT INTO seckill_activities (activity_id, activity_name, start_time, end_time, status)
    VALUES (seq_seckill_activities.NEXTVAL, 'Midnight Flash Sale 23:00-01:00', TRUNC(SYSDATE) + INTERVAL '23' HOUR, TRUNC(SYSDATE + 1) + INTERVAL '1' HOUR, 'active');
    
    COMMIT;
EXCEPTION WHEN OTHERS THEN
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('Seckill Activities insert error: ' || SQLERRM);
END;
/

-- ============================================================
-- 14. Add More Messages (System notifications)
-- ============================================================
DECLARE
    v_user_id NUMBER;
BEGIN
    BEGIN
        SELECT user_id INTO v_user_id FROM users WHERE username = '13800138001';
    EXCEPTION WHEN NO_DATA_FOUND THEN v_user_id := NULL;
    END;
    
    IF v_user_id IS NOT NULL THEN
        INSERT INTO messages (message_id, user_id, title, content, msg_type, is_read, create_time)
        VALUES (seq_messages.NEXTVAL, v_user_id, 'Order Shipped', 'Your order SM202604200001 has been shipped', 'ORDER', 0, SYSTIMESTAMP - INTERVAL '2' DAY);
        
        INSERT INTO messages (message_id, user_id, title, content, msg_type, is_read, create_time)
        VALUES (seq_messages.NEXTVAL, v_user_id, 'Coupon Available', 'You have a new 50 RMB coupon available', 'COUPON', 0, SYSTIMESTAMP - INTERVAL '1' DAY);
        
        INSERT INTO messages (message_id, user_id, title, content, msg_type, is_read, create_time)
        VALUES (seq_messages.NEXTVAL, v_user_id, 'System Announcement', 'Platform maintenance scheduled for next Sunday', 'SYSTEM', 1, SYSTIMESTAMP - INTERVAL '5' DAY);
    END IF;
    
    COMMIT;
EXCEPTION WHEN OTHERS THEN
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('Messages insert error: ' || SQLERRM);
END;
/

-- ============================================================
-- Final Commit
-- ============================================================
COMMIT;

PROMPT ============================================================
PROMPT Supplementary data insertion completed successfully
PROMPT ============================================================
