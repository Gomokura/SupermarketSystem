-- 促销表
CREATE TABLE promotions (
    promotion_id   NUMBER PRIMARY KEY,
    promo_name     VARCHAR2(100) NOT NULL,
    promo_type     VARCHAR2(20) NOT NULL,  -- discount(折扣) / reduce(满减)
    condition_val  NUMBER(10,2) NOT NULL,  -- 折扣率(0.9) 或 满减门槛(100)
    discount_val   NUMBER(10,2) NOT NULL,  -- 折扣率 或 减免金额
    start_time     DATE NOT NULL,
    end_time       DATE NOT NULL,
    status         VARCHAR2(10) DEFAULT 'active',
    create_time    DATE DEFAULT SYSDATE
);
CREATE SEQUENCE seq_promotion START WITH 1 INCREMENT BY 1;

-- 促销绑定商品（空表示全场）
CREATE TABLE promotion_products (
    id             NUMBER PRIMARY KEY,
    promotion_id   NUMBER NOT NULL,
    product_id     NUMBER NOT NULL,
    FOREIGN KEY (promotion_id) REFERENCES promotions(promotion_id),
    FOREIGN KEY (product_id)   REFERENCES products(product_id)
);
CREATE SEQUENCE seq_promo_product START WITH 1 INCREMENT BY 1;

-- 配送表
CREATE TABLE deliveries (
    delivery_id    NUMBER PRIMARY KEY,
    order_id       NUMBER NOT NULL UNIQUE,
    address        VARCHAR2(200),
    receiver       VARCHAR2(50),
    phone          VARCHAR2(20),
    status         VARCHAR2(20) DEFAULT 'pending',  -- pending/dispatched/delivering/done
    dispatch_time  DATE,
    done_time      DATE,
    remark         VARCHAR2(200),
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);
CREATE SEQUENCE seq_delivery START WITH 1 INCREMENT BY 1;
