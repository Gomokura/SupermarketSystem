-- 创建序列
CREATE SEQUENCE seq_user START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_category START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_product START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_order START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_order_item START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_inventory_log START WITH 1 INCREMENT BY 1;

-- 创建索引
CREATE INDEX idx_product_name ON products(product_name);
CREATE INDEX idx_product_category ON products(category_id);
CREATE INDEX idx_order_user ON orders(user_id);
CREATE INDEX idx_order_time ON orders(order_time);
