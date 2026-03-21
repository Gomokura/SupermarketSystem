-- 插入用户数据
INSERT INTO users VALUES (1, 'user1', '123456', '张三', '13800138001', 'user', SYSDATE);
INSERT INTO users VALUES (2, 'user2', '123456', '李四', '13800138002', 'user', SYSDATE);
INSERT INTO users VALUES (3, 'user3', '123456', '王五', '13800138003', 'user', SYSDATE);
INSERT INTO users VALUES (4, 'user4', '123456', '赵六', '13800138004', 'user', SYSDATE);
INSERT INTO users VALUES (5, 'user5', '123456', '钱七', '13800138005', 'user', SYSDATE);
INSERT INTO users VALUES (6, 'user6', '123456', '孙八', '13800138006', 'user', SYSDATE);
INSERT INTO users VALUES (7, 'user7', '123456', '周九', '13800138007', 'user', SYSDATE);
INSERT INTO users VALUES (8, 'user8', '123456', '吴十', '13800138008', 'user', SYSDATE);
INSERT INTO users VALUES (9, 'admin', '123456', '管理员', '13900139000', 'admin', SYSDATE);
INSERT INTO users VALUES (10, 'test', '123456', '测试用户', '13900139001', 'user', SYSDATE);

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

-- 插入商品数据
INSERT INTO products VALUES (1, '可口可乐', 1, 3.5, 100, '经典可乐', SYSDATE);
INSERT INTO products VALUES (2, '康师傅方便面', 1, 4.5, 200, '红烧牛肉面', SYSDATE);
INSERT INTO products VALUES (3, '农夫山泉', 2, 2.0, 150, '天然矿泉水', SYSDATE);
INSERT INTO products VALUES (4, '奥利奥饼干', 4, 12.5, 80, '夹心饼干', SYSDATE);
INSERT INTO products VALUES (5, '洗洁精', 6, 8.0, 60, '柠檬清香', SYSDATE);
INSERT INTO products VALUES (6, '牙膏', 7, 15.0, 90, '美白牙膏', SYSDATE);
INSERT INTO products VALUES (7, '酱油', 5, 6.5, 120, '生抽酱油', SYSDATE);
INSERT INTO products VALUES (8, '笔记本', 8, 5.0, 200, 'A5笔记本', SYSDATE);
INSERT INTO products VALUES (9, '充电线', 9, 19.9, 50, 'Type-C数据线', SYSDATE);
INSERT INTO products VALUES (10, '垃圾袋', 3, 9.9, 100, '加厚垃圾袋', SYSDATE);

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

-- 插入订单数据
INSERT INTO orders VALUES (1, 1, 19.50, SYSDATE-5, 'completed', 'cash');
INSERT INTO orders VALUES (2, 1, 45.00, SYSDATE-3, 'completed', 'wechat');
INSERT INTO orders VALUES (3, 2, 28.50, SYSDATE-2, 'pending', 'alipay');
INSERT INTO orders VALUES (4, 3, 15.00, SYSDATE-1, 'completed', 'card');
INSERT INTO orders VALUES (5, 4, 33.00, SYSDATE, 'pending', 'wechat');
INSERT INTO orders VALUES (6, 5, 50.00, SYSDATE-4, 'completed', 'cash');
INSERT INTO orders VALUES (7, 6, 22.50, SYSDATE-6, 'cancelled', 'alipay');
INSERT INTO orders VALUES (8, 7, 38.00, SYSDATE-7, 'completed', 'wechat');
INSERT INTO orders VALUES (9, 8, 12.00, SYSDATE-8, 'completed', 'cash');
INSERT INTO orders VALUES (10, 2, 60.00, SYSDATE-9, 'completed', 'card');

-- 插入订单明细数据
INSERT INTO order_items VALUES (1, 1, 1, 3, 3.5);
INSERT INTO order_items VALUES (2, 1, 2, 2, 4.5);
INSERT INTO order_items VALUES (3, 2, 3, 5, 2.0);
INSERT INTO order_items VALUES (4, 2, 4, 2, 12.5);
INSERT INTO order_items VALUES (5, 3, 5, 1, 8.0);
INSERT INTO order_items VALUES (6, 3, 6, 1, 15.0);
INSERT INTO order_items VALUES (7, 4, 7, 2, 6.5);
INSERT INTO order_items VALUES (8, 5, 8, 10, 5.0);
INSERT INTO order_items VALUES (9, 6, 9, 2, 19.9);
INSERT INTO order_items VALUES (10, 7, 10, 3, 9.9);

COMMIT;
