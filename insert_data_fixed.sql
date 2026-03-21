-- 插入用户数据 (user_id, username, password, real_name, role, phone, status, create_time)
INSERT INTO users VALUES (1, 'user1', '123456', '张三', 'user', '13800138001', 'active', SYSDATE);
INSERT INTO users VALUES (2, 'user2', '123456', '李四', 'user', '13800138002', 'active', SYSDATE);
INSERT INTO users VALUES (3, 'user3', '123456', '王五', 'user', '13800138003', 'active', SYSDATE);
INSERT INTO users VALUES (4, 'user4', '123456', '赵六', 'user', '13800138004', 'active', SYSDATE);
INSERT INTO users VALUES (5, 'user5', '123456', '钱七', 'user', '13800138005', 'active', SYSDATE);
INSERT INTO users VALUES (6, 'user6', '123456', '孙八', 'user', '13800138006', 'active', SYSDATE);
INSERT INTO users VALUES (7, 'user7', '123456', '周九', 'user', '13800138007', 'active', SYSDATE);
INSERT INTO users VALUES (8, 'user8', '123456', '吴十', 'user', '13800138008', 'active', SYSDATE);
INSERT INTO users VALUES (9, 'admin', '123456', '管理员', 'admin', '13900139000', 'active', SYSDATE);
INSERT INTO users VALUES (10, 'test', '123456', '测试用户', 'user', '13900139001', 'active', SYSDATE);

-- 插入分类数据
INSERT INTO categories VALUES (1, '食品', '各类食品');
INSERT INTO categories VALUES (2, '饮料', '各类饮品');
INSERT INTO categories VALUES (3, '日用品', '生活用品');
INSERT INTO categories VALUES (4, '零食', '休闲零食');
INSERT INTO categories VALUES (5, '调味品', '厨房调料');
INSERT INTO categories VALUES (6, '清洁用品', '家居清洁');
INSERT INTO categories VALUES (7, '个人护理', '洗护用品');
INSERT INTO categories VALUES (8, '文具', '办公文具');
INSERT INTO categories VALUES (9, '电子产品', '数码配件');
INSERT INTO categories VALUES (10, '其他', '其他商品');

-- 插入商品数据 (product_id, product_name, category_id, price, stock, unit, supplier, status, create_time)
INSERT INTO products VALUES (1, '可口可乐', 2, 3.5, 100, '瓶', '可口可乐公司', 'active', SYSDATE);
INSERT INTO products VALUES (2, '康师傅方便面', 1, 4.5, 200, '袋', '康师傅', 'active', SYSDATE);
INSERT INTO products VALUES (3, '农夫山泉', 2, 2.0, 150, '瓶', '农夫山泉', 'active', SYSDATE);
INSERT INTO products VALUES (4, '奥利奥饼干', 4, 12.5, 80, '盒', '亿滋', 'active', SYSDATE);
INSERT INTO products VALUES (5, '洗洁精', 6, 8.0, 60, '瓶', '立白', 'active', SYSDATE);
INSERT INTO products VALUES (6, '牙膏', 7, 15.0, 90, '支', '高露洁', 'active', SYSDATE);
INSERT INTO products VALUES (7, '酱油', 5, 6.5, 120, '瓶', '海天', 'active', SYSDATE);
INSERT INTO products VALUES (8, '笔记本', 8, 5.0, 200, '本', '晨光', 'active', SYSDATE);
INSERT INTO products VALUES (9, '充电线', 9, 19.9, 50, '根', '品胜', 'active', SYSDATE);
INSERT INTO products VALUES (10, '垃圾袋', 3, 9.9, 100, '卷', '妙洁', 'active', SYSDATE);

-- 插入购物车数据
INSERT INTO cart VALUES (1, 1, 1, 2, SYSDATE);
INSERT INTO cart VALUES (2, 1, 3, 3, SYSDATE);
INSERT INTO cart VALUES (3, 2, 2, 1, SYSDATE);
INSERT INTO cart VALUES (4, 2, 4, 2, SYSDATE);
INSERT INTO cart VALUES (5, 3, 5, 1, SYSDATE);
INSERT INTO cart VALUES (6, 3, 6, 1, SYSDATE);
INSERT INTO cart VALUES (7, 4, 7, 2, SYSDATE);
INSERT INTO cart VALUES (8, 5, 8, 5, SYSDATE);
INSERT INTO cart VALUES (9, 6, 9, 1, SYSDATE);
INSERT INTO cart VALUES (10, 7, 10, 3, SYSDATE);

-- 插入订单数据 (order_id, user_id, total_amount, order_status, payment_method, order_time)
INSERT INTO orders VALUES (1, 1, 19.50, 'completed', 'cash', SYSDATE-5);
INSERT INTO orders VALUES (2, 1, 45.00, 'completed', 'wechat', SYSDATE-3);
INSERT INTO orders VALUES (3, 2, 28.50, 'pending', 'alipay', SYSDATE-2);
INSERT INTO orders VALUES (4, 3, 15.00, 'completed', 'card', SYSDATE-1);
INSERT INTO orders VALUES (5, 4, 33.00, 'pending', 'wechat', SYSDATE);
INSERT INTO orders VALUES (6, 5, 50.00, 'completed', 'cash', SYSDATE-4);
INSERT INTO orders VALUES (7, 6, 22.50, 'cancelled', 'alipay', SYSDATE-6);
INSERT INTO orders VALUES (8, 7, 38.00, 'completed', 'wechat', SYSDATE-7);
INSERT INTO orders VALUES (9, 8, 12.00, 'completed', 'cash', SYSDATE-8);
INSERT INTO orders VALUES (10, 2, 60.00, 'completed', 'card', SYSDATE-9);

-- 插入订单明细数据 (item_id, order_id, product_id, quantity, unit_price, subtotal)
INSERT INTO order_items VALUES (1, 1, 1, 3, 3.5, 10.5);
INSERT INTO order_items VALUES (2, 1, 2, 2, 4.5, 9.0);
INSERT INTO order_items VALUES (3, 2, 3, 5, 2.0, 10.0);
INSERT INTO order_items VALUES (4, 2, 4, 2, 12.5, 25.0);
INSERT INTO order_items VALUES (5, 3, 5, 1, 8.0, 8.0);
INSERT INTO order_items VALUES (6, 3, 6, 1, 15.0, 15.0);
INSERT INTO order_items VALUES (7, 4, 7, 2, 6.5, 13.0);
INSERT INTO order_items VALUES (8, 5, 8, 10, 5.0, 50.0);
INSERT INTO order_items VALUES (9, 6, 9, 2, 19.9, 39.8);
INSERT INTO order_items VALUES (10, 7, 10, 3, 9.9, 29.7);

COMMIT;
