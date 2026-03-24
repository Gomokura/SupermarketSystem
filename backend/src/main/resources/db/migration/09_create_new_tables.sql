-- ============================================================
-- 超市管理系统 · 新建缺失数据库表
-- 文件：09_create_new_tables.sql
-- ============================================================

-- ============================================================
-- 1. ADMINS — 后台管理员表（与普通用户USERS分离）
-- ============================================================
CREATE TABLE ADMINS (
    admin_id        NUMBER PRIMARY KEY,
    username        VARCHAR2(50)  NOT NULL UNIQUE,
    password        VARCHAR2(100) NOT NULL,
    real_name       VARCHAR2(50),
    role            VARCHAR2(30)  NOT NULL, -- super_admin/store_manager/product_staff/finance/customer_service/warehouse
    phone           VARCHAR2(20),
    status          VARCHAR2(10)  DEFAULT 'active', -- active/disabled
    last_login_time DATE,
    create_time     DATE          DEFAULT SYSDATE
);
CREATE SEQUENCE seq_admin START WITH 1 INCREMENT BY 1;
COMMENT ON TABLE ADMINS IS '后台管理员表';
COMMENT ON COLUMN ADMINS.role IS '角色：super_admin超管/store_manager店长/product_staff商品专员/finance财务/customer_service客服/warehouse仓管';

-- ============================================================
-- 2. BRANDS — 品牌表
-- ============================================================
CREATE TABLE BRANDS (
    brand_id    NUMBER PRIMARY KEY,
    brand_name  VARCHAR2(100) NOT NULL,
    logo_url    VARCHAR2(255),
    description VARCHAR2(500),
    sort_order  NUMBER DEFAULT 0,
    status      VARCHAR2(10) DEFAULT 'active',
    create_time DATE DEFAULT SYSDATE
);
CREATE SEQUENCE seq_brand START WITH 1 INCREMENT BY 1;
COMMENT ON TABLE BRANDS IS '品牌表';

-- ============================================================
-- 3. PRODUCT_SKUS — 商品SKU规格表
-- ============================================================
CREATE TABLE PRODUCT_SKUS (
    sku_id      NUMBER PRIMARY KEY,
    product_id  NUMBER        NOT NULL,
    spec_name   VARCHAR2(200),          -- 规格描述，如"红色 500ml"
    price       NUMBER(10,2)  NOT NULL,
    stock       NUMBER        DEFAULT 0,
    barcode     VARCHAR2(50),
    status      VARCHAR2(10)  DEFAULT 'active',
    CONSTRAINT fk_sku_product FOREIGN KEY (product_id) REFERENCES PRODUCTS(product_id)
);
CREATE SEQUENCE seq_sku START WITH 1 INCREMENT BY 1;
CREATE INDEX idx_sku_product ON PRODUCT_SKUS(product_id);
COMMENT ON TABLE PRODUCT_SKUS IS '商品SKU规格表';
COMMENT ON COLUMN PRODUCT_SKUS.spec_name IS '规格描述，如"红色-500ml"';

-- ============================================================
-- 4. COUPONS — 优惠券模板表
-- ============================================================
CREATE TABLE COUPONS (
    coupon_id    NUMBER PRIMARY KEY,
    coupon_name  VARCHAR2(100) NOT NULL,
    coupon_type  VARCHAR2(20)  NOT NULL, -- full_reduction满减/discount折扣/category品类
    min_amount   NUMBER(10,2)  DEFAULT 0, -- 使用门槛
    discount     NUMBER(10,2)  NOT NULL,  -- 满减时=减免金额；折扣时=折扣率(如0.85=八五折)
    category_id  NUMBER,                  -- 品类券指定分类，NULL=全场
    total_count  NUMBER        DEFAULT 0, -- 总发放量，0=不限
    per_limit    NUMBER        DEFAULT 1, -- 每人限领次数
    issued_count NUMBER        DEFAULT 0, -- 已发放数量
    start_time   DATE          NOT NULL,
    end_time     DATE          NOT NULL,
    status       VARCHAR2(10)  DEFAULT 'active', -- active/paused/expired
    create_time  DATE          DEFAULT SYSDATE
);
CREATE SEQUENCE seq_coupon START WITH 1 INCREMENT BY 1;
COMMENT ON TABLE COUPONS IS '优惠券模板表';
COMMENT ON COLUMN COUPONS.coupon_type IS '类型：full_reduction满减 / discount折扣 / category品类券';
COMMENT ON COLUMN COUPONS.discount IS '满减时=减免金额；折扣时=折扣率(0.85=八五折)';

-- ============================================================
-- 5. USER_COUPONS — 用户领取的优惠券
-- ============================================================
CREATE TABLE USER_COUPONS (
    id           NUMBER PRIMARY KEY,
    user_id      NUMBER       NOT NULL,
    coupon_id    NUMBER       NOT NULL,
    status       VARCHAR2(20) DEFAULT 'unused', -- unused未使用/used已使用/expired已过期
    receive_time DATE         DEFAULT SYSDATE,
    use_time     DATE,
    order_id     NUMBER,
    CONSTRAINT fk_uc_user   FOREIGN KEY (user_id)   REFERENCES USERS(user_id),
    CONSTRAINT fk_uc_coupon FOREIGN KEY (coupon_id) REFERENCES COUPONS(coupon_id)
);
CREATE SEQUENCE seq_user_coupon START WITH 1 INCREMENT BY 1;
CREATE INDEX idx_uc_user   ON USER_COUPONS(user_id);
CREATE INDEX idx_uc_status ON USER_COUPONS(status);
COMMENT ON TABLE USER_COUPONS IS '用户已领取的优惠券';

-- ============================================================
-- 6. AFTER_SALES — 售后/退款申请表
-- ============================================================
CREATE TABLE AFTER_SALES (
    after_sale_id  NUMBER PRIMARY KEY,
    order_id       NUMBER        NOT NULL,
    user_id        NUMBER        NOT NULL,
    after_type     VARCHAR2(30)  NOT NULL, -- refund_only仅退款 / return_refund退货退款
    reason         VARCHAR2(100) NOT NULL,
    description    VARCHAR2(500),
    images         VARCHAR2(1000),         -- 多张图片路径，逗号分隔
    status         VARCHAR2(20)  DEFAULT 'pending', -- pending待处理/approved已同意/rejected已拒绝/completed已完成
    refund_amount  NUMBER(10,2),           -- 申请退款金额
    reject_reason  VARCHAR2(300),          -- 拒绝原因
    handler_id     NUMBER,                 -- 处理的管理员ID
    handle_time    DATE,
    create_time    DATE          DEFAULT SYSDATE,
    CONSTRAINT fk_as_order FOREIGN KEY (order_id) REFERENCES ORDERS(order_id),
    CONSTRAINT fk_as_user  FOREIGN KEY (user_id)  REFERENCES USERS(user_id)
);
CREATE SEQUENCE seq_after_sale START WITH 1 INCREMENT BY 1;
CREATE INDEX idx_as_order  ON AFTER_SALES(order_id);
CREATE INDEX idx_as_status ON AFTER_SALES(status);
COMMENT ON TABLE AFTER_SALES IS '售后退款申请表';

-- ============================================================
-- 7. REVIEWS — 商品评价表
-- ============================================================
CREATE TABLE REVIEWS (
    review_id    NUMBER PRIMARY KEY,
    order_id     NUMBER        NOT NULL,
    order_item_id NUMBER,
    product_id   NUMBER        NOT NULL,
    user_id      NUMBER        NOT NULL,
    rating       NUMBER(1)     NOT NULL, -- 1-5星
    content      VARCHAR2(1000),
    images       VARCHAR2(1000),         -- 评价图片，逗号分隔
    tags         VARCHAR2(200),          -- 评价标签，逗号分隔
    is_anonymous NUMBER(1)    DEFAULT 0, -- 0实名/1匿名
    is_hidden    NUMBER(1)    DEFAULT 0, -- 0显示/1隐藏
    admin_reply  VARCHAR2(500),          -- 商家回复
    create_time  DATE         DEFAULT SYSDATE,
    CONSTRAINT fk_rev_product FOREIGN KEY (product_id) REFERENCES PRODUCTS(product_id),
    CONSTRAINT fk_rev_user    FOREIGN KEY (user_id)    REFERENCES USERS(user_id)
);
CREATE SEQUENCE seq_review START WITH 1 INCREMENT BY 1;
CREATE INDEX idx_review_product ON REVIEWS(product_id);
CREATE INDEX idx_review_user    ON REVIEWS(user_id);
COMMENT ON TABLE REVIEWS IS '商品评价表';

-- ============================================================
-- 8. COURIERS — 配送员表
-- ============================================================
CREATE TABLE COURIERS (
    courier_id    NUMBER PRIMARY KEY,
    courier_name  VARCHAR2(50)  NOT NULL,
    phone         VARCHAR2(20)  NOT NULL UNIQUE,
    password      VARCHAR2(100) NOT NULL,
    status        VARCHAR2(10)  DEFAULT 'offline', -- online在线/offline离线
    is_disabled   NUMBER(1)     DEFAULT 0,          -- 0正常/1禁用
    today_count   NUMBER        DEFAULT 0,           -- 今日已送单数
    total_count   NUMBER        DEFAULT 0,           -- 累计配送单数
    create_time   DATE          DEFAULT SYSDATE
);
CREATE SEQUENCE seq_courier START WITH 1 INCREMENT BY 1;
COMMENT ON TABLE COURIERS IS '配送员表';

-- ============================================================
-- 9. DELIVERY_TASKS — 配送任务表
-- ============================================================
CREATE TABLE DELIVERY_TASKS (
    task_id     NUMBER PRIMARY KEY,
    order_id    NUMBER       NOT NULL,
    courier_id  NUMBER       NOT NULL,
    status      VARCHAR2(20) DEFAULT 'pending',  -- pending待取件/picking配送中/done已送达/failed配送失败
    assign_time DATE         DEFAULT SYSDATE,
    pickup_time DATE,
    done_time   DATE,
    fail_reason VARCHAR2(200),
    CONSTRAINT fk_dt_order   FOREIGN KEY (order_id)   REFERENCES ORDERS(order_id),
    CONSTRAINT fk_dt_courier FOREIGN KEY (courier_id) REFERENCES COURIERS(courier_id)
);
CREATE SEQUENCE seq_delivery_task START WITH 1 INCREMENT BY 1;
CREATE INDEX idx_dt_courier ON DELIVERY_TASKS(courier_id);
CREATE INDEX idx_dt_status  ON DELIVERY_TASKS(status);
COMMENT ON TABLE DELIVERY_TASKS IS '配送任务表';

-- ============================================================
-- 10. MESSAGES — 站内消息通知表
-- ============================================================
CREATE TABLE MESSAGES (
    msg_id      NUMBER PRIMARY KEY,
    user_id     NUMBER        NOT NULL,
    title       VARCHAR2(100) NOT NULL,
    content     VARCHAR2(500) NOT NULL,
    msg_type    VARCHAR2(20)  DEFAULT 'system', -- order订单/promotion促销/system系统/refund退款
    is_read     NUMBER(1)     DEFAULT 0,         -- 0未读/1已读
    create_time DATE          DEFAULT SYSDATE,
    CONSTRAINT fk_msg_user FOREIGN KEY (user_id) REFERENCES USERS(user_id)
);
CREATE SEQUENCE seq_message START WITH 1 INCREMENT BY 1;
CREATE INDEX idx_msg_user   ON MESSAGES(user_id);
CREATE INDEX idx_msg_unread ON MESSAGES(user_id, is_read);
COMMENT ON TABLE MESSAGES IS '站内消息通知表';

-- ============================================================
-- 11. POINTS_LOGS — 积分流水表
-- ============================================================
CREATE TABLE POINTS_LOGS (
    log_id         NUMBER PRIMARY KEY,
    user_id        NUMBER        NOT NULL,
    change_amount  NUMBER        NOT NULL, -- 正数=增加，负数=消耗
    balance_after  NUMBER        NOT NULL, -- 变动后积分余额
    log_type       VARCHAR2(30)  NOT NULL, -- purchase购物获得/deduct消费抵扣/manual手动调整/refund退款回滚
    remark         VARCHAR2(200),
    ref_id         NUMBER,                 -- 关联订单ID等
    create_time    DATE          DEFAULT SYSDATE,
    CONSTRAINT fk_pl_user FOREIGN KEY (user_id) REFERENCES USERS(user_id)
);
CREATE SEQUENCE seq_points_log START WITH 1 INCREMENT BY 1;
CREATE INDEX idx_pl_user ON POINTS_LOGS(user_id);
COMMENT ON TABLE POINTS_LOGS IS '积分流水表';

-- ============================================================
-- 12. BANNERS — 首页Banner轮播图表
-- ============================================================
CREATE TABLE BANNERS (
    banner_id    NUMBER PRIMARY KEY,
    image_url    VARCHAR2(255) NOT NULL,
    link_type    VARCHAR2(20),  -- product商品/category分类/activity活动/none无跳转
    link_target  VARCHAR2(100), -- 跳转目标ID或URL
    sort_order   NUMBER DEFAULT 0,
    start_time   DATE,
    end_time     DATE,
    is_active    NUMBER(1) DEFAULT 1,
    create_time  DATE DEFAULT SYSDATE
);
CREATE SEQUENCE seq_banner START WITH 1 INCREMENT BY 1;
COMMENT ON TABLE BANNERS IS '首页Banner轮播图表';

-- ============================================================
-- 13. SECKILL_ACTIVITIES — 秒杀活动场次表
-- ============================================================
CREATE TABLE SECKILL_ACTIVITIES (
    seckill_id  NUMBER PRIMARY KEY,
    seckill_name VARCHAR2(100) NOT NULL,
    start_time  DATE          NOT NULL,
    end_time    DATE          NOT NULL,
    status      VARCHAR2(20)  DEFAULT 'pending', -- pending未开始/running进行中/paused已暂停/ended已结束
    create_time DATE          DEFAULT SYSDATE
);
CREATE SEQUENCE seq_seckill START WITH 1 INCREMENT BY 1;
COMMENT ON TABLE SECKILL_ACTIVITIES IS '秒杀活动场次表';

-- ============================================================
-- 14. SECKILL_PRODUCTS — 秒杀商品表
-- ============================================================
CREATE TABLE SECKILL_PRODUCTS (
    id             NUMBER PRIMARY KEY,
    seckill_id     NUMBER       NOT NULL,
    product_id     NUMBER       NOT NULL,
    seckill_price  NUMBER(10,2) NOT NULL,
    seckill_stock  NUMBER       NOT NULL,
    sold_count     NUMBER       DEFAULT 0,
    CONSTRAINT fk_sp_seckill FOREIGN KEY (seckill_id) REFERENCES SECKILL_ACTIVITIES(seckill_id),
    CONSTRAINT fk_sp_product FOREIGN KEY (product_id) REFERENCES PRODUCTS(product_id)
);
CREATE SEQUENCE seq_seckill_product START WITH 1 INCREMENT BY 1;
COMMENT ON TABLE SECKILL_PRODUCTS IS '秒杀场次商品表';

-- ============================================================
-- 15. PURCHASE_ORDER_ITEMS — 采购单明细表
-- ============================================================
CREATE TABLE PURCHASE_ORDER_ITEMS (
    item_id         NUMBER PRIMARY KEY,
    po_id           NUMBER       NOT NULL,
    product_id      NUMBER       NOT NULL,
    quantity        NUMBER       NOT NULL, -- 采购数量
    actual_quantity NUMBER       DEFAULT 0, -- 实际收货数量（到货时填写）
    unit_price      NUMBER(10,2) NOT NULL,
    CONSTRAINT fk_poi_po      FOREIGN KEY (po_id)      REFERENCES PURCHASE_ORDERS(po_id),
    CONSTRAINT fk_poi_product FOREIGN KEY (product_id) REFERENCES PRODUCTS(product_id)
);
CREATE SEQUENCE seq_po_item START WITH 1 INCREMENT BY 1;
CREATE INDEX idx_poi_po ON PURCHASE_ORDER_ITEMS(po_id);
COMMENT ON TABLE PURCHASE_ORDER_ITEMS IS '采购单商品明细表';

-- ============================================================
-- 16. STOCKTAKE_TASKS — 库存盘点任务表
-- ============================================================
CREATE TABLE STOCKTAKE_TASKS (
    task_id     NUMBER PRIMARY KEY,
    scope       VARCHAR2(20) DEFAULT 'all', -- all全部/category按分类
    category_id NUMBER,                      -- scope=category时指定分类
    status      VARCHAR2(20) DEFAULT 'pending', -- pending待盘点/counting盘点中/done已完成
    creator_id  NUMBER,
    create_time DATE DEFAULT SYSDATE,
    submit_time DATE
);
CREATE SEQUENCE seq_stocktake START WITH 1 INCREMENT BY 1;
COMMENT ON TABLE STOCKTAKE_TASKS IS '库存盘点任务表';

-- ============================================================
-- 17. STOCKTAKE_ITEMS — 盘点明细表
-- ============================================================
CREATE TABLE STOCKTAKE_ITEMS (
    id           NUMBER PRIMARY KEY,
    task_id      NUMBER NOT NULL,
    product_id   NUMBER NOT NULL,
    book_stock   NUMBER NOT NULL, -- 账面库存（任务创建时快照）
    actual_stock NUMBER,          -- 实盘数量
    difference   NUMBER,          -- 差异=实盘-账面
    diff_reason  VARCHAR2(200),   -- 差异原因
    CONSTRAINT fk_si_task    FOREIGN KEY (task_id)    REFERENCES STOCKTAKE_TASKS(task_id),
    CONSTRAINT fk_si_product FOREIGN KEY (product_id) REFERENCES PRODUCTS(product_id)
);
CREATE SEQUENCE seq_stocktake_item START WITH 1 INCREMENT BY 1;
CREATE INDEX idx_si_task ON STOCKTAKE_ITEMS(task_id);
COMMENT ON TABLE STOCKTAKE_ITEMS IS '盘点明细表';

-- ============================================================
-- 18. CASHIER_SHIFTS — 收银班次表
-- ============================================================
CREATE TABLE CASHIER_SHIFTS (
    shift_id       NUMBER PRIMARY KEY,
    cashier_id     NUMBER        NOT NULL, -- 操作员（ADMINS表）
    cash_start     NUMBER(10,2)  DEFAULT 0, -- 备用金
    cash_end       NUMBER(10,2),            -- 交班时清点现金
    total_orders   NUMBER        DEFAULT 0,
    cash_total     NUMBER(10,2)  DEFAULT 0, -- 现金收款合计
    sim_pay_total  NUMBER(10,2)  DEFAULT 0, -- 模拟支付合计
    start_time     DATE          DEFAULT SYSDATE,
    end_time       DATE,
    status         VARCHAR2(10)  DEFAULT 'open' -- open开班中/closed已交班
);
CREATE SEQUENCE seq_shift START WITH 1 INCREMENT BY 1;
COMMENT ON TABLE CASHIER_SHIFTS IS '收银班次表';

-- ============================================================
-- 19. PRODUCT_FAVORITES — 商品收藏表
-- ============================================================
CREATE TABLE PRODUCT_FAVORITES (
    id          NUMBER PRIMARY KEY,
    user_id     NUMBER NOT NULL,
    product_id  NUMBER NOT NULL,
    create_time DATE   DEFAULT SYSDATE,
    CONSTRAINT uq_fav UNIQUE (user_id, product_id),
    CONSTRAINT fk_fav_user    FOREIGN KEY (user_id)    REFERENCES USERS(user_id),
    CONSTRAINT fk_fav_product FOREIGN KEY (product_id) REFERENCES PRODUCTS(product_id)
);
CREATE SEQUENCE seq_favorite START WITH 1 INCREMENT BY 1;
CREATE INDEX idx_fav_user ON PRODUCT_FAVORITES(user_id);
COMMENT ON TABLE PRODUCT_FAVORITES IS '商品收藏表';

-- ============================================================
-- 20. DAMAGE_RECORDS — 报损记录表
-- ============================================================
CREATE TABLE DAMAGE_RECORDS (
    record_id   NUMBER PRIMARY KEY,
    product_id  NUMBER        NOT NULL,
    quantity    NUMBER        NOT NULL,
    reason      VARCHAR2(200) NOT NULL, -- 破损/过期/其他
    operator_id NUMBER,
    create_time DATE DEFAULT SYSDATE,
    CONSTRAINT fk_dr_product FOREIGN KEY (product_id) REFERENCES PRODUCTS(product_id)
);
CREATE SEQUENCE seq_damage START WITH 1 INCREMENT BY 1;
COMMENT ON TABLE DAMAGE_RECORDS IS '报损记录表';

COMMIT;
