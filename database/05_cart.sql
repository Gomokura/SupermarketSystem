-- 购物车表
CREATE TABLE cart (
    cart_id NUMBER PRIMARY KEY,
    user_id NUMBER NOT NULL,
    product_id NUMBER NOT NULL,
    quantity NUMBER DEFAULT 1,
    add_time DATE DEFAULT SYSDATE,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- 购物车序列
CREATE SEQUENCE seq_cart START WITH 1 INCREMENT BY 1;

-- 索引
CREATE INDEX idx_cart_user ON cart(user_id);
