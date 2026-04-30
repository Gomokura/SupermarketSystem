-- 插入测试用户
INSERT INTO users (id, username, password, email, phone, real_name, gender, avatar, status, points, level_id, created_time)
VALUES (seq_users.nextval, 'testuser', '$2a$10$7OuqQzSX0yvHMt7UdzLx1OFCN9Kd7bQkGKMW8Ek8Ycfyw.B4V8.4e', 'test@example.com', '13800138000', '测试用户', 1, NULL, 1, 0, 1, SYSDATE);

INSERT INTO users (id, username, password, email, phone, real_name, gender, avatar, status, points, level_id, created_time)
VALUES (seq_users.nextval, 'admin', '$2a$10$7OuqQzSX0yvHMt7UdzLx1OFCN9Kd7bQkGKMW8Ek8Ycfyw.B4V8.4e', 'admin@example.com', '13800138001', '管理员', 1, NULL, 1, 0, 1, SYSDATE);

-- 插入管理员
INSERT INTO admins (id, username, password, email, phone, real_name, avatar, status, created_time)
VALUES (seq_admins.nextval, 'admin', '$2a$10$7OuqQzSX0yvHMt7UdzLx1OFCN9Kd7bQkGKMW8Ek8Ycfyw.B4V8.4e', 'admin@example.com', '13800138001', '管理员', NULL, 1, SYSDATE);

-- 插入分类
INSERT INTO categories (id, category_name, description, status, created_time)
VALUES (seq_categories.nextval, '食品', '食品分类', 1, SYSDATE);

INSERT INTO categories (id, category_name, description, status, created_time)
VALUES (seq_categories.nextval, '日用品', '日用品分类', 1, SYSDATE);

INSERT INTO categories (id, category_name, description, status, created_time)
VALUES (seq_categories.nextval, '电子产品', '电子产品分类', 1, SYSDATE);

-- 插入品牌
INSERT INTO brands (id, brand_name, logo_url, status, created_time)
VALUES (seq_brands.nextval, '品牌A', 'http://example.com/logo1.png', 1, SYSDATE);

INSERT INTO brands (id, brand_name, logo_url, status, created_time)
VALUES (seq_brands.nextval, '品牌B', 'http://example.com/logo2.png', 1, SYSDATE);

-- 插入商品
INSERT INTO products (id, product_name, category_id, brand_id, price, stock, description, status, created_time)
VALUES (seq_products.nextval, '测试商品1', 1, 1, 99.99, 100, '这是一个测试商品', 1, SYSDATE);

INSERT INTO products (id, product_name, category_id, brand_id, price, stock, description, status, created_time)
VALUES (seq_products.nextval, '测试商品2', 1, 2, 199.99, 50, '这是第二个测试商品', 1, SYSDATE);

INSERT INTO products (id, product_name, category_id, brand_id, price, stock, description, status, created_time)
VALUES (seq_products.nextval, '测试商品3', 2, 1, 49.99, 200, '这是第三个测试商品', 1, SYSDATE);

-- 插入Banner
INSERT INTO banners (id, banner_image, banner_title, banner_link, sort_order, is_enabled, created_time)
VALUES (seq_banners.nextval, 'http://example.com/banner1.jpg', '促销活动1', 'http://example.com/promo1', 1, 1, SYSDATE);

-- 插入优惠券
INSERT INTO coupons (id, coupon_name, coupon_code, discount_type, discount_value, usage_limit, current_usage, start_time, end_time, status, created_time)
VALUES (seq_coupons.nextval, '新人优惠券', 'NEWUSER10', 1, 10, 100, 0, SYSDATE - 1, SYSDATE + 30, 1, SYSDATE);

-- 插入秒杀活动
INSERT INTO seckill_activity_model (id, activity_name, activity_start_time, activity_end_time, description, status, created_time)
VALUES (seq_seckill_activity_model.nextval, '周末秒杀', SYSDATE - 1, SYSDATE + 2, '周末秒杀活动', 1, SYSDATE);

COMMIT;
