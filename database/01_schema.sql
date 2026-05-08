-- ============================================================
-- 超市管理系统数据库 Schema v3.0
-- 文件: 01_schema.sql，请先阅读 00_README.sql 了解整体说明
-- 数据库: Oracle XE  连接用户: system  密码: Liu12345
-- 执行方式: sqlplus system/Liu12345@127.0.0.1:1521:ORCL @01_schema.sql
-- 创建时间: v3.0 (共 34 张表 + 34 个序列, 约 162 个字段)
-- 主要变更记录:
--   新增 product_skus 支持多规格SKU管理 (C-18)
--   新增 order_status_logs 订单状态变更日志 (C-42/B-17)
--   新增 cashier_record_items 收银明细表 (K-06/K-07)
--   新增 full_reduce_rules 满减规则表 (B-35)
--   新增 products 表 barcode/cost_price/deleted 字段
--   新增 orders 表 source/delivery_time_slot/express_*/refund* 字段
--   新增 after_sales 表 images/item_id/as_no 字段
--   新增 purchase_orders 表 expected_arrive_time/approved_by/approved_time
--   新增 cashier_shifts 表交接班相关字段
--   新增 cashier_records 表 received_amount/change_amount/coupon_id/items
--   新增 suppliers 表 email/bank_account/payment_days
--   新增 banners 表 start_time/end_time 定时上下线
--   新增 coupons 表 per_limit 单用户限领/description 描述
--   新增 reviews 表 tags 评价标签
--   新增 messages 表 ref_id 关联业务ID
--   新增 damage_records 表 damage_no/unit_cost/total_cost
--   新增 admin_users 表 phone 字段
--   新增 inventory_check_items 表 remark 备注
-- ============================================================

SET FEEDBACK OFF
SET ECHO OFF

-- ============================================================
-- STEP 1: 清理旧表（按依赖关系反向删除，避免外键约束）
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
  drop_if_exists('FULL_REDUCE_RULES');
  drop_if_exists('CASHIER_RECORD_ITEMS');
  drop_if_exists('ORDER_STATUS_LOGS');
  drop_if_exists('PRODUCT_SKUS');
  drop_if_exists('AUDIT_LOGS');
  drop_if_exists('CASHIER_RECORD_ITEMS');
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
  drop_if_exists('FULL_REDUCE_RULES');
  drop_if_exists('ACTIVITY_PRODUCTS');
  drop_if_exists('ACTIVITIES');
  drop_if_exists('USER_COUPONS');
  drop_if_exists('COUPONS');
  drop_if_exists('FAVORITES');
  drop_if_exists('POINTS_LOGS');
  drop_if_exists('MESSAGES');
  drop_if_exists('REVIEWS');
  drop_if_exists('AFTER_SALES');
  drop_if_exists('ORDER_STATUS_LOGS');
  drop_if_exists('ORDER_ITEMS');
  drop_if_exists('ORDERS');
  drop_if_exists('CART');
  drop_if_exists('ADDRESSES');
  drop_if_exists('PRODUCT_IMAGES');
  drop_if_exists('PRODUCT_SKUS');
  drop_if_exists('PRODUCTS');
  drop_if_exists('SUPPLIERS');
  drop_if_exists('BRANDS');
  drop_if_exists('CATEGORIES');
  drop_if_exists('ADMIN_USERS');
  drop_if_exists('USERS');
END;
/

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
  drop_seq('SEQ_PRODUCT_SKUS');
  drop_seq('SEQ_PRODUCT_IMAGES');
  drop_seq('SEQ_ADDRESSES');
  drop_seq('SEQ_CART');
  drop_seq('SEQ_ORDERS');
  drop_seq('SEQ_ORDER_ITEMS');
  drop_seq('SEQ_ORDER_STATUS_LOGS');
  drop_seq('SEQ_AFTER_SALES');
  drop_seq('SEQ_REVIEWS');
  drop_seq('SEQ_MESSAGES');
  drop_seq('SEQ_POINTS_LOGS');
  drop_seq('SEQ_FAVORITES');
  drop_seq('SEQ_COUPONS');
  drop_seq('SEQ_USER_COUPONS');
  drop_seq('SEQ_ACTIVITIES');
  drop_seq('SEQ_ACTIVITY_PRODUCTS');
  drop_seq('SEQ_FULL_REDUCE_RULES');
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
  drop_seq('SEQ_CASHIER_RECORD_ITEMS');
  drop_seq('SEQ_AUDIT_LOGS');
  drop_seq('SEQ_USER');
  drop_seq('SEQ_CATEGORY');
  drop_seq('SEQ_PRODUCT');
  drop_seq('SEQ_ORDER');
  drop_seq('SEQ_ORDER_ITEM');
  drop_seq('SEQ_INVENTORY_LOG');
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
PROMPT STEP 2: 创建数据表 (v3.0)
PROMPT ============================================================

-- ============================================================
-- 表1：会员用户表
-- 关联页面: C-01~C-06
-- ============================================================
CREATE TABLE users (
    user_id       NUMBER          NOT NULL,
    username      VARCHAR2(50)    NOT NULL,
    password      VARCHAR2(100)   NOT NULL,
    nickname      VARCHAR2(50),
    real_name     VARCHAR2(50),
    gender        CHAR(1)         DEFAULT 'U',
    birthday      DATE,
    email         VARCHAR2(100),
    avatar_url    VARCHAR2(500),
    phone         VARCHAR2(20),
    member_level  VARCHAR2(10)    DEFAULT 'NORMAL',
    points        NUMBER          DEFAULT 0,
    total_consume NUMBER(12,2)    DEFAULT 0,
    order_count   NUMBER          DEFAULT 0,
    last_order_time TIMESTAMP,
    ban_reason    VARCHAR2(200),
    status        VARCHAR2(10)    DEFAULT 'active',
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    update_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT chk_users_gender CHECK (gender IN ('M','F','U')),
    CONSTRAINT chk_users_level CHECK (member_level IN ('NORMAL','SILVER','GOLD','DIAMOND')),
    CONSTRAINT chk_users_status CHECK (status IN ('active','banned')),
    CONSTRAINT chk_users_points CHECK (points >= 0)
);
CREATE SEQUENCE seq_users START WITH 1000 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表2：管理员用户表
-- 关联页面: B-01~B-06
-- ============================================================
CREATE TABLE admin_users (
    admin_id      NUMBER          NOT NULL,
    username      VARCHAR2(50)    NOT NULL,
    password      VARCHAR2(100)   NOT NULL,
    real_name     VARCHAR2(50),
    phone         VARCHAR2(20),
    role          VARCHAR2(20)    NOT NULL,
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
-- 表3：商品分类表（支持二级分类，parent_id=0 表示一级分类）
-- 关联页面: B-13, C-12
-- ============================================================
CREATE TABLE categories (
    category_id   NUMBER          NOT NULL,
    parent_id     NUMBER          DEFAULT 0,
    category_name VARCHAR2(50)    NOT NULL,
    icon_url      VARCHAR2(500),
    sort_order    NUMBER          DEFAULT 0,
    status        VARCHAR2(10)    DEFAULT 'active',
    description   VARCHAR2(200),
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_categories PRIMARY KEY (category_id),
    CONSTRAINT chk_cat_status CHECK (status IN ('active','inactive'))
);
CREATE SEQUENCE seq_categories START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表4：品牌表
-- 关联页面: B-14, C-16
-- ============================================================
CREATE TABLE brands (
    brand_id      NUMBER          NOT NULL,
    brand_name    VARCHAR2(100)   NOT NULL,
    logo_url      VARCHAR2(500),
    description   VARCHAR2(500),
    product_count NUMBER          DEFAULT 0,
    status        VARCHAR2(10)    DEFAULT 'active',
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_brands PRIMARY KEY (brand_id),
    CONSTRAINT uq_brand_name UNIQUE (brand_name),
    CONSTRAINT chk_brands_status CHECK (status IN ('active','inactive'))
);
CREATE SEQUENCE seq_brands START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表5：供应商表
-- 关联页面: B-15
-- ============================================================
CREATE TABLE suppliers (
    supplier_id   NUMBER          NOT NULL,
    supplier_name VARCHAR2(100)   NOT NULL,
    contact_name  VARCHAR2(50),
    contact_phone VARCHAR2(20),
    email         VARCHAR2(100),
    address       VARCHAR2(200),
    bank_account  VARCHAR2(100),
    payment_days  NUMBER          DEFAULT 30,
    status        VARCHAR2(10)    DEFAULT 'active',
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    update_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_suppliers PRIMARY KEY (supplier_id),
    CONSTRAINT uq_sup_name UNIQUE (supplier_name),
    CONSTRAINT chk_sup_status CHECK (status IN ('active','inactive')),
    CONSTRAINT chk_sup_payment_days CHECK (payment_days >= 0)
);
CREATE SEQUENCE seq_suppliers START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表6：商品表
-- 关联页面: B-07~B-12, C-13~C-21, K-05
-- ============================================================
CREATE TABLE products (
    product_id    NUMBER          NOT NULL,
    product_name  VARCHAR2(100)   NOT NULL,
    barcode       VARCHAR2(50),
    category_id   NUMBER,
    brand_id      NUMBER,
    supplier_id   NUMBER,
    description   CLOB,
    cover_image   VARCHAR2(500),
    unit          VARCHAR2(20),
    cost_price    NUMBER(10,2)    DEFAULT 0,
    original_price NUMBER(10,2)  NOT NULL,
    price         NUMBER(10,2)   NOT NULL,
    stock         NUMBER         DEFAULT 0,
    stock_warning NUMBER         DEFAULT 10,
    sales_count   NUMBER         DEFAULT 0,
    avg_rating    NUMBER(3,1)    DEFAULT 5.0,
    review_count  NUMBER         DEFAULT 0,
    is_recommend  NUMBER(1)      DEFAULT 0,
    has_sku       NUMBER(1)      DEFAULT 0,
    status        VARCHAR2(10)   DEFAULT 'active',
    is_deleted    NUMBER(1)      DEFAULT 0,
    create_time   TIMESTAMP      DEFAULT SYSTIMESTAMP,
    update_time   TIMESTAMP      DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_products PRIMARY KEY (product_id),
    CONSTRAINT fk_prod_category FOREIGN KEY (category_id) REFERENCES categories(category_id),
    CONSTRAINT fk_prod_brand FOREIGN KEY (brand_id) REFERENCES brands(brand_id),
    CONSTRAINT fk_prod_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id),
    CONSTRAINT chk_prod_status CHECK (status IN ('active','off_shelf')),
    CONSTRAINT chk_prod_price CHECK (price >= 0),
    CONSTRAINT chk_prod_stock CHECK (stock >= 0),
    CONSTRAINT chk_prod_deleted CHECK (is_deleted IN (0,1)),
    CONSTRAINT chk_prod_recommend CHECK (is_recommend IN (0,1)),
    CONSTRAINT chk_prod_has_sku CHECK (has_sku IN (0,1))
);
CREATE SEQUENCE seq_products START WITH 1000 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表7：商品SKU规格表
-- 关联页面: C-18
-- ============================================================
CREATE TABLE product_skus (
    sku_id        NUMBER          NOT NULL,
    product_id    NUMBER          NOT NULL,
    sku_name      VARCHAR2(100)   NOT NULL,
    sku_spec      VARCHAR2(200),
    price         NUMBER(10,2)    NOT NULL,
    original_price NUMBER(10,2),
    cost_price    NUMBER(10,2)    DEFAULT 0,
    stock         NUMBER          DEFAULT 0,
    barcode       VARCHAR2(50),
    sort_order    NUMBER          DEFAULT 0,
    status        VARCHAR2(10)    DEFAULT 'active',
    CONSTRAINT pk_product_skus PRIMARY KEY (sku_id),
    CONSTRAINT fk_sku_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT chk_sku_price CHECK (price >= 0),
    CONSTRAINT chk_sku_stock CHECK (stock >= 0),
    CONSTRAINT chk_sku_status CHECK (status IN ('active','inactive'))
);
CREATE SEQUENCE seq_product_skus START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表8：商品图片表
-- 关联页面: C-17
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
-- 表9：收货地址表
-- 关联页面: C-29~C-33
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
    update_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_addresses PRIMARY KEY (address_id),
    CONSTRAINT fk_addr_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT chk_addr_default CHECK (is_default IN (0,1))
);
CREATE SEQUENCE seq_addresses START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表10：购物车表
-- 关联页面: C-22~C-28
-- ============================================================
CREATE TABLE cart (
    cart_id       NUMBER          NOT NULL,
    user_id       NUMBER          NOT NULL,
    product_id    NUMBER          NOT NULL,
    sku_id        NUMBER,
    quantity      NUMBER          DEFAULT 1,
    is_checked    NUMBER(1)       DEFAULT 1,
    add_time      TIMESTAMP       DEFAULT SYSTIMESTAMP,
    update_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_cart PRIMARY KEY (cart_id),
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_cart_sku FOREIGN KEY (sku_id) REFERENCES product_skus(sku_id),
    CONSTRAINT uq_cart_user_prod_sku UNIQUE (user_id, product_id, sku_id),
    CONSTRAINT chk_cart_qty CHECK (quantity >= 1),
    CONSTRAINT chk_cart_checked CHECK (is_checked IN (0,1))
);
CREATE SEQUENCE seq_cart START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表11：优惠券表
-- 关联页面: B-31~B-33, C-50~C-52
-- ============================================================
CREATE TABLE coupons (
    coupon_id     NUMBER          NOT NULL,
    coupon_name   VARCHAR2(100)   NOT NULL,
    description   VARCHAR2(200),
    coupon_type   VARCHAR2(20)    NOT NULL,
    face_value    NUMBER(10,2)    NOT NULL,
    min_amount    NUMBER(10,2)    DEFAULT 0,
    category_id   NUMBER,
    total_count   NUMBER          DEFAULT -1,
    issued_count  NUMBER          DEFAULT 0,
    used_count    NUMBER          DEFAULT 0,
    per_limit     NUMBER          DEFAULT 1,
    start_time    TIMESTAMP       NOT NULL,
    end_time      TIMESTAMP       NOT NULL,
    status        VARCHAR2(10)    DEFAULT 'active',
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_coupons PRIMARY KEY (coupon_id),
    CONSTRAINT fk_coup_category FOREIGN KEY (category_id) REFERENCES categories(category_id),
    CONSTRAINT chk_coup_type CHECK (coupon_type IN ('FULL_REDUCE','DISCOUNT','CATEGORY')),
    CONSTRAINT chk_coup_status CHECK (status IN ('active','inactive')),
    CONSTRAINT chk_coup_face_value CHECK (face_value > 0)
);
CREATE SEQUENCE seq_coupons START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表12：用户优惠券表
-- 关联页面: C-51~C-52
-- ============================================================
CREATE TABLE user_coupons (
    uc_id         NUMBER          NOT NULL,
    user_id       NUMBER          NOT NULL,
    coupon_id     NUMBER          NOT NULL,
    status        VARCHAR2(10)    DEFAULT 'unused',
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
-- 表13：活动表
-- 关联页面: B-34, B-35, C-11
-- ============================================================
CREATE TABLE activities (
    activity_id   NUMBER          NOT NULL,
    activity_name VARCHAR2(100)   NOT NULL,
    activity_type VARCHAR2(20)    NOT NULL,
    scope_type    VARCHAR2(20)    DEFAULT 'ALL',
    scope_category_id NUMBER,
    seckill_stock NUMBER          DEFAULT 0,
    start_time    TIMESTAMP       NOT NULL,
    end_time      TIMESTAMP       NOT NULL,
    status        VARCHAR2(10)    DEFAULT 'active',
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_activities PRIMARY KEY (activity_id),
    CONSTRAINT chk_act_type CHECK (activity_type IN ('SECKILL','FULL_REDUCE','DISCOUNT')),
    CONSTRAINT chk_act_status CHECK (status IN ('active','inactive')),
    CONSTRAINT chk_act_scope CHECK (scope_type IN ('ALL','CATEGORY'))
);
CREATE SEQUENCE seq_activities START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表13-1：满减规则表
-- 关联页面: B-35
-- ============================================================
CREATE TABLE full_reduce_rules (
    rule_id       NUMBER          NOT NULL,
    activity_id   NUMBER          NOT NULL,
    threshold     NUMBER(10,2)    NOT NULL,
    reduce_amount NUMBER(10,2)    NOT NULL,
    sort_order    NUMBER          DEFAULT 0,
    CONSTRAINT pk_full_reduce_rules PRIMARY KEY (rule_id),
    CONSTRAINT fk_frr_activity FOREIGN KEY (activity_id) REFERENCES activities(activity_id),
    CONSTRAINT chk_frr_threshold CHECK (threshold > 0),
    CONSTRAINT chk_frr_reduce CHECK (reduce_amount > 0)
);
CREATE SEQUENCE seq_full_reduce_rules START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表13-2：活动商品表
-- 关联页面: B-34
-- ============================================================
CREATE TABLE activity_products (
    id              NUMBER          NOT NULL,
    activity_id     NUMBER          NOT NULL,
    product_id      NUMBER          NOT NULL,
    sku_id          NUMBER,
    activity_price  NUMBER(10,2)    NOT NULL,
    activity_stock  NUMBER          DEFAULT 0,
    sold_count      NUMBER          DEFAULT 0,
    CONSTRAINT pk_activity_products PRIMARY KEY (id),
    CONSTRAINT fk_ap_activity FOREIGN KEY (activity_id) REFERENCES activities(activity_id),
    CONSTRAINT fk_ap_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_ap_sku FOREIGN KEY (sku_id) REFERENCES product_skus(sku_id),
    CONSTRAINT uq_ap UNIQUE (activity_id, product_id, sku_id),
    CONSTRAINT chk_ap_price CHECK (activity_price >= 0),
    CONSTRAINT chk_ap_stock CHECK (activity_stock >= 0)
);
CREATE SEQUENCE seq_activity_products START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表14：轮播图表
-- 关联页面: B-36, C-07
-- ============================================================
CREATE TABLE banners (
    banner_id     NUMBER          NOT NULL,
    image_url     VARCHAR2(500)   NOT NULL,
    title         VARCHAR2(100),
    link_type     VARCHAR2(20)    DEFAULT 'NONE',
    link_id       NUMBER,
    sort_order    NUMBER          DEFAULT 0,
    start_time    TIMESTAMP,
    end_time      TIMESTAMP,
    status        VARCHAR2(10)    DEFAULT 'active',
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_banners PRIMARY KEY (banner_id),
    CONSTRAINT chk_banner_link CHECK (link_type IN ('PRODUCT','CATEGORY','ACTIVITY','NONE')),
    CONSTRAINT chk_banner_status CHECK (status IN ('active','inactive'))
);
CREATE SEQUENCE seq_banners START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表15：订单表
-- 关联页面: C-34~C-49, B-16~B-21, P-04~P-09
-- ============================================================
CREATE TABLE orders (
    order_id             NUMBER          NOT NULL,
    order_no             VARCHAR2(30)    NOT NULL,
    user_id              NUMBER          NOT NULL,
    source               VARCHAR2(10)    DEFAULT 'ONLINE',
    address_id           NUMBER,
    receiver_snapshot    VARCHAR2(500),
    total_amount         NUMBER(12,2)    NOT NULL,
    discount_amount      NUMBER(12,2)    DEFAULT 0,
    coupon_discount      NUMBER(12,2)    DEFAULT 0,
    points_deduct_amount NUMBER(12,2)    DEFAULT 0,
    freight_amount       NUMBER(12,2)    DEFAULT 0,
    pay_amount           NUMBER(12,2)    NOT NULL,
    pay_method           VARCHAR2(20)    DEFAULT 'MOCK',
    coupon_id            NUMBER,
    uc_id                NUMBER,
    points_used          NUMBER          DEFAULT 0,
    delivery_time_slot   VARCHAR2(50),
    express_company      VARCHAR2(50),
    express_no           VARCHAR2(50),
    remark               VARCHAR2(500),
    cancel_reason        VARCHAR2(200),
    refund_amount        NUMBER(12,2)    DEFAULT 0,
    status               VARCHAR2(20)   DEFAULT 'PENDING_PAY',
    pay_time             TIMESTAMP,
    ship_time            TIMESTAMP,
    pickup_time          TIMESTAMP,
    deliver_time         TIMESTAMP,
    confirm_time         TIMESTAMP,
    complete_time        TIMESTAMP,
    cancel_time          TIMESTAMP,
    refund_time          TIMESTAMP,
    create_time          TIMESTAMP       DEFAULT SYSTIMESTAMP,
    update_time          TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_orders PRIMARY KEY (order_id),
    CONSTRAINT uq_order_no UNIQUE (order_no),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_order_address FOREIGN KEY (address_id) REFERENCES addresses(address_id),
    CONSTRAINT fk_order_coupon FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id),
    CONSTRAINT chk_order_status CHECK (status IN ('PENDING_PAY','PAID','PENDING_SHIP','SHIPPING','PENDING_RECEIVED','COMPLETED','CANCELLED','REFUNDED')),
    CONSTRAINT chk_order_source CHECK (source IN ('ONLINE','CASHIER')),
    CONSTRAINT chk_order_pay CHECK (pay_method IN ('MOCK','CASH','MOCK_CARD','WECHAT','ALIPAY'))
);
CREATE SEQUENCE seq_orders START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表16：订单商品明细表
-- 关联页面: C-42, B-17
-- ============================================================
CREATE TABLE order_items (
    item_id       NUMBER          NOT NULL,
    order_id      NUMBER          NOT NULL,
    product_id    NUMBER          NOT NULL,
    sku_id        NUMBER,
    product_name  VARCHAR2(100)   NOT NULL,
    sku_name      VARCHAR2(100),
    product_image VARCHAR2(500),
    unit_price    NUMBER(10,2)    NOT NULL,
    cost_price    NUMBER(10,2)    DEFAULT 0,
    quantity      NUMBER          NOT NULL,
    subtotal      NUMBER(12,2)    NOT NULL,
    CONSTRAINT pk_order_items PRIMARY KEY (item_id),
    CONSTRAINT fk_oi_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_oi_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT chk_oi_qty CHECK (quantity >= 1),
    CONSTRAINT chk_oi_price CHECK (unit_price >= 0)
);
CREATE SEQUENCE seq_order_items START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表17：订单状态变更日志表
-- 关联页面: C-42, B-17
-- ============================================================
CREATE TABLE order_status_logs (
    log_id        NUMBER          NOT NULL,
    order_id      NUMBER          NOT NULL,
    from_status   VARCHAR2(20),
    to_status     VARCHAR2(20)    NOT NULL,
    operator_type VARCHAR2(10)    DEFAULT 'USER',
    operator_id   NUMBER,
    operator_name VARCHAR2(50),
    remark        VARCHAR2(200),
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_order_status_logs PRIMARY KEY (log_id),
    CONSTRAINT fk_osl_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT chk_osl_op_type CHECK (operator_type IN ('USER','ADMIN','SYSTEM','COURIER'))
);
CREATE SEQUENCE seq_order_status_logs START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表18：售后申请表
-- 关联页面: C-47~C-48, B-22~B-25
-- ============================================================
CREATE TABLE after_sales (
    as_id         NUMBER          NOT NULL,
    as_no         VARCHAR2(30)    NOT NULL,
    order_id      NUMBER          NOT NULL,
    item_id       NUMBER,
    user_id       NUMBER          NOT NULL,
    as_type       VARCHAR2(20)    DEFAULT 'REFUND',
    reason        VARCHAR2(500)   NOT NULL,
    images        VARCHAR2(2000),
    refund_amount NUMBER(12,2),
    status        VARCHAR2(20)    DEFAULT 'PENDING',
    admin_remark  VARCHAR2(500),
    handler_id    NUMBER,
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    handle_time   TIMESTAMP,
    CONSTRAINT pk_after_sales PRIMARY KEY (as_id),
    CONSTRAINT uq_as_no UNIQUE (as_no),
    CONSTRAINT fk_as_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_as_item FOREIGN KEY (item_id) REFERENCES order_items(item_id),
    CONSTRAINT fk_as_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_as_handler FOREIGN KEY (handler_id) REFERENCES admin_users(admin_id),
    CONSTRAINT chk_as_type CHECK (as_type IN ('REFUND','EXCHANGE','RETURN')),
    CONSTRAINT chk_as_status CHECK (status IN ('PENDING','APPROVED','REJECTED','COMPLETED'))
);
CREATE SEQUENCE seq_after_sales START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表19：商品评价表
-- 关联页面: C-20, C-49, B-37~B-39
-- ============================================================
CREATE TABLE reviews (
    review_id     NUMBER          NOT NULL,
    order_id      NUMBER          NOT NULL,
    order_item_id NUMBER          NOT NULL,
    product_id    NUMBER          NOT NULL,
    user_id       NUMBER          NOT NULL,
    rating        NUMBER(1)       NOT NULL,
    content       VARCHAR2(1000),
    images        VARCHAR2(2000),
    tags          VARCHAR2(500),
    is_anonymous  NUMBER(1)       DEFAULT 0,
    is_hidden     NUMBER(1)       DEFAULT 0,
    reply         VARCHAR2(500),
    reply_time    TIMESTAMP,
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_reviews PRIMARY KEY (review_id),
    CONSTRAINT fk_rev_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_rev_order_item FOREIGN KEY (order_item_id) REFERENCES order_items(item_id),
    CONSTRAINT fk_rev_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_rev_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT uq_rev_order_item UNIQUE (order_item_id),
    CONSTRAINT chk_rev_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT chk_rev_anon CHECK (is_anonymous IN (0,1)),
    CONSTRAINT chk_rev_hidden CHECK (is_hidden IN (0,1))
);
CREATE SEQUENCE seq_reviews START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表20：商品收藏表
-- 关联页面: C-53~C-54
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
-- 表21：消息通知表
-- 关联页面: C-55~C-56
-- ============================================================
CREATE TABLE messages (
    message_id    NUMBER          NOT NULL,
    user_id       NUMBER          NOT NULL,
    title         VARCHAR2(100)   NOT NULL,
    content       VARCHAR2(1000)  NOT NULL,
    msg_type      VARCHAR2(20)    DEFAULT 'SYSTEM',
    ref_id        NUMBER,
    is_read       NUMBER(1)       DEFAULT 0,
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_messages PRIMARY KEY (message_id),
    CONSTRAINT fk_msg_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT chk_msg_type CHECK (msg_type IN ('SYSTEM','ORDER','COUPON','AFTER_SALES')),
    CONSTRAINT chk_msg_read CHECK (is_read IN (0,1))
);
CREATE SEQUENCE seq_messages START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表22：积分变动日志表
-- 关联页面: C-57, B-29
-- ============================================================
CREATE TABLE points_logs (
    log_id        NUMBER          NOT NULL,
    user_id       NUMBER          NOT NULL,
    change_amount NUMBER          NOT NULL,
    balance_after NUMBER          NOT NULL,
    reason        VARCHAR2(100)   NOT NULL,
    ref_id        NUMBER,
    operator_id   NUMBER,
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_points_logs PRIMARY KEY (log_id),
    CONSTRAINT fk_pl_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_pl_operator FOREIGN KEY (operator_id) REFERENCES admin_users(admin_id)
);
CREATE SEQUENCE seq_points_logs START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表23：库存变动日志表
-- 关联页面: W-08
-- ============================================================
CREATE TABLE inventory_logs (
    log_id        NUMBER          NOT NULL,
    product_id    NUMBER          NOT NULL,
    sku_id        NUMBER,
    change_amount NUMBER          NOT NULL,
    balance_after NUMBER          NOT NULL,
    log_type      VARCHAR2(20)    NOT NULL,
    ref_id        NUMBER,
    remark        VARCHAR2(200),
    operator_id   NUMBER,
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_inventory_logs PRIMARY KEY (log_id),
    CONSTRAINT fk_il_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_il_sku FOREIGN KEY (sku_id) REFERENCES product_skus(sku_id),
    CONSTRAINT fk_il_operator FOREIGN KEY (operator_id) REFERENCES admin_users(admin_id),
    CONSTRAINT chk_il_type CHECK (log_type IN ('PURCHASE_IN','ORDER_OUT','DAMAGE','CHECK_ADJUST','MANUAL'))
);
CREATE SEQUENCE seq_inventory_logs START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表24：采购单表
-- 关联页面: W-01~W-05
-- ============================================================
CREATE TABLE purchase_orders (
    po_id               NUMBER          NOT NULL,
    po_no               VARCHAR2(30)    NOT NULL,
    supplier_id         NUMBER,
    total_amount        NUMBER(12,2)    NOT NULL,
    status              VARCHAR2(20)    DEFAULT 'DRAFT',
    expected_arrive_time TIMESTAMP,
    operator_id         NUMBER          NOT NULL,
    approved_by         NUMBER,
    approved_time       TIMESTAMP,
    remark              VARCHAR2(500),
    create_time         TIMESTAMP       DEFAULT SYSTIMESTAMP,
    complete_time       TIMESTAMP,
    CONSTRAINT pk_purchase_orders PRIMARY KEY (po_id),
    CONSTRAINT uq_po_no UNIQUE (po_no),
    CONSTRAINT fk_po_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id),
    CONSTRAINT fk_po_operator FOREIGN KEY (operator_id) REFERENCES admin_users(admin_id),
    CONSTRAINT fk_po_approved_by FOREIGN KEY (approved_by) REFERENCES admin_users(admin_id),
    CONSTRAINT chk_po_status CHECK (status IN ('DRAFT','SUBMITTED','APPROVED','PARTIALLY_ARRIVED','COMPLETED','CANCELLED'))
);
CREATE SEQUENCE seq_purchase_orders START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表25：采购单明细表
-- 关联页面: W-02, W-04
-- ============================================================
CREATE TABLE purchase_order_items (
    item_id          NUMBER          NOT NULL,
    po_id            NUMBER          NOT NULL,
    product_id       NUMBER          NOT NULL,
    sku_id           NUMBER,
    order_quantity   NUMBER          NOT NULL,
    arrived_quantity NUMBER          DEFAULT 0,
    unit_price       NUMBER(10,2)    NOT NULL,
    subtotal         NUMBER(12,2)    NOT NULL,
    CONSTRAINT pk_po_items PRIMARY KEY (item_id),
    CONSTRAINT fk_poi_po FOREIGN KEY (po_id) REFERENCES purchase_orders(po_id),
    CONSTRAINT fk_poi_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_poi_sku FOREIGN KEY (sku_id) REFERENCES product_skus(sku_id),
    CONSTRAINT chk_poi_qty CHECK (order_quantity >= 1),
    CONSTRAINT chk_poi_arrived CHECK (arrived_quantity >= 0)
);
CREATE SEQUENCE seq_po_items START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表26：库存盘点表
-- 关联页面: W-10~W-14
-- ============================================================
CREATE TABLE inventory_checks (
    check_id      NUMBER          NOT NULL,
    check_no      VARCHAR2(30)    NOT NULL,
    check_scope   VARCHAR2(20)    DEFAULT 'ALL',
    scope_category_id NUMBER,
    status        VARCHAR2(20)    DEFAULT 'IN_PROGRESS',
    operator_id   NUMBER          NOT NULL,
    approved_by   NUMBER,
    approved_time TIMESTAMP,
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    complete_time TIMESTAMP,
    CONSTRAINT pk_inventory_checks PRIMARY KEY (check_id),
    CONSTRAINT uq_check_no UNIQUE (check_no),
    CONSTRAINT fk_ic_operator FOREIGN KEY (operator_id) REFERENCES admin_users(admin_id),
    CONSTRAINT fk_ic_approved_by FOREIGN KEY (approved_by) REFERENCES admin_users(admin_id),
    CONSTRAINT chk_ic_status CHECK (status IN ('IN_PROGRESS','PENDING_APPROVE','COMPLETED')),
    CONSTRAINT chk_ic_scope CHECK (check_scope IN ('ALL','CATEGORY'))
);
CREATE SEQUENCE seq_inventory_checks START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表27：盘点明细表
-- 关联页面: W-12~W-13
-- ============================================================
CREATE TABLE inventory_check_items (
    item_id          NUMBER          NOT NULL,
    check_id         NUMBER          NOT NULL,
    product_id       NUMBER          NOT NULL,
    sku_id           NUMBER,
    system_quantity  NUMBER          NOT NULL,
    actual_quantity  NUMBER,
    difference       NUMBER GENERATED ALWAYS AS (actual_quantity - system_quantity) VIRTUAL,
    remark           VARCHAR2(200),
    CONSTRAINT pk_check_items PRIMARY KEY (item_id),
    CONSTRAINT fk_ci_check FOREIGN KEY (check_id) REFERENCES inventory_checks(check_id),
    CONSTRAINT fk_ci_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_ci_sku FOREIGN KEY (sku_id) REFERENCES product_skus(sku_id)
);
CREATE SEQUENCE seq_check_items START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表28：报损记录表
-- 关联页面: W-09, D-17
-- ============================================================
CREATE TABLE damage_records (
    damage_id     NUMBER          NOT NULL,
    damage_no     VARCHAR2(30)    NOT NULL,
    product_id    NUMBER          NOT NULL,
    sku_id        NUMBER,
    quantity      NUMBER          NOT NULL,
    unit_cost     NUMBER(10,2)    DEFAULT 0,
    total_cost    NUMBER(12,2)    DEFAULT 0,
    reason        VARCHAR2(200)   NOT NULL,
    operator_id   NUMBER          NOT NULL,
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_damage_records PRIMARY KEY (damage_id),
    CONSTRAINT uq_damage_no UNIQUE (damage_no),
    CONSTRAINT fk_dr_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_dr_sku FOREIGN KEY (sku_id) REFERENCES product_skus(sku_id),
    CONSTRAINT fk_dr_operator FOREIGN KEY (operator_id) REFERENCES admin_users(admin_id),
    CONSTRAINT chk_dr_qty CHECK (quantity >= 1)
);
CREATE SEQUENCE seq_damage_records START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表29：配送员表
-- 关联页面: B-40~B-43, P-01~P-03
-- ============================================================
CREATE TABLE delivery_persons (
    courier_id          NUMBER          NOT NULL,
    real_name           VARCHAR2(50)    NOT NULL,
    phone               VARCHAR2(20)    NOT NULL,
    password            VARCHAR2(100)   NOT NULL,
    total_delivery_count NUMBER         DEFAULT 0,
    status              VARCHAR2(10)    DEFAULT 'active',
    create_time         TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_delivery_persons PRIMARY KEY (courier_id),
    CONSTRAINT uq_courier_phone UNIQUE (phone),
    CONSTRAINT chk_courier_status CHECK (status IN ('active','inactive'))
);
CREATE SEQUENCE seq_delivery_persons START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表30：配送任务表
-- 关联页面: B-18, B-43, P-04~P-09
-- ============================================================
CREATE TABLE delivery_tasks (
    task_id           NUMBER          NOT NULL,
    order_id          NUMBER          NOT NULL,
    courier_id        NUMBER,
    status            VARCHAR2(20)    DEFAULT 'ASSIGNED',
    fail_reason       VARCHAR2(200),
    assign_time       TIMESTAMP       DEFAULT SYSTIMESTAMP,
    pickup_time       TIMESTAMP,
    deliver_time      TIMESTAMP,
    CONSTRAINT pk_delivery_tasks PRIMARY KEY (task_id),
    CONSTRAINT uq_task_order UNIQUE (order_id),
    CONSTRAINT fk_dt_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_dt_courier FOREIGN KEY (courier_id) REFERENCES delivery_persons(courier_id),
    CONSTRAINT chk_dt_status CHECK (status IN ('ASSIGNED','PICKED_UP','DELIVERED','FAILED'))
);
CREATE SEQUENCE seq_delivery_tasks START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表31：收银员交接班表
-- 关联页面: K-01~K-04
-- ============================================================
CREATE TABLE cashier_shifts (
    shift_id          NUMBER          NOT NULL,
    cashier_id        NUMBER          NOT NULL,
    start_cash        NUMBER(10,2)    DEFAULT 0,
    end_cash          NUMBER(10,2),
    total_cash_amount NUMBER(12,2)    DEFAULT 0,
    total_mock_amount NUMBER(12,2)    DEFAULT 0,
    total_order_count NUMBER          DEFAULT 0,
    cash_diff         NUMBER(12,2),
    start_time        TIMESTAMP       NOT NULL,
    end_time          TIMESTAMP,
    status            VARCHAR2(10)    DEFAULT 'OPEN',
    CONSTRAINT pk_cashier_shifts PRIMARY KEY (shift_id),
    CONSTRAINT fk_cs_cashier FOREIGN KEY (cashier_id) REFERENCES admin_users(admin_id),
    CONSTRAINT chk_cs_status CHECK (status IN ('OPEN','CLOSED'))
);
CREATE SEQUENCE seq_cashier_shifts START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表32：收银记录表
-- 关联页面: K-10~K-12
-- ============================================================
CREATE TABLE cashier_records (
    record_id         NUMBER          NOT NULL,
    shift_id          NUMBER          NOT NULL,
    user_id           NUMBER,
    member_phone      VARCHAR2(20),
    total_amount      NUMBER(12,2)    NOT NULL,
    discount_amount   NUMBER(12,2)    DEFAULT 0,
    coupon_id         NUMBER,
    uc_id             NUMBER,
    pay_amount        NUMBER(12,2)    NOT NULL,
    pay_method        VARCHAR2(20)    DEFAULT 'CASH',
    received_amount   NUMBER(12,2),
    change_amount     NUMBER(12,2)    DEFAULT 0,
    cashier_id        NUMBER          NOT NULL,
    create_time       TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_cashier_records PRIMARY KEY (record_id),
    CONSTRAINT fk_cr_shift FOREIGN KEY (shift_id) REFERENCES cashier_shifts(shift_id),
    CONSTRAINT fk_cr_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_cr_cashier FOREIGN KEY (cashier_id) REFERENCES admin_users(admin_id),
    CONSTRAINT fk_cr_coupon FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id),
    CONSTRAINT chk_cr_pay CHECK (pay_method IN ('CASH','WECHAT','ALIPAY','MEMBER_CARD'))
);
CREATE SEQUENCE seq_cashier_records START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表33：收银明细表
-- 关联页面: K-06/K-07
-- ============================================================
CREATE TABLE cashier_record_items (
    item_id        NUMBER          NOT NULL,
    record_id      NUMBER          NOT NULL,
    product_id     NUMBER          NOT NULL,
    sku_id         NUMBER,
    product_name   VARCHAR2(100)   NOT NULL,
    sku_name       VARCHAR2(100),
    unit_price     NUMBER(10,2)    NOT NULL,
    quantity       NUMBER          NOT NULL,
    subtotal       NUMBER(12,2)    NOT NULL,
    CONSTRAINT pk_cashier_record_items PRIMARY KEY (item_id),
    CONSTRAINT fk_cri_record FOREIGN KEY (record_id) REFERENCES cashier_records(record_id),
    CONSTRAINT fk_cri_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_cri_sku FOREIGN KEY (sku_id) REFERENCES product_skus(sku_id),
    CONSTRAINT chk_cri_qty CHECK (quantity >= 1)
);
CREATE SEQUENCE seq_cashier_record_items START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 表34：操作审计日志表
-- 关联页面: B-44
-- ============================================================
CREATE TABLE audit_logs (
    log_id        NUMBER          NOT NULL,
    admin_id      NUMBER          NOT NULL,
    admin_name    VARCHAR2(50)    NOT NULL,
    module        VARCHAR2(50)    NOT NULL,
    action        VARCHAR2(20)    NOT NULL,
    target_table  VARCHAR2(50),
    target_id     NUMBER,
    before_data   CLOB,
    after_data    CLOB,
    ip_address    VARCHAR2(50),
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_audit_logs PRIMARY KEY (log_id),
    CONSTRAINT fk_al_admin FOREIGN KEY (admin_id) REFERENCES admin_users(admin_id),
    CONSTRAINT chk_al_action CHECK (action IN ('CREATE','UPDATE','DELETE','STATUS_CHANGE'))
);
CREATE SEQUENCE seq_audit_logs START WITH 1 INCREMENT BY 1 NOCACHE;

PROMPT ============================================================
PROMPT STEP 3: 创建索引
PROMPT ============================================================

CREATE INDEX idx_prod_category    ON products(category_id);
CREATE INDEX idx_prod_brand       ON products(brand_id);
CREATE INDEX idx_prod_supplier    ON products(supplier_id);
CREATE INDEX idx_prod_status      ON products(status, is_deleted);
CREATE INDEX idx_prod_name        ON products(product_name);
CREATE INDEX idx_prod_barcode     ON products(barcode);
CREATE INDEX idx_prod_recommend   ON products(is_recommend);
CREATE INDEX idx_prod_stock_warn  ON products(stock, stock_warning);
CREATE INDEX idx_sku_product      ON product_skus(product_id);
CREATE INDEX idx_order_user       ON orders(user_id);
CREATE INDEX idx_order_status     ON orders(status);
CREATE INDEX idx_order_time       ON orders(create_time);
CREATE INDEX idx_order_source     ON orders(source);
CREATE INDEX idx_order_pay_time   ON orders(pay_time);
CREATE INDEX idx_oi_order         ON order_items(order_id);
CREATE INDEX idx_oi_product       ON order_items(product_id);
CREATE INDEX idx_osl_order        ON order_status_logs(order_id);
CREATE INDEX idx_as_order         ON after_sales(order_id);
CREATE INDEX idx_as_status        ON after_sales(status);
CREATE INDEX idx_cart_user        ON cart(user_id);
CREATE INDEX idx_il_product       ON inventory_logs(product_id);
CREATE INDEX idx_il_type_time     ON inventory_logs(log_type, create_time);
CREATE INDEX idx_msg_user_read    ON messages(user_id, is_read);
CREATE INDEX idx_rev_product      ON reviews(product_id, is_hidden);
CREATE INDEX idx_pl_user          ON points_logs(user_id);
CREATE INDEX idx_fav_user         ON favorites(user_id);
CREATE INDEX idx_uc_user_status   ON user_coupons(user_id, status);
CREATE INDEX idx_uc_coupon        ON user_coupons(coupon_id);
CREATE INDEX idx_po_supplier      ON purchase_orders(supplier_id);
CREATE INDEX idx_po_status        ON purchase_orders(status);
CREATE INDEX idx_poi_po           ON purchase_order_items(po_id);
CREATE INDEX idx_ci_check         ON inventory_check_items(check_id);
CREATE INDEX idx_dr_product       ON damage_records(product_id);
CREATE INDEX idx_dr_time          ON damage_records(create_time);
CREATE INDEX idx_dt_courier       ON delivery_tasks(courier_id);
CREATE INDEX idx_dt_status        ON delivery_tasks(status);
CREATE INDEX idx_cr_shift         ON cashier_records(shift_id);
CREATE INDEX idx_cri_record       ON cashier_record_items(record_id);
CREATE INDEX idx_al_admin         ON audit_logs(admin_id);
CREATE INDEX idx_al_module_time   ON audit_logs(module, create_time);
CREATE INDEX idx_act_type_status  ON activities(activity_type, status, start_time, end_time);
CREATE INDEX idx_ap_activity      ON activity_products(activity_id);
CREATE INDEX idx_frr_activity     ON full_reduce_rules(activity_id);

PROMPT ============================================================
PROMPT STEP 4: 初始数据
PROMPT ============================================================

-- 管理员用户 (密码占位符，运行时由应用替换为BCrypt哈希)
INSERT INTO admin_users (admin_id, username, password, real_name, phone, role, status)
VALUES (seq_admin_users.NEXTVAL, 'admin', '$2a$10$PLACEHOLDER_FOR_BCRYPT_ADMIN123', '超级管理员', '13000000001', 'SUPER_ADMIN', 'active');

INSERT INTO admin_users (admin_id, username, password, real_name, phone, role, status)
VALUES (seq_admin_users.NEXTVAL, 'manager', '$2a$10$PLACEHOLDER_FOR_BCRYPT_ADMIN123', '店长', '13000000002', 'MANAGER', 'active');

INSERT INTO admin_users (admin_id, username, password, real_name, phone, role, status)
VALUES (seq_admin_users.NEXTVAL, 'cashier01', '$2a$10$PLACEHOLDER_FOR_BCRYPT_ADMIN123', '收银员01', '13000000003', 'CASHIER', 'active');

INSERT INTO admin_users (admin_id, username, password, real_name, phone, role, status)
VALUES (seq_admin_users.NEXTVAL, 'warehouse01', '$2a$10$PLACEHOLDER_FOR_BCRYPT_ADMIN123', '仓管员', '13000000004', 'WAREHOUSE', 'active');

INSERT INTO admin_users (admin_id, username, password, real_name, phone, role, status)
VALUES (seq_admin_users.NEXTVAL, 'product01', '$2a$10$PLACEHOLDER_FOR_BCRYPT_ADMIN123', '商品管理员', '13000000005', 'PRODUCT', 'active');

INSERT INTO admin_users (admin_id, username, password, real_name, phone, role, status)
VALUES (seq_admin_users.NEXTVAL, 'service01', '$2a$10$PLACEHOLDER_FOR_BCRYPT_ADMIN123', '客服', '13000000006', 'SERVICE', 'active');

-- 配送员 (密码明文123456)
INSERT INTO delivery_persons (courier_id, real_name, phone, password, status)
VALUES (seq_delivery_persons.NEXTVAL, '张三', '13900000001', '123456', 'active');

INSERT INTO delivery_persons (courier_id, real_name, phone, password, status)
VALUES (seq_delivery_persons.NEXTVAL, '李四', '13900000002', '123456', 'active');

INSERT INTO delivery_persons (courier_id, real_name, phone, password, status)
VALUES (seq_delivery_persons.NEXTVAL, '王五', '13900000003', '123456', 'active');

-- 会员用户 (密码占位符)
INSERT INTO users (user_id, username, password, nickname, real_name, phone, member_level, points, status)
VALUES (seq_users.NEXTVAL, '13800138001', '$2a$10$PLACEHOLDER_FOR_BCRYPT_123456', '银卡会员', '张三', '13800138001', 'SILVER', 520, 'active');

INSERT INTO users (user_id, username, password, nickname, real_name, phone, member_level, points, status)
VALUES (seq_users.NEXTVAL, '13800138002', '$2a$10$PLACEHOLDER_FOR_BCRYPT_123456', '金卡会员', '李四', '13800138002', 'GOLD', 1200, 'active');

INSERT INTO users (user_id, username, password, nickname, real_name, phone, member_level, points, status)
VALUES (seq_users.NEXTVAL, '13800138003', '$2a$10$PLACEHOLDER_FOR_BCRYPT_123456', '普通会员', '王五', '13800138003', 'NORMAL', 0, 'active');

-- 商品分类 (一级5个，二级9个)
INSERT INTO categories (category_id, parent_id, category_name, sort_order, status, description)
VALUES (seq_categories.NEXTVAL, 0, '饮料', 1, 'active', '各种饮料饮品');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status, description)
VALUES (seq_categories.NEXTVAL, 0, '食品', 2, 'active', '休闲零食食品');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status, description)
VALUES (seq_categories.NEXTVAL, 0, '日用品', 3, 'active', '日常生活用品');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status, description)
VALUES (seq_categories.NEXTVAL, 0, '酒类', 4, 'active', '白酒红酒啤酒');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status, description)
VALUES (seq_categories.NEXTVAL, 0, '调味品', 5, 'active', '油盐酱醋调料');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 1, '碳酸饮料', 1, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 1, '瓶装水', 2, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 2, '膨化食品', 1, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 2, '坚果', 2, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 2, '糖果巧克力', 3, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 3, '个人护理', 1, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 3, '家居清洁', 2, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 4, '白酒', 1, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 4, '啤酒', 2, 'active');

-- 品牌 (8个)
INSERT INTO brands (brand_id, brand_name, status) VALUES (seq_brands.NEXTVAL, '可口可乐', 'active');
INSERT INTO brands (brand_id, brand_name, status) VALUES (seq_brands.NEXTVAL, '农夫山泉', 'active');
INSERT INTO brands (brand_id, brand_name, status) VALUES (seq_brands.NEXTVAL, '乐事薯片', 'active');
INSERT INTO brands (brand_id, brand_name, status) VALUES (seq_brands.NEXTVAL, '统一', 'active');
INSERT INTO brands (brand_id, brand_name, status) VALUES (seq_brands.NEXTVAL, '海天', 'active');
INSERT INTO brands (brand_id, brand_name, status) VALUES (seq_brands.NEXTVAL, '伊利', 'active');
INSERT INTO brands (brand_id, brand_name, status) VALUES (seq_brands.NEXTVAL, '蒙牛', 'active');
INSERT INTO brands (brand_id, brand_name, status) VALUES (seq_brands.NEXTVAL, '奥利奥', 'active');

-- 供应商 (3个)
INSERT INTO suppliers (supplier_id, supplier_name, contact_name, contact_phone, email, address, bank_account, payment_days, status)
VALUES (seq_suppliers.NEXTVAL, '可口可乐饮料有限公司', '王经理', '020-88888881', 'wangmgr@coke.com', '广东省广州市天河区珠江新城1号', '工行6217000000000001', 30, 'active');

INSERT INTO suppliers (supplier_id, supplier_name, contact_name, contact_phone, email, address, bank_account, payment_days, status)
VALUES (seq_suppliers.NEXTVAL, '农夫山泉股份有限公司', '李总', '0571-88888882', 'limgr@nongfu.com', '浙江省杭州市西湖区龙井路88号2楼', '工行6222000000000002', 30, 'active');

INSERT INTO suppliers (supplier_id, supplier_name, contact_name, contact_phone, email, address, bank_account, payment_days, status)
VALUES (seq_suppliers.NEXTVAL, 'MASTER食品贸易公司', '张经理', '022-88888883', 'zhangmgr@master.com', '天津市滨海新区响螺湾3号仓库', '工行6225000000000003', 45, 'active');

-- 商品数据 (9个商品，product_id 1000~1008)
INSERT INTO products (product_id, product_name, barcode, category_id, brand_id, supplier_id, cost_price, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, '可口可乐 330ml', '6901234500001', 6, 1, 1, 2.00, 4.00, 3.50, 500, 50, '瓶', 9999, 4.8, 1, 'active');

INSERT INTO products (product_id, product_name, barcode, category_id, brand_id, supplier_id, cost_price, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, '农夫山泉 550ml', '6901234500002', 7, 2, 2, 1.00, 2.50, 2.00, 1000, 100, '瓶', 8888, 4.9, 1, 'active');

INSERT INTO products (product_id, product_name, barcode, category_id, brand_id, supplier_id, cost_price, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, '乐事薯片 家庭装 组合', '6901234500003', 8, 3, 3, 2.50, 5.50, 4.50, 300, 30, '包', 5000, 4.7, 1, 'active');

INSERT INTO products (product_id, product_name, barcode, category_id, brand_id, supplier_id, cost_price, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, '统一冰红茶 500ml', '6901234500004', 6, 4, 2, 1.50, 3.50, 3.00, 400, 40, '瓶', 3000, 4.6, 0, 'active');

INSERT INTO products (product_id, product_name, barcode, category_id, brand_id, supplier_id, cost_price, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, '海天酱油 生抽 400ml', '6901234500005', 5, 5, 1, 15.00, 35.00, 29.90, 150, 20, '瓶', 1200, 4.5, 0, 'active');

INSERT INTO products (product_id, product_name, barcode, category_id, brand_id, supplier_id, cost_price, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, '伊利纯牛奶 枕包 120g', '6901234500006', 2, 6, 1, 6.00, 15.00, 12.00, 200, 20, '袋', 2000, 4.7, 0, 'active');

INSERT INTO products (product_id, product_name, barcode, category_id, brand_id, supplier_id, cost_price, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, '蒙牛酸奶 原味 388g', '6901234500007', 2, 7, 1, 8.00, 20.00, 16.90, 250, 25, '杯', 4500, 4.8, 1, 'active');

INSERT INTO products (product_id, product_name, barcode, category_id, brand_id, supplier_id, cost_price, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, '奥利奥饼干 夹心 104g', '6901234500008', 2, 8, 1, 3.00, 8.00, 6.50, 300, 30, '盒', 6000, 4.6, 0, 'active');

-- 第9个商品：可口可乐多规格（has_sku=1）
INSERT INTO products (product_id, product_name, barcode, category_id, brand_id, supplier_id, cost_price, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, has_sku, status)
VALUES (seq_products.NEXTVAL, '可口可乐全系列', NULL, 6, 1, 1, 0, 5.00, 3.50, 0, 0, '瓶/箱', 500, 4.8, 0, 1, 'active');

-- SKU规格（为第9个商品创建3个规格）
INSERT INTO product_skus (sku_id, product_id, sku_name, sku_spec, price, original_price, cost_price, stock, barcode, sort_order)
VALUES (seq_product_skus.NEXTVAL, seq_products.CURRVAL, '330ml 瓶装', '{"容量":"330ml","包装":"瓶装"}', 3.50, 4.00, 2.00, 200, '6901234509001', 1);

INSERT INTO product_skus (sku_id, product_id, sku_name, sku_spec, price, original_price, cost_price, stock, barcode, sort_order)
VALUES (seq_product_skus.NEXTVAL, seq_products.CURRVAL, '500ml 瓶装', '{"容量":"500ml","包装":"瓶装"}', 4.50, 5.50, 2.50, 200, '6901234509002', 2);

INSERT INTO product_skus (sku_id, product_id, sku_name, sku_spec, price, original_price, cost_price, stock, barcode, sort_order)
VALUES (seq_product_skus.NEXTVAL, seq_products.CURRVAL, '1.25L 大瓶', '{"容量":"1.25L","包装":"瓶装"}', 6.50, 8.00, 3.50, 100, '6901234509003', 3);

-- 轮播图 (3个)
INSERT INTO banners (banner_id, image_url, title, link_type, sort_order, status)
VALUES (seq_banners.NEXTVAL, '/uploads/banner/banner1.jpg', '夏季饮料大促', 'CATEGORY', 1, 'active');
INSERT INTO banners (banner_id, image_url, title, link_type, sort_order, status)
VALUES (seq_banners.NEXTVAL, '/uploads/banner/banner2.jpg', '新品上市', 'NONE', 2, 'active');
INSERT INTO banners (banner_id, image_url, title, link_type, sort_order, status)
VALUES (seq_banners.NEXTVAL, '/uploads/banner/banner3.jpg', '会员日专享', 'NONE', 3, 'active');

-- 优惠券数据 (3张)
INSERT INTO coupons (coupon_id, coupon_name, description, coupon_type, face_value, min_amount, total_count, per_limit, start_time, end_time, status)
VALUES (seq_coupons.NEXTVAL, '新人满50减10', '全场满50元可用', 'FULL_REDUCE', 10.00, 50.00, 1000, 1, SYSTIMESTAMP, SYSTIMESTAMP + INTERVAL '30' DAY, 'active');

INSERT INTO coupons (coupon_id, coupon_name, description, coupon_type, face_value, min_amount, total_count, per_limit, start_time, end_time, status)
VALUES (seq_coupons.NEXTVAL, '全场9折券', '全场商品可享9折优惠', 'DISCOUNT', 0.9, 0.00, -1, 1, SYSTIMESTAMP, SYSTIMESTAMP + INTERVAL '7' DAY, 'active');

INSERT INTO coupons (coupon_id, coupon_name, description, coupon_type, face_value, min_amount, category_id, total_count, per_limit, start_time, end_time, status)
VALUES (seq_coupons.NEXTVAL, '食品类满20减5', '休闲食品专区满20元减5元', 'CATEGORY', 5.00, 20.00, 2, 500, 2, SYSTIMESTAMP, SYSTIMESTAMP + INTERVAL '14' DAY, 'active');

-- 活动数据 (2个活动)
INSERT INTO activities (activity_id, activity_name, activity_type, scope_type, start_time, end_time, status)
VALUES (seq_activities.NEXTVAL, '可口可乐秒杀', 'SECKILL', 'ALL', SYSTIMESTAMP, SYSTIMESTAMP + INTERVAL '2' DAY, 'active');

INSERT INTO activity_products (id, activity_id, product_id, activity_price, activity_stock, sold_count)
VALUES (seq_activity_products.NEXTVAL, 1, 1000, 2.90, 100, 0);

INSERT INTO activities (activity_id, activity_name, activity_type, scope_type, start_time, end_time, status)
VALUES (seq_activities.NEXTVAL, '夏日满减', 'FULL_REDUCE', 'ALL', SYSTIMESTAMP, SYSTIMESTAMP + INTERVAL '30' DAY, 'active');

INSERT INTO full_reduce_rules (rule_id, activity_id, threshold, reduce_amount, sort_order)
VALUES (seq_full_reduce_rules.NEXTVAL, 2, 100.00, 15.00, 1);

INSERT INTO full_reduce_rules (rule_id, activity_id, threshold, reduce_amount, sort_order)
VALUES (seq_full_reduce_rules.NEXTVAL, 2, 200.00, 35.00, 2);

INSERT INTO full_reduce_rules (rule_id, activity_id, threshold, reduce_amount, sort_order)
VALUES (seq_full_reduce_rules.NEXTVAL, 2, 300.00, 60.00, 3);

COMMIT;

PROMPT ============================================================
PROMPT STEP 5: 数据验证
PROMPT ============================================================

SELECT table_name AS "表名", num_rows AS "行数(估算)"
FROM user_tables
WHERE table_name IN ('USERS','ADMIN_USERS','CATEGORIES','BRANDS','SUPPLIERS','PRODUCTS','PRODUCT_SKUS','PRODUCT_IMAGES','ADDRESSES','CART','COUPONS','USER_COUPONS','ACTIVITIES','ACTIVITY_PRODUCTS','FULL_REDUCE_RULES','BANNERS','ORDERS','ORDER_ITEMS','ORDER_STATUS_LOGS','AFTER_SALES','REVIEWS','FAVORITES','MESSAGES','POINTS_LOGS','INVENTORY_LOGS','PURCHASE_ORDERS','PURCHASE_ORDER_ITEMS','INVENTORY_CHECKS','INVENTORY_CHECK_ITEMS','DAMAGE_RECORDS','DELIVERY_PERSONS','DELIVERY_TASKS','CASHIER_SHIFTS','CASHIER_RECORDS','CASHIER_RECORD_ITEMS','AUDIT_LOGS')
ORDER BY table_name;

SELECT '=== 初始数据统计 ===' AS INFO FROM DUAL;
SELECT 'admin_users(管理员):    ' || COUNT(*) FROM admin_users;
SELECT 'users(会员):            ' || COUNT(*) FROM users;
SELECT 'categories(分类):       ' || COUNT(*) FROM categories;
SELECT 'brands(品牌):           ' || COUNT(*) FROM brands;
SELECT 'suppliers(供应商):      ' || COUNT(*) FROM suppliers;
SELECT 'products(商品):         ' || COUNT(*) FROM products;
SELECT 'product_skus(SKU):      ' || COUNT(*) FROM product_skus;
SELECT 'coupons(优惠券):        ' || COUNT(*) FROM coupons;
SELECT 'activities(活动):       ' || COUNT(*) FROM activities;
SELECT 'full_reduce_rules(满减): ' || COUNT(*) FROM full_reduce_rules;
SELECT 'banners(轮播图):        ' || COUNT(*) FROM banners;
SELECT 'delivery_persons(配送员): ' || COUNT(*) FROM delivery_persons;

PROMPT ============================================================
PROMPT 数据库创建完成！
PROMPT 共创建 34 张表 + 34 个序列
PROMPT 超市管理系统 v3.0 - Oracle XE
PROMPT ============================================================
