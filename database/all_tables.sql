-- ============================================================
-- 超市管理系统 — 完整数据库脚本 v2.0
-- 数据库: Oracle XE  用户: system  密码: 123456
-- 执行方式: sqlplus system/123456@localhost:1521:XE @all_tables.sql
-- ============================================================

-- 关闭回显加速执行
SET FEEDBACK OFF
SET ECHO OFF

-- ============================================================
-- STEP 1: 清理旧表（按依赖顺序从叶到根删除）
-- ============================================================
DECLARE
  PROCEDURE drop_if_exists(p_name VARCHAR2) IS
  BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE ' || p_name || ' CASCADE CONSTRAINTS PURGE';
    DBMS_OUTPUT.PUT_LINE('已删除表: ' || p_name);
  EXCEPTION
    WHEN OTHERS THEN NULL;
  END;
BEGIN
  drop_if_exists('AUDIT_LOGS');
  drop_if_exists('CASHIER_RECORDS');
  drop_if_exists('CASHIER_SHIFTS');
  drop_if_exists('DAMAGE_RECORDS');
  drop_if_exists('INVENTORY_CHECK_ITEMS');
  drop_if_exists('INVENTORY_CHECKS');
  drop_if_exists('PURCHASE_ORDER_ITEMS');
  drop_if_exists('PURCHASE_ORDERS');
  drop_if_exists('INVENTORY_LOGS');
  drop_if_exists('DELIVERY_TASKS');
  drop_if_exists('DELIVERY_PERSONS');
  drop_if_exists('BANNERS');
  drop_if_exists('ACTIVITY_PRODUCTS');
  drop_if_exists('ACTIVITIES');
  drop_if_exists('USER_COUPONS');
  drop_if_exists('COUPONS');
  drop_if_exists('FAVORITES');
  drop_if_exists('POINTS_LOGS');
  drop_if_exists('MESSAGES');
  drop_if_exists('REVIEWS');
  drop_if_exists('AFTER_SALES');
  drop_if_exists('ORDER_ITEMS');
  drop_if_exists('ORDERS');
  drop_if_exists('CART');
  drop_if_exists('ADDRESSES');
  drop_if_exists('PRODUCT_IMAGES');
  drop_if_exists('PRODUCTS');
  drop_if_exists('SUPPLIERS');
  drop_if_exists('BRANDS');
  drop_if_exists('CATEGORIES');
  drop_if_exists('ADMIN_USERS');
  drop_if_exists('USERS');
END;
/

-- 清理旧序列
DECLARE
  PROCEDURE drop_seq(p_name VARCHAR2) IS
  BEGIN
    EXECUTE IMMEDIATE 'DROP SEQUENCE ' || p_name;
  EXCEPTION
    WHEN OTHERS THEN NULL;
  END;
BEGIN
  drop_seq('SEQ_USERS');
  drop_seq('SEQ_ADMIN_USERS');
  drop_seq('SEQ_CATEGORIES');
  drop_seq('SEQ_BRANDS');
  drop_seq('SEQ_SUPPLIERS');
  drop_seq('SEQ_PRODUCTS');
  drop_seq('SEQ_PRODUCT_IMAGES');
  drop_seq('SEQ_ADDRESSES');
  drop_seq('SEQ_CART');
  drop_seq('SEQ_ORDERS');
  drop_seq('SEQ_ORDER_ITEMS');
  drop_seq('SEQ_AFTER_SALES');
  drop_seq('SEQ_REVIEWS');
  drop_seq('SEQ_MESSAGES');
  drop_seq('SEQ_POINTS_LOGS');
  drop_seq('SEQ_FAVORITES');
  drop_seq('SEQ_COUPONS');
  drop_seq('SEQ_USER_COUPONS');
  drop_seq('SEQ_ACTIVITIES');
  drop_seq('SEQ_BANNERS');
  drop_seq('SEQ_INVENTORY_LOGS');
  drop_seq('SEQ_PURCHASE_ORDERS');
  drop_seq('SEQ_PO_ITEMS');
  drop_seq('SEQ_INVENTORY_CHECKS');
  drop_seq('SEQ_CHECK_ITEMS');
  drop_seq('SEQ_DAMAGE_RECORDS');
  drop_seq('SEQ_DELIVERY_PERSONS');
  drop_seq('SEQ_DELIVERY_TASKS');
  drop_seq('SEQ_CASHIER_SHIFTS');
  drop_seq('SEQ_CASHIER_RECORDS');
  drop_seq('SEQ_AUDIT_LOGS');
  -- 兼容旧序列名
  drop_seq('SEQ_USER');
  drop_seq('SEQ_CATEGORY');
  drop_seq('SEQ_PRODUCT');
  drop_seq('SEQ_ORDER');
  drop_seq('SEQ_ORDER_ITEM');
  drop_seq('SEQ_INVENTORY_LOG');
  drop_seq('SEQ_CART');
  drop_seq('SEQ_PROMOTION');
  drop_seq('SEQ_PROMO_PRODUCT');
  drop_seq('SEQ_DELIVERY');
  drop_seq('SEQ_PAYMENT');
  drop_seq('SEQ_WAREHOUSING');
  drop_seq('SEQ_OUTBOUND');
  drop_seq('SEQ_ADDRESS');
  drop_seq('SEQ_BEHAVIOR');
END;
/

PROMPT ============================================================
PROMPT STEP 2: 创建表结构
PROMPT ============================================================

-- ============================================================
-- 【1】用户表 (顾客端)
-- ============================================================
CREATE TABLE users (
    user_id       NUMBER          NOT NULL,
    username      VARCHAR2(50)    NOT NULL,          -- 手机号作为登录账号
    password      VARCHAR2(100)   NOT NULL,          -- BCrypt 加密
    nickname      VARCHAR2(50),
    real_name     VARCHAR2(50),
    gender        CHAR(1)         DEFAULT 'U',       -- M/F/U(未知)
    birthday      DATE,
    email         VARCHAR2(100),
    avatar_url    VARCHAR2(500),
    phone         VARCHAR2(20),
    member_level  VARCHAR2(20)    DEFAULT 'NORMAL',  -- NORMAL/SILVER/GOLD/DIAMOND
    points        NUMBER          DEFAULT 0,
    status        VARCHAR2(10)    DEFAULT 'active',  -- active/banned
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    update_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT chk_users_gender CHECK (gender IN ('M','F','U')),
    CONSTRAINT chk_users_level CHECK (member_level IN ('NORMAL','SILVER','GOLD','DIAMOND')),
    CONSTRAINT chk_users_status CHECK (status IN ('active','banned'))
);
CREATE SEQUENCE seq_users START WITH 1000 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【2】管理员用户表
-- ============================================================
CREATE TABLE admin_users (
    admin_id      NUMBER          NOT NULL,
    username      VARCHAR2(50)    NOT NULL,
    password      VARCHAR2(100)   NOT NULL,
    real_name     VARCHAR2(50),
    role          VARCHAR2(30)    NOT NULL,
    -- SUPER_ADMIN / MANAGER / PRODUCT / FINANCE / SERVICE / WAREHOUSE / CASHIER
    status        VARCHAR2(10)    DEFAULT 'active',
    last_login    TIMESTAMP,
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_admin_users PRIMARY KEY (admin_id),
    CONSTRAINT uq_admin_username UNIQUE (username),
    CONSTRAINT chk_admin_role CHECK (role IN ('SUPER_ADMIN','MANAGER','PRODUCT','FINANCE','SERVICE','WAREHOUSE','CASHIER')),
    CONSTRAINT chk_admin_status CHECK (status IN ('active','inactive'))
);
CREATE SEQUENCE seq_admin_users START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【3】商品分类表（支持二级：parent_id=0 为一级）
-- ============================================================
CREATE TABLE categories (
    category_id   NUMBER          NOT NULL,
    parent_id     NUMBER          DEFAULT 0,         -- 0=一级分类
    category_name VARCHAR2(50)    NOT NULL,
    icon_url      VARCHAR2(500),
    sort_order    NUMBER          DEFAULT 0,
    status        VARCHAR2(10)    DEFAULT 'active',
    description   VARCHAR2(200),
    CONSTRAINT pk_categories PRIMARY KEY (category_id),
    CONSTRAINT chk_cat_status CHECK (status IN ('active','inactive'))
);
CREATE SEQUENCE seq_categories START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【4】品牌表
-- ============================================================
CREATE TABLE brands (
    brand_id      NUMBER          NOT NULL,
    brand_name    VARCHAR2(100)   NOT NULL,
    logo_url      VARCHAR2(500),
    description   VARCHAR2(500),
    status        VARCHAR2(10)    DEFAULT 'active',
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_brands PRIMARY KEY (brand_id),
    CONSTRAINT chk_brands_status CHECK (status IN ('active','inactive'))
);
CREATE SEQUENCE seq_brands START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【5】供应商表
-- ============================================================
CREATE TABLE suppliers (
    supplier_id   NUMBER          NOT NULL,
    supplier_name VARCHAR2(100)   NOT NULL,
    contact_name  VARCHAR2(50),
    contact_phone VARCHAR2(20),
    address       VARCHAR2(200),
    status        VARCHAR2(10)    DEFAULT 'active',
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_suppliers PRIMARY KEY (supplier_id),
    CONSTRAINT chk_sup_status CHECK (status IN ('active','inactive'))
);
CREATE SEQUENCE seq_suppliers START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【6】商品主表
-- ============================================================
CREATE TABLE products (
    product_id    NUMBER          NOT NULL,
    product_name  VARCHAR2(100)   NOT NULL,
    category_id   NUMBER,
    brand_id      NUMBER,
    supplier_id   NUMBER,
    description   CLOB,
    cover_image   VARCHAR2(500),
    unit          VARCHAR2(20),                      -- 件/箱/kg/瓶/包
    original_price NUMBER(10,2)   NOT NULL,
    price         NUMBER(10,2)    NOT NULL,
    stock         NUMBER          DEFAULT 0,
    stock_warning NUMBER          DEFAULT 10,        -- 低库存预警阈值
    sales_count   NUMBER          DEFAULT 0,
    avg_rating    NUMBER(3,1)     DEFAULT 5.0,
    is_recommend  NUMBER(1)       DEFAULT 0,         -- 1=首页推荐
    status        VARCHAR2(10)    DEFAULT 'active',  -- active/off_shelf
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    update_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_products PRIMARY KEY (product_id),
    CONSTRAINT fk_prod_category FOREIGN KEY (category_id) REFERENCES categories(category_id),
    CONSTRAINT fk_prod_brand FOREIGN KEY (brand_id) REFERENCES brands(brand_id),
    CONSTRAINT fk_prod_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id),
    CONSTRAINT chk_prod_status CHECK (status IN ('active','off_shelf')),
    CONSTRAINT chk_prod_price CHECK (price >= 0),
    CONSTRAINT chk_prod_stock CHECK (stock >= 0)
);
CREATE SEQUENCE seq_products START WITH 1000 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【7】商品图片表（商品多图）
-- ============================================================
CREATE TABLE product_images (
    image_id      NUMBER          NOT NULL,
    product_id    NUMBER          NOT NULL,
    image_url     VARCHAR2(500)   NOT NULL,
    sort_order    NUMBER          DEFAULT 0,
    CONSTRAINT pk_product_images PRIMARY KEY (image_id),
    CONSTRAINT fk_img_product FOREIGN KEY (product_id) REFERENCES products(product_id)
);
CREATE SEQUENCE seq_product_images START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【8】收货地址表
-- ============================================================
CREATE TABLE addresses (
    address_id    NUMBER          NOT NULL,
    user_id       NUMBER          NOT NULL,
    receiver_name VARCHAR2(50)    NOT NULL,
    phone         VARCHAR2(20)    NOT NULL,
    province      VARCHAR2(50)    NOT NULL,
    city          VARCHAR2(50)    NOT NULL,
    district      VARCHAR2(50)    NOT NULL,
    detail        VARCHAR2(200)   NOT NULL,
    is_default    NUMBER(1)       DEFAULT 0,
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_addresses PRIMARY KEY (address_id),
    CONSTRAINT fk_addr_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT chk_addr_default CHECK (is_default IN (0,1))
);
CREATE SEQUENCE seq_addresses START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【9】购物车表
-- ============================================================
CREATE TABLE cart (
    cart_id       NUMBER          NOT NULL,
    user_id       NUMBER          NOT NULL,
    product_id    NUMBER          NOT NULL,
    quantity      NUMBER          DEFAULT 1,
    is_checked    NUMBER(1)       DEFAULT 1,          -- 1=勾选结算
    add_time      TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_cart PRIMARY KEY (cart_id),
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT uq_cart_user_product UNIQUE (user_id, product_id),
    CONSTRAINT chk_cart_qty CHECK (quantity >= 1),
    CONSTRAINT chk_cart_checked CHECK (is_checked IN (0,1))
);
CREATE SEQUENCE seq_cart START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【10】优惠券模板表
-- ============================================================
CREATE TABLE coupons (
    coupon_id     NUMBER          NOT NULL,
    coupon_name   VARCHAR2(100)   NOT NULL,
    coupon_type   VARCHAR2(20)    NOT NULL,           -- FULL_REDUCE/DISCOUNT/CATEGORY
    face_value    NUMBER(10,2),                       -- 满减金额 或 折扣(0.9=九折)
    min_amount    NUMBER(10,2)    DEFAULT 0,          -- 使用门槛
    category_id   NUMBER,                             -- 品类券限定分类(NULL=全场)
    total_count   NUMBER          DEFAULT -1,         -- -1=不限量
    issued_count  NUMBER          DEFAULT 0,
    start_time    TIMESTAMP       NOT NULL,
    end_time      TIMESTAMP       NOT NULL,
    status        VARCHAR2(10)    DEFAULT 'active',
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_coupons PRIMARY KEY (coupon_id),
    CONSTRAINT chk_coup_type CHECK (coupon_type IN ('FULL_REDUCE','DISCOUNT','CATEGORY')),
    CONSTRAINT chk_coup_status CHECK (status IN ('active','inactive'))
);
CREATE SEQUENCE seq_coupons START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【11】用户优惠券表
-- ============================================================
CREATE TABLE user_coupons (
    uc_id         NUMBER          NOT NULL,
    user_id       NUMBER          NOT NULL,
    coupon_id     NUMBER          NOT NULL,
    status        VARCHAR2(10)    DEFAULT 'unused',   -- unused/used/expired
    get_time      TIMESTAMP       DEFAULT SYSTIMESTAMP,
    use_time      TIMESTAMP,
    order_id      NUMBER,
    CONSTRAINT pk_user_coupons PRIMARY KEY (uc_id),
    CONSTRAINT fk_uc_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_uc_coupon FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id),
    CONSTRAINT chk_uc_status CHECK (status IN ('unused','used','expired'))
);
CREATE SEQUENCE seq_user_coupons START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【12】促销活动表
-- ============================================================
CREATE TABLE activities (
    activity_id   NUMBER          NOT NULL,
    activity_name VARCHAR2(100)   NOT NULL,
    activity_type VARCHAR2(20)    NOT NULL,           -- SECKILL/FULL_REDUCE
    rules         CLOB,                               -- JSON规则
    start_time    TIMESTAMP       NOT NULL,
    end_time      TIMESTAMP       NOT NULL,
    status        VARCHAR2(10)    DEFAULT 'active',
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_activities PRIMARY KEY (activity_id),
    CONSTRAINT chk_act_type CHECK (activity_type IN ('SECKILL','FULL_REDUCE')),
    CONSTRAINT chk_act_status CHECK (status IN ('active','inactive'))
);
CREATE SEQUENCE seq_activities START WITH 1 INCREMENT BY 1 NOCACHE;

-- 活动关联商品
CREATE TABLE activity_products (
    id            NUMBER          NOT NULL,
    activity_id   NUMBER          NOT NULL,
    product_id    NUMBER          NOT NULL,
    activity_price NUMBER(10,2),                      -- 秒杀价
    CONSTRAINT pk_activity_products PRIMARY KEY (id),
    CONSTRAINT fk_ap_activity FOREIGN KEY (activity_id) REFERENCES activities(activity_id),
    CONSTRAINT fk_ap_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT uq_ap UNIQUE (activity_id, product_id)
);
CREATE SEQUENCE seq_activity_products START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【13】首页轮播图表
-- ============================================================
CREATE TABLE banners (
    banner_id     NUMBER          NOT NULL,
    image_url     VARCHAR2(500)   NOT NULL,
    link_type     VARCHAR2(20)    DEFAULT 'NONE',     -- PRODUCT/CATEGORY/ACTIVITY/NONE
    link_id       NUMBER,
    sort_order    NUMBER          DEFAULT 0,
    status        VARCHAR2(10)    DEFAULT 'active',
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_banners PRIMARY KEY (banner_id),
    CONSTRAINT chk_banner_link CHECK (link_type IN ('PRODUCT','CATEGORY','ACTIVITY','NONE')),
    CONSTRAINT chk_banner_status CHECK (status IN ('active','inactive'))
);
CREATE SEQUENCE seq_banners START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【14】订单主表
-- ============================================================
CREATE TABLE orders (
    order_id           NUMBER          NOT NULL,
    order_no           VARCHAR2(30)    NOT NULL,      -- 业务单号 SM202603230001
    user_id            NUMBER          NOT NULL,
    address_id         NUMBER,
    total_amount       NUMBER(12,2)    NOT NULL,
    discount_amount    NUMBER(12,2)    DEFAULT 0,
    pay_amount         NUMBER(12,2)    NOT NULL,
    pay_method         VARCHAR2(20)    DEFAULT 'MOCK',
    coupon_id          NUMBER,
    points_used        NUMBER          DEFAULT 0,
    status             VARCHAR2(20)    DEFAULT 'PENDING_PAY',
    -- PENDING_PAY / PAID / PENDING_SHIP / SHIPPING / COMPLETED / CANCELLED
    delivery_person_id NUMBER,
    remark             VARCHAR2(500),
    pay_time           TIMESTAMP,
    ship_time          TIMESTAMP,
    complete_time      TIMESTAMP,
    cancel_time        TIMESTAMP,
    create_time        TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_orders PRIMARY KEY (order_id),
    CONSTRAINT uq_order_no UNIQUE (order_no),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_order_address FOREIGN KEY (address_id) REFERENCES addresses(address_id),
    CONSTRAINT chk_order_status CHECK (status IN ('PENDING_PAY','PAID','PENDING_SHIP','SHIPPING','COMPLETED','CANCELLED')),
    CONSTRAINT chk_order_pay CHECK (pay_method IN ('MOCK','CASH','MOCK_CARD'))
);
CREATE SEQUENCE seq_orders START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【15】订单明细表
-- ============================================================
CREATE TABLE order_items (
    item_id       NUMBER          NOT NULL,
    order_id      NUMBER          NOT NULL,
    product_id    NUMBER          NOT NULL,
    product_name  VARCHAR2(100)   NOT NULL,           -- 下单时价格/名称快照
    product_image VARCHAR2(500),
    unit_price    NUMBER(10,2)    NOT NULL,
    quantity      NUMBER          NOT NULL,
    subtotal      NUMBER(12,2)    NOT NULL,
    CONSTRAINT pk_order_items PRIMARY KEY (item_id),
    CONSTRAINT fk_oi_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_oi_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT chk_oi_qty CHECK (quantity >= 1)
);
CREATE SEQUENCE seq_order_items START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【16】售后申请表
-- ============================================================
CREATE TABLE after_sales (
    as_id         NUMBER          NOT NULL,
    order_id      NUMBER          NOT NULL,
    user_id       NUMBER          NOT NULL,
    as_type       VARCHAR2(20)    DEFAULT 'REFUND',   -- REFUND/EXCHANGE
    reason        VARCHAR2(500)   NOT NULL,
    status        VARCHAR2(20)    DEFAULT 'PENDING',  -- PENDING/APPROVED/REJECTED/COMPLETED
    refund_amount NUMBER(12,2),
    admin_remark  VARCHAR2(500),
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    handle_time   TIMESTAMP,
    CONSTRAINT pk_after_sales PRIMARY KEY (as_id),
    CONSTRAINT fk_as_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_as_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT chk_as_type CHECK (as_type IN ('REFUND','EXCHANGE')),
    CONSTRAINT chk_as_status CHECK (status IN ('PENDING','APPROVED','REJECTED','COMPLETED'))
);
CREATE SEQUENCE seq_after_sales START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【17】商品评价表
-- ============================================================
CREATE TABLE reviews (
    review_id     NUMBER          NOT NULL,
    order_id      NUMBER          NOT NULL,
    product_id    NUMBER          NOT NULL,
    user_id       NUMBER          NOT NULL,
    rating        NUMBER(1)       NOT NULL,           -- 1~5星
    content       VARCHAR2(500),
    images        VARCHAR2(2000),                     -- 图片URL逗号分隔
    is_anonymous  NUMBER(1)       DEFAULT 0,
    is_hidden     NUMBER(1)       DEFAULT 0,          -- 管理员隐藏
    reply         VARCHAR2(500),
    reply_time    TIMESTAMP,
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_reviews PRIMARY KEY (review_id),
    CONSTRAINT fk_rev_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_rev_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_rev_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT chk_rev_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT chk_rev_anon CHECK (is_anonymous IN (0,1)),
    CONSTRAINT chk_rev_hidden CHECK (is_hidden IN (0,1))
);
CREATE SEQUENCE seq_reviews START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【18】商品收藏表
-- ============================================================
CREATE TABLE favorites (
    fav_id        NUMBER          NOT NULL,
    user_id       NUMBER          NOT NULL,
    product_id    NUMBER          NOT NULL,
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_favorites PRIMARY KEY (fav_id),
    CONSTRAINT fk_fav_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_fav_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT uq_fav UNIQUE (user_id, product_id)
);
CREATE SEQUENCE seq_favorites START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【19】站内消息表
-- ============================================================
CREATE TABLE messages (
    message_id    NUMBER          NOT NULL,
    user_id       NUMBER          NOT NULL,
    title         VARCHAR2(100)   NOT NULL,
    content       VARCHAR2(500)   NOT NULL,
    msg_type      VARCHAR2(30)    DEFAULT 'SYSTEM',   -- SYSTEM/ORDER/COUPON/AFTER_SALES
    is_read       NUMBER(1)       DEFAULT 0,
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_messages PRIMARY KEY (message_id),
    CONSTRAINT fk_msg_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT chk_msg_type CHECK (msg_type IN ('SYSTEM','ORDER','COUPON','AFTER_SALES')),
    CONSTRAINT chk_msg_read CHECK (is_read IN (0,1))
);
CREATE SEQUENCE seq_messages START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【20】积分流水表
-- ============================================================
CREATE TABLE points_logs (
    log_id        NUMBER          NOT NULL,
    user_id       NUMBER          NOT NULL,
    change_amount NUMBER          NOT NULL,           -- 正=增加 负=扣减
    balance_after NUMBER          NOT NULL,
    reason        VARCHAR2(100)   NOT NULL,
    ref_id        NUMBER,                             -- 关联订单ID等
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_points_logs PRIMARY KEY (log_id),
    CONSTRAINT fk_pl_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);
CREATE SEQUENCE seq_points_logs START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【21】库存变动日志表
-- ============================================================
CREATE TABLE inventory_logs (
    log_id        NUMBER          NOT NULL,
    product_id    NUMBER          NOT NULL,
    change_amount NUMBER          NOT NULL,           -- 正=入库 负=出库/报损
    balance_after NUMBER          NOT NULL,
    log_type      VARCHAR2(20)    NOT NULL,
    -- PURCHASE_IN / ORDER_OUT / DAMAGE / CHECK_ADJUST / MANUAL
    ref_id        NUMBER,
    remark        VARCHAR2(200),
    operator_id   NUMBER,
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_inventory_logs PRIMARY KEY (log_id),
    CONSTRAINT fk_il_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT chk_il_type CHECK (log_type IN ('PURCHASE_IN','ORDER_OUT','DAMAGE','CHECK_ADJUST','MANUAL'))
);
CREATE SEQUENCE seq_inventory_logs START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【22】采购单表
-- ============================================================
CREATE TABLE purchase_orders (
    po_id         NUMBER          NOT NULL,
    po_no         VARCHAR2(30)    NOT NULL,
    supplier_id   NUMBER,
    total_amount  NUMBER(12,2)    NOT NULL,
    status        VARCHAR2(20)    DEFAULT 'DRAFT',
    -- DRAFT / SUBMITTED / PARTIALLY_ARRIVED / COMPLETED
    operator_id   NUMBER          NOT NULL,
    remark        VARCHAR2(500),
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    complete_time TIMESTAMP,
    CONSTRAINT pk_purchase_orders PRIMARY KEY (po_id),
    CONSTRAINT uq_po_no UNIQUE (po_no),
    CONSTRAINT fk_po_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id),
    CONSTRAINT chk_po_status CHECK (status IN ('DRAFT','SUBMITTED','PARTIALLY_ARRIVED','COMPLETED'))
);
CREATE SEQUENCE seq_purchase_orders START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【23】采购明细表
-- ============================================================
CREATE TABLE purchase_order_items (
    item_id          NUMBER          NOT NULL,
    po_id            NUMBER          NOT NULL,
    product_id       NUMBER          NOT NULL,
    order_quantity   NUMBER          NOT NULL,
    arrived_quantity NUMBER          DEFAULT 0,
    unit_price       NUMBER(10,2)    NOT NULL,
    CONSTRAINT pk_po_items PRIMARY KEY (item_id),
    CONSTRAINT fk_poi_po FOREIGN KEY (po_id) REFERENCES purchase_orders(po_id),
    CONSTRAINT fk_poi_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT chk_poi_qty CHECK (order_quantity >= 1)
);
CREATE SEQUENCE seq_po_items START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【24】库存盘点主表
-- ============================================================
CREATE TABLE inventory_checks (
    check_id      NUMBER          NOT NULL,
    check_no      VARCHAR2(30)    NOT NULL,
    status        VARCHAR2(20)    DEFAULT 'IN_PROGRESS',
    -- IN_PROGRESS / PENDING_APPROVE / COMPLETED
    operator_id   NUMBER          NOT NULL,
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    complete_time TIMESTAMP,
    CONSTRAINT pk_inventory_checks PRIMARY KEY (check_id),
    CONSTRAINT uq_check_no UNIQUE (check_no),
    CONSTRAINT chk_ic_status CHECK (status IN ('IN_PROGRESS','PENDING_APPROVE','COMPLETED'))
);
CREATE SEQUENCE seq_inventory_checks START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【25】库存盘点明细表
-- ============================================================
CREATE TABLE inventory_check_items (
    item_id          NUMBER          NOT NULL,
    check_id         NUMBER          NOT NULL,
    product_id       NUMBER          NOT NULL,
    system_quantity  NUMBER          NOT NULL,        -- 系统库存（盘点时快照）
    actual_quantity  NUMBER,                          -- 实际盘点数量
    difference       NUMBER,                          -- 差异=实际-系统
    CONSTRAINT pk_check_items PRIMARY KEY (item_id),
    CONSTRAINT fk_ci_check FOREIGN KEY (check_id) REFERENCES inventory_checks(check_id),
    CONSTRAINT fk_ci_product FOREIGN KEY (product_id) REFERENCES products(product_id)
);
CREATE SEQUENCE seq_check_items START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【26】报损记录表
-- ============================================================
CREATE TABLE damage_records (
    damage_id     NUMBER          NOT NULL,
    product_id    NUMBER          NOT NULL,
    quantity      NUMBER          NOT NULL,
    reason        VARCHAR2(200)   NOT NULL,           -- 过期/破损/其他
    operator_id   NUMBER          NOT NULL,
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_damage_records PRIMARY KEY (damage_id),
    CONSTRAINT fk_dr_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT chk_dr_qty CHECK (quantity >= 1)
);
CREATE SEQUENCE seq_damage_records START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【27】配送员表
-- ============================================================
CREATE TABLE delivery_persons (
    courier_id    NUMBER          NOT NULL,
    real_name     VARCHAR2(50)    NOT NULL,
    phone         VARCHAR2(20)    NOT NULL,
    password      VARCHAR2(100)   NOT NULL,
    status        VARCHAR2(10)    DEFAULT 'active',
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_delivery_persons PRIMARY KEY (courier_id),
    CONSTRAINT uq_courier_phone UNIQUE (phone),
    CONSTRAINT chk_courier_status CHECK (status IN ('active','inactive'))
);
CREATE SEQUENCE seq_delivery_persons START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【28】配送任务表
-- ============================================================
CREATE TABLE delivery_tasks (
    task_id       NUMBER          NOT NULL,
    order_id      NUMBER          NOT NULL,
    courier_id    NUMBER,
    status        VARCHAR2(20)    DEFAULT 'ASSIGNED',
    -- ASSIGNED / PICKED_UP / DELIVERED / FAILED
    assign_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    pickup_time   TIMESTAMP,
    deliver_time  TIMESTAMP,
    fail_reason   VARCHAR2(200),
    CONSTRAINT pk_delivery_tasks PRIMARY KEY (task_id),
    CONSTRAINT uq_task_order UNIQUE (order_id),
    CONSTRAINT fk_dt_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_dt_courier FOREIGN KEY (courier_id) REFERENCES delivery_persons(courier_id),
    CONSTRAINT chk_dt_status CHECK (status IN ('ASSIGNED','PICKED_UP','DELIVERED','FAILED'))
);
CREATE SEQUENCE seq_delivery_tasks START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【29】收银班次表
-- ============================================================
CREATE TABLE cashier_shifts (
    shift_id      NUMBER          NOT NULL,
    cashier_id    NUMBER          NOT NULL,
    start_time    TIMESTAMP       NOT NULL,
    end_time      TIMESTAMP,
    start_cash    NUMBER(10,2)    DEFAULT 0,
    end_cash      NUMBER(10,2),
    status        VARCHAR2(10)    DEFAULT 'OPEN',     -- OPEN/CLOSED
    CONSTRAINT pk_cashier_shifts PRIMARY KEY (shift_id),
    CONSTRAINT fk_cs_cashier FOREIGN KEY (cashier_id) REFERENCES admin_users(admin_id),
    CONSTRAINT chk_cs_status CHECK (status IN ('OPEN','CLOSED'))
);
CREATE SEQUENCE seq_cashier_shifts START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【30】收银记录表
-- ============================================================
CREATE TABLE cashier_records (
    record_id      NUMBER          NOT NULL,
    shift_id       NUMBER          NOT NULL,
    user_id        NUMBER,                            -- 会员可为空
    total_amount   NUMBER(12,2)    NOT NULL,
    discount_amount NUMBER(12,2)   DEFAULT 0,
    pay_amount     NUMBER(12,2)    NOT NULL,
    pay_method     VARCHAR2(20)    DEFAULT 'CASH',    -- CASH/MOCK_CARD
    cashier_id     NUMBER          NOT NULL,
    create_time    TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_cashier_records PRIMARY KEY (record_id),
    CONSTRAINT fk_cr_shift FOREIGN KEY (shift_id) REFERENCES cashier_shifts(shift_id),
    CONSTRAINT fk_cr_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_cr_cashier FOREIGN KEY (cashier_id) REFERENCES admin_users(admin_id),
    CONSTRAINT chk_cr_pay CHECK (pay_method IN ('CASH','MOCK_CARD'))
);
CREATE SEQUENCE seq_cashier_records START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【31】操作审计日志表
-- ============================================================
CREATE TABLE audit_logs (
    log_id        NUMBER          NOT NULL,
    operator_id   NUMBER          NOT NULL,
    operator_name VARCHAR2(50)    NOT NULL,
    module        VARCHAR2(50)    NOT NULL,
    action        VARCHAR2(50)    NOT NULL,           -- CREATE/UPDATE/DELETE
    target_id     NUMBER,
    before_data   CLOB,
    after_data    CLOB,
    ip_address    VARCHAR2(50),
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_audit_logs PRIMARY KEY (log_id)
);
CREATE SEQUENCE seq_audit_logs START WITH 1 INCREMENT BY 1 NOCACHE;

PROMPT ============================================================
PROMPT STEP 3: 创建索引（提升查询性能）
PROMPT ============================================================

-- 商品相关
CREATE INDEX idx_prod_category   ON products(category_id);
CREATE INDEX idx_prod_brand      ON products(brand_id);
CREATE INDEX idx_prod_status     ON products(status);
CREATE INDEX idx_prod_name       ON products(product_name);
CREATE INDEX idx_prod_recommend  ON products(is_recommend);

-- 订单相关
CREATE INDEX idx_order_user      ON orders(user_id);
CREATE INDEX idx_order_status    ON orders(status);
CREATE INDEX idx_order_time      ON orders(create_time);
CREATE INDEX idx_order_no        ON orders(order_no);

-- 购物车
CREATE INDEX idx_cart_user       ON cart(user_id);

-- 库存日志
CREATE INDEX idx_il_product      ON inventory_logs(product_id);
CREATE INDEX idx_il_time         ON inventory_logs(create_time);

-- 消息
CREATE INDEX idx_msg_user_read   ON messages(user_id, is_read);

-- 评价
CREATE INDEX idx_rev_product     ON reviews(product_id);

-- 积分
CREATE INDEX idx_pl_user         ON points_logs(user_id);

-- 收藏
CREATE INDEX idx_fav_user        ON favorites(user_id);

PROMPT ============================================================
PROMPT STEP 4: 初始化基础数据
PROMPT ============================================================

-- ① 管理员账户（密码均为明文，后端登录时需改为BCrypt验证或测试用明文对比）
-- SUPER_ADMIN: admin / admin123
INSERT INTO admin_users (admin_id, username, password, real_name, role, status)
VALUES (seq_admin_users.NEXTVAL, 'admin', 'admin123', '系统管理员', 'SUPER_ADMIN', 'active');

-- MANAGER: manager / manager123
INSERT INTO admin_users (admin_id, username, password, real_name, role, status)
VALUES (seq_admin_users.NEXTVAL, 'manager', 'manager123', '店长', 'MANAGER', 'active');

-- CASHIER: cashier01 / cashier123
INSERT INTO admin_users (admin_id, username, password, real_name, role, status)
VALUES (seq_admin_users.NEXTVAL, 'cashier01', 'cashier123', '收银员小王', 'CASHIER', 'active');

-- WAREHOUSE: warehouse01 / warehouse123
INSERT INTO admin_users (admin_id, username, password, real_name, role, status)
VALUES (seq_admin_users.NEXTVAL, 'warehouse01', 'warehouse123', '仓管小李', 'WAREHOUSE', 'active');

-- ② 配送员
INSERT INTO delivery_persons (courier_id, real_name, phone, password, status)
VALUES (seq_delivery_persons.NEXTVAL, '张配送', '13900000001', 'courier123', 'active');

INSERT INTO delivery_persons (courier_id, real_name, phone, password, status)
VALUES (seq_delivery_persons.NEXTVAL, '李配送', '13900000002', 'courier123', 'active');

-- ③ 顾客用户
INSERT INTO users (user_id, username, password, nickname, real_name, phone, member_level, points, status)
VALUES (seq_users.NEXTVAL, '13800138001', 'user123', '小明', '张三', '13800138001', 'SILVER', 520, 'active');

INSERT INTO users (user_id, username, password, nickname, real_name, phone, member_level, points, status)
VALUES (seq_users.NEXTVAL, '13800138002', 'user123', '小红', '李四', '13800138002', 'NORMAL', 0, 'active');

-- ④ 一级分类
INSERT INTO categories (category_id, parent_id, category_name, sort_order, status, description)
VALUES (seq_categories.NEXTVAL, 0, '食品', 1, 'active', '各类食品零食');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status, description)
VALUES (seq_categories.NEXTVAL, 0, '饮料', 2, 'active', '各类饮品');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status, description)
VALUES (seq_categories.NEXTVAL, 0, '日用品', 3, 'active', '生活日用品');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status, description)
VALUES (seq_categories.NEXTVAL, 0, '生鲜', 4, 'active', '新鲜蔬果肉蛋');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status, description)
VALUES (seq_categories.NEXTVAL, 0, '酒水', 5, 'active', '啤酒白酒红酒');

-- ⑤ 二级分类（基于上方一级分类ID，Oracle Sequence从1开始，故分类ID为1~5）
INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 1, '饼干糕点', 1, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 1, '方便速食', 2, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 2, '碳酸饮料', 1, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 2, '矿泉水', 2, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 3, '洗护用品', 1, 'active');

-- ⑥ 品牌
INSERT INTO brands (brand_id, brand_name, status)
VALUES (seq_brands.NEXTVAL, '可口可乐', 'active');
INSERT INTO brands (brand_id, brand_name, status)
VALUES (seq_brands.NEXTVAL, '农夫山泉', 'active');
INSERT INTO brands (brand_id, brand_name, status)
VALUES (seq_brands.NEXTVAL, '康师傅', 'active');
INSERT INTO brands (brand_id, brand_name, status)
VALUES (seq_brands.NEXTVAL, '统一', 'active');
INSERT INTO brands (brand_id, brand_name, status)
VALUES (seq_brands.NEXTVAL, '海飞丝', 'active');
INSERT INTO brands (brand_id, brand_name, status)
VALUES (seq_brands.NEXTVAL, '高露洁', 'active');

-- ⑦ 供应商
INSERT INTO suppliers (supplier_id, supplier_name, contact_name, contact_phone, status)
VALUES (seq_suppliers.NEXTVAL, '可口可乐公司', '王经理', '020-88888881', 'active');
INSERT INTO suppliers (supplier_id, supplier_name, contact_name, contact_phone, status)
VALUES (seq_suppliers.NEXTVAL, '农夫山泉股份', '李经理', '0571-88888882', 'active');
INSERT INTO suppliers (supplier_id, supplier_name, contact_name, contact_phone, status)
VALUES (seq_suppliers.NEXTVAL, '顶益食品有限公司', '张经理', '022-88888883', 'active');

-- ⑧ 商品（含饮料/食品/日用品各品类）
INSERT INTO products (product_id, product_name, category_id, brand_id, supplier_id, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, '可口可乐 330ml', 8, 1, 1, 4.00, 3.50, 500, 50, '罐', 9999, 4.8, 1, 'active');

INSERT INTO products (product_id, product_name, category_id, brand_id, supplier_id, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, '农夫山泉 550ml', 9, 2, 2, 2.50, 2.00, 1000, 100, '瓶', 8888, 4.9, 1, 'active');

INSERT INTO products (product_id, product_name, category_id, brand_id, supplier_id, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, '康师傅方便面 红烧牛肉', 7, 3, 3, 5.50, 4.50, 300, 30, '包', 5000, 4.7, 1, 'active');

INSERT INTO products (product_id, product_name, category_id, brand_id, supplier_id, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, '统一冰红茶 500ml', 8, 4, 2, 3.50, 3.00, 400, 40, '瓶', 3000, 4.6, 0, 'active');

INSERT INTO products (product_id, product_name, category_id, brand_id, supplier_id, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, '海飞丝洗发水 400ml', 10, 5, 1, 35.00, 29.90, 150, 20, '瓶', 1200, 4.5, 0, 'active');

INSERT INTO products (product_id, product_name, category_id, brand_id, supplier_id, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, '高露洁牙膏 120g', 10, 6, 1, 15.00, 12.00, 200, 20, '支', 2000, 4.7, 0, 'active');

INSERT INTO products (product_id, product_name, category_id, brand_id, supplier_id, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, '奥利奥饼干 388g', 6, 1, 1, 20.00, 16.90, 250, 25, '盒', 4500, 4.8, 1, 'active');

INSERT INTO products (product_id, product_name, category_id, brand_id, supplier_id, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, '乐事薯片 原味 104g', 6, 1, 1, 8.00, 6.50, 300, 30, '包', 6000, 4.6, 0, 'active');

-- ⑨ 轮播图
INSERT INTO banners (banner_id, image_url, link_type, sort_order, status)
VALUES (seq_banners.NEXTVAL, '/uploads/banner/banner1.jpg', 'NONE', 1, 'active');
INSERT INTO banners (banner_id, image_url, link_type, sort_order, status)
VALUES (seq_banners.NEXTVAL, '/uploads/banner/banner2.jpg', 'NONE', 2, 'active');

-- ⑩ 示例优惠券
INSERT INTO coupons (coupon_id, coupon_name, coupon_type, face_value, min_amount, total_count, start_time, end_time, status)
VALUES (seq_coupons.NEXTVAL, '新人满50减10', 'FULL_REDUCE', 10.00, 50.00, 1000,
        SYSTIMESTAMP, SYSTIMESTAMP + INTERVAL '30' DAY, 'active');

INSERT INTO coupons (coupon_id, coupon_name, coupon_type, face_value, min_amount, total_count, start_time, end_time, status)
VALUES (seq_coupons.NEXTVAL, '全场九折券', 'DISCOUNT', 0.9, 0.00, -1,
        SYSTIMESTAMP, SYSTIMESTAMP + INTERVAL '7' DAY, 'active');

COMMIT;

PROMPT ============================================================
PROMPT STEP 5: 验证建表结果
PROMPT ============================================================

SELECT
    table_name                          AS "表名",
    num_rows                            AS "行数(统计值)"
FROM user_tables
WHERE table_name IN (
    'USERS','ADMIN_USERS','CATEGORIES','BRANDS','SUPPLIERS',
    'PRODUCTS','PRODUCT_IMAGES','ADDRESSES','CART',
    'COUPONS','USER_COUPONS','ACTIVITIES','ACTIVITY_PRODUCTS','BANNERS',
    'ORDERS','ORDER_ITEMS','AFTER_SALES','REVIEWS','FAVORITES',
    'MESSAGES','POINTS_LOGS','INVENTORY_LOGS',
    'PURCHASE_ORDERS','PURCHASE_ORDER_ITEMS',
    'INVENTORY_CHECKS','INVENTORY_CHECK_ITEMS','DAMAGE_RECORDS',
    'DELIVERY_PERSONS','DELIVERY_TASKS',
    'CASHIER_SHIFTS','CASHIER_RECORDS','AUDIT_LOGS'
)
ORDER BY table_name;

-- 统计实际行数
SELECT '已初始化数据验证:' AS INFO FROM DUAL;
SELECT 'admin_users(管理员): ' || COUNT(*) FROM admin_users;
SELECT 'users(顾客): '        || COUNT(*) FROM users;
SELECT 'categories(分类): '   || COUNT(*) FROM categories;
SELECT 'brands(品牌): '       || COUNT(*) FROM brands;
SELECT 'suppliers(供应商): '  || COUNT(*) FROM suppliers;
SELECT 'products(商品): '     || COUNT(*) FROM products;
SELECT 'coupons(优惠券): '    || COUNT(*) FROM coupons;
SELECT 'banners(轮播图): '    || COUNT(*) FROM banners;
SELECT 'delivery_persons(配送员): ' || COUNT(*) FROM delivery_persons;

PROMPT ============================================================
PROMPT 全部完成！共创建 32 张表 + 32 条序列
PROMPT ============================================================
