-- =============================================
-- 08_additions.sql
-- 新增表 + 修改现有表以支持完整业务流程
-- =============================================

-- 1. users 允许 courier 角色
ALTER TABLE users DROP CONSTRAINT SYS_C_ROLE;
ALTER TABLE users ADD CONSTRAINT chk_role CHECK (role IN ('admin','user','courier'));

-- 2. categories 支持层级
ALTER TABLE categories ADD parent_id NUMBER DEFAULT NULL;

-- 3. deliveries 绑定配送员
ALTER TABLE deliveries ADD courier_id NUMBER;
ALTER TABLE deliveries ADD CONSTRAINT fk_delivery_courier FOREIGN KEY (courier_id) REFERENCES users(user_id);

-- 4. 支付表
CREATE TABLE payment (
    payment_id   NUMBER PRIMARY KEY,
    order_id     NUMBER NOT NULL,
    amount       NUMBER(10,2) NOT NULL,
    status       VARCHAR2(10) DEFAULT 'success' CHECK (status IN ('success','failed')),
    pay_time     DATE DEFAULT SYSDATE,
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);
CREATE SEQUENCE seq_payment START WITH 1 INCREMENT BY 1;

-- 5. 入库表
CREATE TABLE warehousing (
    warehousing_id NUMBER PRIMARY KEY,
    product_id     NUMBER NOT NULL,
    quantity       NUMBER NOT NULL,
    operator_id    NUMBER,
    remark         VARCHAR2(200),
    create_time    DATE DEFAULT SYSDATE,
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);
CREATE SEQUENCE seq_warehousing START WITH 1 INCREMENT BY 1;

-- 6. 出库表（必须绑定订单）
CREATE TABLE outbound (
    outbound_id  NUMBER PRIMARY KEY,
    order_id     NUMBER NOT NULL,
    product_id   NUMBER NOT NULL,
    quantity     NUMBER NOT NULL,
    create_time  DATE DEFAULT SYSDATE,
    FOREIGN KEY (order_id)   REFERENCES orders(order_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);
CREATE SEQUENCE seq_outbound START WITH 1 INCREMENT BY 1;

-- 7. 用户地址表
CREATE TABLE address (
    address_id  NUMBER PRIMARY KEY,
    user_id     NUMBER NOT NULL,
    receiver    VARCHAR2(50),
    phone       VARCHAR2(20),
    detail      VARCHAR2(255) NOT NULL,
    is_default  NUMBER(1) DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
CREATE SEQUENCE seq_address START WITH 1 INCREMENT BY 1;

-- 8. 用户行为表（推荐系统）
CREATE TABLE user_behavior (
    behavior_id  NUMBER PRIMARY KEY,
    user_id      NUMBER NOT NULL,
    product_id   NUMBER NOT NULL,
    action       VARCHAR2(10) CHECK (action IN ('view','click','buy')),
    create_time  DATE DEFAULT SYSDATE,
    FOREIGN KEY (user_id)    REFERENCES users(user_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);
CREATE SEQUENCE seq_behavior START WITH 1 INCREMENT BY 1;

-- 9. 库存视图（动态计算，禁止直接读 products.stock）
CREATE OR REPLACE VIEW v_stock AS
SELECT
    p.product_id,
    p.product_name,
    NVL(SUM(w.quantity), 0) - NVL(SUM(ob.quantity), 0) AS stock
FROM products p
LEFT JOIN warehousing w  ON p.product_id = w.product_id
LEFT JOIN outbound   ob  ON p.product_id = ob.product_id
GROUP BY p.product_id, p.product_name;
