-- ============================================================
-- 超市管理系统 - 测试数据初始化脚本
-- 密码格式：MD5加密
-- ============================================================

SET FEEDBACK ON
SET ECHO ON

-- ==================== 1. 插入测试用户（C端） ====================
-- 密码: user123 -> MD5: 6ad14ba9986e3615423dfca256d04e3f

INSERT INTO users (user_id, username, password, nickname, real_name, phone, member_level, points, status, create_time)
VALUES (1001, '13800138001', '6ad14ba9986e3615423dfca256d04e3f', '银卡会员', '张三', '13800138001', 'SILVER', 520, 'active', SYSTIMESTAMP);

INSERT INTO users (user_id, username, password, nickname, real_name, phone, member_level, points, status, create_time)
VALUES (1002, '13800138002', '6ad14ba9986e3615423dfca256d04e3f', '金卡会员', '李四', '13800138002', 'GOLD', 1200, 'active', SYSTIMESTAMP);

INSERT INTO users (user_id, username, password, nickname, real_name, phone, member_level, points, status, create_time)
VALUES (1003, '13800138003', '6ad14ba9986e3615423dfca256d04e3f', '普通会员', '王五', '13800138003', 'NORMAL', 0, 'active', SYSTIMESTAMP);

-- ==================== 2. 插入管理员（B端） ====================
-- admin123 -> MD5: 0192023a7bbd73250516f069df18b500
-- manager123 -> MD5: 0795151defba7a4b5dfa89170de46277
-- cashier123 -> MD5: dbb8c54ee649f8af049357a5f99cede6
-- warehouse123 -> MD5: e7bca0b30b7fdb9f0ecbb7832c5f5348
-- product123 -> MD5: 4751368fbef4cc9420716a698d0c393a
-- service123 -> MD5: 3df2c034f564ae53106c928b7278d1ca

INSERT INTO admin_users (admin_id, username, password, real_name, phone, role, status, create_time)
VALUES (1, 'admin', '0192023a7bbd73250516f069df18b500', '超级管理员', '13800000001', 'SUPER_ADMIN', 'active', SYSTIMESTAMP);

INSERT INTO admin_users (admin_id, username, password, real_name, phone, role, status, create_time)
VALUES (2, 'manager', '0795151defba7a4b5dfa89170de46277', '店长', '13800000002', 'MANAGER', 'active', SYSTIMESTAMP);

INSERT INTO admin_users (admin_id, username, password, real_name, phone, role, status, create_time)
VALUES (3, 'cashier01', 'dbb8c54ee649f8af049357a5f99cede6', '收银员小张', '13800000003', 'CASHIER', 'active', SYSTIMESTAMP);

INSERT INTO admin_users (admin_id, username, password, real_name, phone, role, status, create_time)
VALUES (4, 'warehouse01', 'e7bca0b30b7fdb9f0ecbb7832c5f5348', '仓管老王', '13800000004', 'WAREHOUSE', 'active', SYSTIMESTAMP);

INSERT INTO admin_users (admin_id, username, password, real_name, phone, role, status, create_time)
VALUES (5, 'product01', '4751368fbef4cc9420716a698d0c393a', '商品专员小李', '13800000005', 'PRODUCT', 'active', SYSTIMESTAMP);

INSERT INTO admin_users (admin_id, username, password, real_name, phone, role, status, create_time)
VALUES (6, 'service01', '3df2c034f564ae53106c928b7278d1ca', '客服小美', '13800000006', 'SERVICE', 'active', SYSTIMESTAMP);

-- ==================== 3. 插入配送员 ====================
-- courier123 -> MD5: 1ed4a8186a0606351d36115f28cc3793

INSERT INTO delivery_persons (courier_id, phone, password, name, status, create_time)
VALUES (1, '13900000001', '1ed4a8186a0606351d36115f28cc3793', '张配送', 'active', SYSTIMESTAMP);

INSERT INTO delivery_persons (courier_id, phone, password, name, status, create_time)
VALUES (2, '13900000002', '1ed4a8186a0606351d36115f28cc3793', '李配送', 'active', SYSTIMESTAMP);

INSERT INTO delivery_persons (courier_id, phone, password, name, status, create_time)
VALUES (3, '13900000003', '1ed4a8186a0606351d36115f28cc3793', '王配送', 'active', SYSTIMESTAMP);

-- ==================== 4. 插入商品分类 ====================

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (1, 0, '生鲜食品', 1, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (2, 0, '日用百货', 2, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (3, 0, '饮料零食', 3, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (4, 0, '粮油调味', 4, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (5, 1, '新鲜水果', 1, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (6, 1, '新鲜蔬菜', 2, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (7, 3, '碳酸饮料', 1, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (8, 3, '休闲零食', 2, 'active');

-- ==================== 5. 插入品牌 ====================

INSERT INTO brands (brand_id, brand_name, status)
VALUES (1, '可口可乐', 'active');

INSERT INTO brands (brand_id, brand_name, status)
VALUES (2, '农夫山泉', 'active');

INSERT INTO brands (brand_id, brand_name, status)
VALUES (3, '蒙牛', 'active');

INSERT INTO brands (brand_id, brand_name, status)
VALUES (4, '伊利', 'active');

INSERT INTO brands (brand_id, brand_name, status)
VALUES (5, '乐事', 'active');

-- ==================== 6. 插入商品 ====================

INSERT INTO products (product_id, category_id, brand_id, product_name, barcode, unit, price, original_price, cost_price, stock, warning_stock, status, is_recommend, create_time)
VALUES (1, 5, 3, '蒙牛纯牛奶', '6907878100013', '箱', 59.90, 69.90, 45.00, 100, 10, 'active', 1, SYSTIMESTAMP);

INSERT INTO products (product_id, category_id, brand_id, product_name, barcode, unit, price, original_price, cost_price, stock, warning_stock, status, is_recommend, create_time)
VALUES (2, 5, 4, '伊利纯牛奶', '6907878100020', '箱', 58.90, 68.90, 44.00, 80, 10, 'active', 1, SYSTIMESTAMP);

INSERT INTO products (product_id, category_id, brand_id, product_name, barcode, unit, price, original_price, cost_price, stock, warning_stock, status, is_recommend, create_time)
VALUES (3, 7, 1, '可口可乐500ml', '6902083888001', '瓶', 3.50, 4.00, 2.50, 500, 50, 'active', 1, SYSTIMESTAMP);

INSERT INTO products (product_id, category_id, brand_id, product_name, barcode, unit, price, original_price, cost_price, stock, warning_stock, status, is_recommend, create_time)
VALUES (4, 7, 2, '农夫山泉550ml', '6921168500013', '瓶', 2.00, 2.50, 1.20, 800, 100, 'active', 1, SYSTIMESTAMP);

INSERT INTO products (product_id, category_id, brand_id, product_name, barcode, unit, price, original_price, cost_price, stock, warning_stock, status, is_recommend, create_time)
VALUES (5, 8, 5, '乐事薯片原味', '6920152400012', '袋', 8.90, 10.90, 6.50, 200, 20, 'active', 1, SYSTIMESTAMP);

INSERT INTO products (product_id, category_id, brand_id, product_name, barcode, unit, price, original_price, cost_price, stock, warning_stock, status, is_recommend, create_time)
VALUES (6, 6, 0, '新鲜西红柿', 'XS001', '斤', 3.99, 4.99, 2.50, 200, 30, 'active', 0, SYSTIMESTAMP);

INSERT INTO products (product_id, category_id, brand_id, product_name, barcode, unit, price, original_price, cost_price, stock, warning_stock, status, is_recommend, create_time)
VALUES (7, 6, 0, '新鲜黄瓜', 'XS002', '斤', 2.99, 3.99, 1.80, 150, 20, 'active', 0, SYSTIMESTAMP);

INSERT INTO products (product_id, category_id, brand_id, product_name, barcode, unit, price, original_price, cost_price, stock, warning_stock, status, is_recommend, create_time)
VALUES (8, 5, 0, '红富士苹果', 'XS003', '斤', 5.99, 6.99, 3.50, 300, 50, 'active', 1, SYSTIMESTAMP);

-- ==================== 7. 插入Banner ====================

INSERT INTO banners (banner_id, image_url, title, link_type, link_target, sort_order, status, start_time, end_time)
VALUES (1, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=supermarket%20promotion%20banner%20fresh%20groceries%20sale&image_size=landscape_16_9', '五一特惠', 'activity', '', 1, 'active', SYSTIMESTAMP, SYSTIMESTAMP + 30);

INSERT INTO banners (banner_id, image_url, title, link_type, link_target, sort_order, status, start_time, end_time)
VALUES (2, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=summer%20sale%20supermarket%20fresh%20fruits&image_size=landscape_16_9', '夏日清凉', 'category', '3', 2, 'active', SYSTIMESTAMP, SYSTIMESTAMP + 30);

-- ==================== 8. 插入优惠券 ====================

INSERT INTO coupons (coupon_id, coupon_type, name, description, min_amount, discount_value, discount_rate, total_count, issued_count, user_limit, status, start_time, end_time, create_time)
VALUES (1, 'new_user', '新人专享券', '新用户注册即送', 0, 10.00, 0, 1000, 0, 1, 'active', SYSTIMESTAMP, SYSTIMESTAMP + 90, SYSTIMESTAMP);

INSERT INTO coupons (coupon_id, coupon_type, name, description, min_amount, discount_value, discount_rate, total_count, issued_count, user_limit, status, start_time, end_time, create_time)
VALUES (2, 'full_reduce', '满100减15', '全场通用', 100.00, 15.00, 0, 500, 0, 3, 'active', SYSTIMESTAMP, SYSTIMESTAMP + 30, SYSTIMESTAMP);

INSERT INTO coupons (coupon_id, coupon_type, name, description, min_amount, discount_value, discount_rate, total_count, issued_count, user_limit, status, start_time, end_time, create_time)
VALUES (3, 'discount', '8折优惠券', '生鲜食品专用', 50.00, 0, 0.8, 300, 0, 2, 'active', SYSTIMESTAMP, SYSTIMESTAMP + 15, SYSTIMESTAMP);

COMMIT;

PROMPT ============================================================
PROMPT 测试数据初始化完成！
PROMPT ============================================================
