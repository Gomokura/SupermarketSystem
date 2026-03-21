-- 添加更多商品数据
INSERT INTO products (product_id, product_name, category_id, price, stock, unit, supplier, status)
VALUES (seq_product.NEXTVAL, '农夫山泉', 2, 2.0, 500, '瓶', '农夫山泉', 'active');

INSERT INTO products (product_id, product_name, category_id, price, stock, unit, supplier, status)
VALUES (seq_product.NEXTVAL, '统一冰红茶', 2, 3.0, 300, '瓶', '统一', 'active');

INSERT INTO products (product_id, product_name, category_id, price, stock, unit, supplier, status)
VALUES (seq_product.NEXTVAL, '旺旺雪饼', 1, 5.5, 150, '包', '旺旺', 'active');

INSERT INTO products (product_id, product_name, category_id, price, stock, unit, supplier, status)
VALUES (seq_product.NEXTVAL, '奥利奥饼干', 1, 8.0, 200, '盒', '奥利奥', 'active');

INSERT INTO products (product_id, product_name, category_id, price, stock, unit, supplier, status)
VALUES (seq_product.NEXTVAL, '牙膏', 3, 12.0, 100, '支', '高露洁', 'active');

INSERT INTO products (product_id, product_name, category_id, price, stock, unit, supplier, status)
VALUES (seq_product.NEXTVAL, '洗发水', 3, 25.0, 80, '瓶', '海飞丝', 'active');

INSERT INTO products (product_id, product_name, category_id, price, stock, unit, supplier, status)
VALUES (seq_product.NEXTVAL, '卫生纸', 3, 15.0, 120, '包', '维达', 'active');

INSERT INTO products (product_id, product_name, category_id, price, stock, unit, supplier, status)
VALUES (seq_product.NEXTVAL, '薯片', 1, 6.5, 180, '包', '乐事', 'active');

COMMIT;
