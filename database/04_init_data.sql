-- 插入初始管理员账户
INSERT INTO users (user_id, username, password, real_name, role, status)
VALUES (seq_user.NEXTVAL, 'admin', 'admin123', '系统管理员', 'admin', 'active');

-- 插入测试普通用户
INSERT INTO users (user_id, username, password, real_name, role, status)
VALUES (seq_user.NEXTVAL, 'user01', 'user123', '张三', 'user', 'active');

-- 插入商品分类
INSERT INTO categories VALUES (seq_category.NEXTVAL, '食品', '食品类商品');
INSERT INTO categories VALUES (seq_category.NEXTVAL, '饮料', '饮料类商品');
INSERT INTO categories VALUES (seq_category.NEXTVAL, '日用品', '日用品类商品');

-- 插入测试商品
INSERT INTO products (product_id, product_name, category_id, price, stock, unit, supplier, status)
VALUES (seq_product.NEXTVAL, '可口可乐', 2, 3.5, 100, '瓶', '可口可乐公司', 'active');
INSERT INTO products (product_id, product_name, category_id, price, stock, unit, supplier, status)
VALUES (seq_product.NEXTVAL, '康师傅方便面', 1, 4.5, 200, '包', '康师傅', 'active');
INSERT INTO products (product_id, product_name, category_id, price, stock, unit, supplier, status)
VALUES (seq_product.NEXTVAL, '洗洁精', 3, 8.0, 50, '瓶', '立白', 'active');

COMMIT;
