-- 4. 订单表
CREATE TABLE orders (
    order_id NUMBER PRIMARY KEY,
    user_id NUMBER,
    total_amount NUMBER(10,2),
    order_status VARCHAR2(20) DEFAULT 'pending',
    payment_method VARCHAR2(20),
    order_time DATE DEFAULT SYSDATE,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- 5. 订单明细表
CREATE TABLE order_items (
    item_id NUMBER PRIMARY KEY,
    order_id NUMBER,
    product_id NUMBER,
    quantity NUMBER NOT NULL,
    unit_price NUMBER(10,2),
    subtotal NUMBER(10,2),
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- 6. 库存记录表
CREATE TABLE inventory_logs (
    log_id NUMBER PRIMARY KEY,
    product_id NUMBER,
    change_type VARCHAR2(20),
    quantity NUMBER,
    operator_id NUMBER,
    remark VARCHAR2(200),
    log_time DATE DEFAULT SYSDATE,
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);
