-- ============================================================
-- 超市管理系统 — 完整数据库脚本 v3.0
-- 数据库: Oracle XE  用户: system  密码: 123456
-- 执行方式: sqlplus system/123456@localhost:1521:XE @all_tables_v3.sql
-- 对应功能清单: v3.0 (六端版 · 32模块 · 162功能点)
-- 升级说明:
--   · 新增 product_skus 表支持SKU规格选择 (C-18)
--   · 新增 order_status_logs 订单状态时间线 (C-42/B-17)
--   · 新增 cashier_record_items 收银明细 (K-06/K-07)
--   · 新增 full_reduce_rules 满减阶梯规则 (B-35)
--   · products 新增 barcode/cost_price/deleted 字段
--   · orders 新增 source/delivery_time_slot/express_*/refund* 字段
--   · after_sales 新增 images/item_id/as_no 字段
--   · purchase_orders 新增 expected_arrive_time/approved_by/approved_time
--   · cashier_shifts 新增交班汇总字段
--   · cashier_records 新增 received_amount/change_amount/coupon_id/items
--   · suppliers 新增 email/bank_account/payment_days
--   · banners 新增 start_time/end_time 展示时间
--   · coupons 新增 per_limit 每人限领次数/description
--   · reviews 新增 tags 评价标签
--   · messages 新增 ref_id 关联业务ID
--   · damage_records 新增 damage_no/unit_cost/total_cost
--   · admin_users 新增 phone 字段
--   · inventory_check_items 新增 remark 差异备注
-- ============================================================

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
  -- 新增表（v3新增，先删）
  drop_if_exists('FULL_REDUCE_RULES');
  drop_if_exists('CASHIER_RECORD_ITEMS');
  drop_if_exists('ORDER_STATUS_LOGS');
  drop_if_exists('PRODUCT_SKUS');
  -- 原有表（从叶到根）
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
  -- 兼容旧序列名
  drop_seq('SEQ_USER');        drop_seq('SEQ_CATEGORY');
  drop_seq('SEQ_PRODUCT');     drop_seq('SEQ_ORDER');
  drop_seq('SEQ_ORDER_ITEM');  drop_seq('SEQ_INVENTORY_LOG');
  drop_seq('SEQ_PROMOTION');   drop_seq('SEQ_PROMO_PRODUCT');
  drop_seq('SEQ_DELIVERY');    drop_seq('SEQ_PAYMENT');
  drop_seq('SEQ_WAREHOUSING'); drop_seq('SEQ_OUTBOUND');
  drop_seq('SEQ_ADDRESS');     drop_seq('SEQ_BEHAVIOR');
END;
/

PROMPT ============================================================
PROMPT STEP 2: 创建表结构 (v3.0)
PROMPT ============================================================

-- ============================================================
-- 【1】用户表（顾客端）
-- 对应功能: C-01~C-06
-- ============================================================
CREATE TABLE users (
    user_id       NUMBER          NOT NULL,
    username      VARCHAR2(50)    NOT NULL,           -- 手机号作为登录账号
    password      VARCHAR2(100)   NOT NULL,           -- BCrypt 加密
    nickname      VARCHAR2(50),
    real_name     VARCHAR2(50),
    gender        CHAR(1)         DEFAULT 'U',        -- M/F/U(未知)
    birthday      DATE,
    email         VARCHAR2(100),
    avatar_url    VARCHAR2(500),
    phone         VARCHAR2(20),                       -- 手机号（与username保持一致，支持脱敏展示）
    member_level  VARCHAR2(10)    DEFAULT 'NORMAL',   -- NORMAL/SILVER/GOLD/DIAMOND
    points        NUMBER          DEFAULT 0,
    total_consume NUMBER(12,2)    DEFAULT 0,          -- 累计消费金额（用户详情B-27消费统计）
    order_count   NUMBER          DEFAULT 0,          -- 累计订单数（B-27）
    last_order_time TIMESTAMP,                        -- 最近下单时间（B-27）
    ban_reason    VARCHAR2(200),                      -- 封禁原因（B-28）
    status        VARCHAR2(10)    DEFAULT 'active',   -- active/banned
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
-- 【2】管理员用户表
-- 对应功能: B-01~B-06
-- 角色说明:
--   SUPER_ADMIN  超级管理员（全部权限）
--   MANAGER      店长（含封禁用户/强制取消/盘点审批权限）
--   PRODUCT      商品专员
--   FINANCE      财务专员
--   SERVICE      客服专员
--   WAREHOUSE    仓库管理员
--   CASHIER      收银员
-- ============================================================
CREATE TABLE admin_users (
    admin_id      NUMBER          NOT NULL,
    username      VARCHAR2(50)    NOT NULL,
    password      VARCHAR2(100)   NOT NULL,
    real_name     VARCHAR2(50),
    phone         VARCHAR2(20),                       -- 管理员手机号（B-04修改用）
    role          VARCHAR2(20)    NOT NULL,
    status        VARCHAR2(10)    DEFAULT 'active',   -- active/inactive
    last_login    TIMESTAMP,
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_admin_users PRIMARY KEY (admin_id),
    CONSTRAINT uq_admin_username UNIQUE (username),
    CONSTRAINT chk_admin_role CHECK (role IN (
        'SUPER_ADMIN','MANAGER','PRODUCT','FINANCE','SERVICE','WAREHOUSE','CASHIER'
    )),
    CONSTRAINT chk_admin_status CHECK (status IN ('active','inactive'))
);
CREATE SEQUENCE seq_admin_users START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【3】商品分类表（支持二级：parent_id=0 为一级）
-- 对应功能: B-13, C-12
-- ============================================================
CREATE TABLE categories (
    category_id   NUMBER          NOT NULL,
    parent_id     NUMBER          DEFAULT 0,          -- 0=一级分类
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
-- 【4】品牌表
-- 对应功能: B-14, C-16筛选
-- ============================================================
CREATE TABLE brands (
    brand_id      NUMBER          NOT NULL,
    brand_name    VARCHAR2(100)   NOT NULL,
    logo_url      VARCHAR2(500),
    description   VARCHAR2(500),
    product_count NUMBER          DEFAULT 0,          -- 关联商品数（B-14展示）
    status        VARCHAR2(10)    DEFAULT 'active',
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_brands PRIMARY KEY (brand_id),
    CONSTRAINT uq_brand_name UNIQUE (brand_name),
    CONSTRAINT chk_brands_status CHECK (status IN ('active','inactive'))
);
CREATE SEQUENCE seq_brands START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【5】供应商表
-- 对应功能: B-15（完整档案：联系人/手机/邮箱/地址/结算账期）
-- ============================================================
CREATE TABLE suppliers (
    supplier_id   NUMBER          NOT NULL,
    supplier_name VARCHAR2(100)   NOT NULL,
    contact_name  VARCHAR2(50),
    contact_phone VARCHAR2(20),
    email         VARCHAR2(100),                      -- B-15邮箱
    address       VARCHAR2(200),
    bank_account  VARCHAR2(100),                      -- B-15结算账号
    payment_days  NUMBER          DEFAULT 30,         -- B-15结算账期（天）
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
-- 【6】商品主表
-- 对应功能: B-07~B-12, C-13~C-21, K-05
-- ============================================================
CREATE TABLE products (
    product_id    NUMBER          NOT NULL,
    product_name  VARCHAR2(100)   NOT NULL,
    barcode       VARCHAR2(50),                       -- 条码（K-05收银搜索/B-08录入）
    category_id   NUMBER,
    brand_id      NUMBER,
    supplier_id   NUMBER,
    description   CLOB,
    cover_image   VARCHAR2(500),
    unit          VARCHAR2(20),                       -- 件/箱/kg/瓶/包
    cost_price    NUMBER(10,2)    DEFAULT 0,          -- 成本价（B-08/D-17报损成本）
    original_price NUMBER(10,2)  NOT NULL,            -- 原价（划线价）
    price         NUMBER(10,2)   NOT NULL,            -- 售价
    stock         NUMBER         DEFAULT 0,           -- 库存（无SKU时用此字段）
    stock_warning NUMBER         DEFAULT 10,          -- 低库存预警阈值
    sales_count   NUMBER         DEFAULT 0,           -- 累计销量
    avg_rating    NUMBER(3,1)    DEFAULT 5.0,         -- 平均评分
    review_count  NUMBER         DEFAULT 0,           -- 评价数量
    is_recommend  NUMBER(1)      DEFAULT 0,           -- 1=首页推荐（C-08）
    has_sku       NUMBER(1)      DEFAULT 0,           -- 1=有多规格SKU（C-18）
    status        VARCHAR2(10)   DEFAULT 'active',    -- active/off_shelf
    is_deleted    NUMBER(1)      DEFAULT 0,           -- 1=逻辑删除（B-10）
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
-- 【7】商品SKU规格表（多规格商品）
-- 对应功能: C-18 SKU规格选择（规格切换价格/库存）
-- 说明: 无多规格商品直接用products.stock/price即可
--       有多规格时 has_sku=1，stock/price以本表为准
-- ============================================================
CREATE TABLE product_skus (
    sku_id        NUMBER          NOT NULL,
    product_id    NUMBER          NOT NULL,
    sku_name      VARCHAR2(100)   NOT NULL,           -- 规格名称，如"500ml/瓶"
    sku_spec      VARCHAR2(200),                      -- 规格属性JSON，如{"容量":"500ml","口味":"原味"}
    price         NUMBER(10,2)    NOT NULL,
    original_price NUMBER(10,2),
    cost_price    NUMBER(10,2)    DEFAULT 0,
    stock         NUMBER          DEFAULT 0,
    barcode       VARCHAR2(50),                       -- SKU级条码
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
-- 【8】商品图片表（多图）
-- 对应功能: C-17 多图轮播
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
-- 【9】收货地址表（最多10条）
-- 对应功能: C-29~C-33
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
-- 【10】购物车表
-- 对应功能: C-22~C-28
-- 说明: 同一用户同商品同SKU唯一；失效商品通过关联查询product.status判断
-- ============================================================
CREATE TABLE cart (
    cart_id       NUMBER          NOT NULL,
    user_id       NUMBER          NOT NULL,
    product_id    NUMBER          NOT NULL,
    sku_id        NUMBER,                             -- NULL=无SKU商品（C-18）
    quantity      NUMBER          DEFAULT 1,
    is_checked    NUMBER(1)       DEFAULT 1,          -- 1=勾选结算（C-26）
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
-- 【11】优惠券模板表
-- 对应功能: B-31~B-33, C-50~C-52
-- 类型说明:
--   FULL_REDUCE  满减券（满min_amount减face_value元）
--   DISCOUNT     折扣券（face_value=0.9表示九折，min_amount为门槛）
--   CATEGORY     品类券（限指定category_id）
-- ============================================================
CREATE TABLE coupons (
    coupon_id     NUMBER          NOT NULL,
    coupon_name   VARCHAR2(100)   NOT NULL,
    description   VARCHAR2(200),                      -- 券说明（C-50展示）
    coupon_type   VARCHAR2(20)    NOT NULL,            -- FULL_REDUCE/DISCOUNT/CATEGORY
    face_value    NUMBER(10,2)    NOT NULL,            -- 满减金额 或 折扣率(0.9=九折)
    min_amount    NUMBER(10,2)    DEFAULT 0,           -- 使用门槛（0=无门槛）
    category_id   NUMBER,                             -- 品类券限定分类（NULL=全场）
    total_count   NUMBER          DEFAULT -1,          -- -1=不限量
    issued_count  NUMBER          DEFAULT 0,           -- 已发放数
    used_count    NUMBER          DEFAULT 0,           -- 已使用数（便于核销统计D-18）
    per_limit     NUMBER          DEFAULT 1,           -- 每人限领次数（C-51，-1=不限）
    start_time    TIMESTAMP       NOT NULL,
    end_time      TIMESTAMP       NOT NULL,
    status        VARCHAR2(10)    DEFAULT 'active',   -- active/inactive
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_coupons PRIMARY KEY (coupon_id),
    CONSTRAINT fk_coup_category FOREIGN KEY (category_id) REFERENCES categories(category_id),
    CONSTRAINT chk_coup_type CHECK (coupon_type IN ('FULL_REDUCE','DISCOUNT','CATEGORY')),
    CONSTRAINT chk_coup_status CHECK (status IN ('active','inactive')),
    CONSTRAINT chk_coup_face_value CHECK (face_value > 0)
);
CREATE SEQUENCE seq_coupons START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【12】用户优惠券表（领券记录）
-- 对应功能: C-51~C-52, C-35（结算选券）
-- ============================================================
CREATE TABLE user_coupons (
    uc_id         NUMBER          NOT NULL,
    user_id       NUMBER          NOT NULL,
    coupon_id     NUMBER          NOT NULL,
    status        VARCHAR2(10)    DEFAULT 'unused',   -- unused/used/expired
    get_time      TIMESTAMP       DEFAULT SYSTIMESTAMP,
    use_time      TIMESTAMP,
    order_id      NUMBER,                             -- 使用的订单
    CONSTRAINT pk_user_coupons PRIMARY KEY (uc_id),
    CONSTRAINT fk_uc_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_uc_coupon FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id),
    CONSTRAINT chk_uc_status CHECK (status IN ('unused','used','expired'))
);
CREATE SEQUENCE seq_user_coupons START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【13】促销活动表
-- 对应功能: B-34（秒杀活动）B-35（满减活动）C-11（促销入口）
-- 说明: 满减阶梯规则存于 full_reduce_rules 子表；秒杀价存于 activity_products
-- ============================================================
CREATE TABLE activities (
    activity_id   NUMBER          NOT NULL,
    activity_name VARCHAR2(100)   NOT NULL,
    activity_type VARCHAR2(20)    NOT NULL,            -- SECKILL/FULL_REDUCE
    scope_type    VARCHAR2(20)    DEFAULT 'ALL',       -- ALL/CATEGORY（满减适用范围）
    scope_category_id NUMBER,                          -- scope_type=CATEGORY时关联分类
    seckill_stock NUMBER          DEFAULT 0,           -- 秒杀总库存（SECKILL类型）
    start_time    TIMESTAMP       NOT NULL,
    end_time      TIMESTAMP       NOT NULL,
    status        VARCHAR2(10)    DEFAULT 'active',    -- active/inactive
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_activities PRIMARY KEY (activity_id),
    CONSTRAINT chk_act_type CHECK (activity_type IN ('SECKILL','FULL_REDUCE')),
    CONSTRAINT chk_act_status CHECK (status IN ('active','inactive')),
    CONSTRAINT chk_act_scope CHECK (scope_type IN ('ALL','CATEGORY'))
);
CREATE SEQUENCE seq_activities START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【13-1】满减阶梯规则表（归属 activities，仅 FULL_REDUCE 类型使用）
-- 对应功能: B-35（如满100减15、满200减35，多档阶梯）
-- ============================================================
CREATE TABLE full_reduce_rules (
    rule_id       NUMBER          NOT NULL,
    activity_id   NUMBER          NOT NULL,
    threshold     NUMBER(10,2)    NOT NULL,            -- 满多少（如100）
    reduce_amount NUMBER(10,2)    NOT NULL,            -- 减多少（如15）
    sort_order    NUMBER          DEFAULT 0,           -- 档位排序（越大门槛越高）
    CONSTRAINT pk_full_reduce_rules PRIMARY KEY (rule_id),
    CONSTRAINT fk_frr_activity FOREIGN KEY (activity_id) REFERENCES activities(activity_id),
    CONSTRAINT chk_frr_threshold CHECK (threshold > 0),
    CONSTRAINT chk_frr_reduce CHECK (reduce_amount > 0)
);
CREATE SEQUENCE seq_full_reduce_rules START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【13-2】活动关联商品（秒杀专用：秒杀价/秒杀库存）
-- 对应功能: B-34
-- ============================================================
CREATE TABLE activity_products (
    id              NUMBER          NOT NULL,
    activity_id     NUMBER          NOT NULL,
    product_id      NUMBER          NOT NULL,
    sku_id          NUMBER,                           -- 指定SKU秒杀（NULL=主商品）
    activity_price  NUMBER(10,2)    NOT NULL,         -- 秒杀价
    activity_stock  NUMBER          DEFAULT 0,        -- 秒杀库存
    sold_count      NUMBER          DEFAULT 0,        -- 已售数量
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
-- 【14】首页轮播图表
-- 对应功能: B-36（含展示时间控制）C-07
-- ============================================================
CREATE TABLE banners (
    banner_id     NUMBER          NOT NULL,
    image_url     VARCHAR2(500)   NOT NULL,
    title         VARCHAR2(100),                      -- Banner标题/说明
    link_type     VARCHAR2(20)    DEFAULT 'NONE',     -- PRODUCT/CATEGORY/ACTIVITY/NONE
    link_id       NUMBER,                             -- 跳转目标ID
    sort_order    NUMBER          DEFAULT 0,
    start_time    TIMESTAMP,                          -- 展示开始时间（B-36，NULL=立即）
    end_time      TIMESTAMP,                          -- 展示结束时间（B-36，NULL=长期）
    status        VARCHAR2(10)    DEFAULT 'active',   -- active/inactive
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_banners PRIMARY KEY (banner_id),
    CONSTRAINT chk_banner_link CHECK (link_type IN ('PRODUCT','CATEGORY','ACTIVITY','NONE')),
    CONSTRAINT chk_banner_status CHECK (status IN ('active','inactive'))
);
CREATE SEQUENCE seq_banners START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【15】订单主表
-- 对应功能: C-34~C-49, B-16~B-21, P-04~P-09
-- 订单状态流转:
--   PENDING_PAY → PAID → PENDING_SHIP → SHIPPING → PENDING_RECEIVED → COMPLETED
--                                                  → CANCELLED（任何阶段前）
--                                                  → REFUNDED（售后完成后）
-- ============================================================
CREATE TABLE orders (
    order_id             NUMBER          NOT NULL,
    order_no             VARCHAR2(30)    NOT NULL,    -- 业务单号 SM202603230001
    user_id              NUMBER          NOT NULL,
    source               VARCHAR2(10)    DEFAULT 'ONLINE', -- ONLINE/CASHIER（B-16来源筛选）
    address_id           NUMBER,                      -- 线上订单必填，收银单可NULL
    receiver_snapshot    VARCHAR2(500),               -- 收货人信息快照（防地址修改影响）
    total_amount         NUMBER(12,2)    NOT NULL,    -- 商品合计
    discount_amount      NUMBER(12,2)    DEFAULT 0,   -- 优惠抵扣合计
    coupon_discount      NUMBER(12,2)    DEFAULT 0,   -- 优惠券抵扣（明细）
    points_deduct_amount NUMBER(12,2)    DEFAULT 0,   -- 积分抵扣金额（C-36）
    freight_amount       NUMBER(12,2)    DEFAULT 0,   -- 运费
    pay_amount           NUMBER(12,2)    NOT NULL,    -- 实付金额
    pay_method           VARCHAR2(20)    DEFAULT 'MOCK', -- MOCK/CASH/MOCK_CARD
    coupon_id            NUMBER,                      -- 使用的优惠券
    uc_id                NUMBER,                      -- 用户优惠券记录ID（核销用）
    points_used          NUMBER          DEFAULT 0,   -- 使用积分数
    delivery_time_slot   VARCHAR2(50),                -- 期望配送时间段（C-37，如"明日上午"）
    express_company      VARCHAR2(50),                -- 快递公司（B-19）
    express_no           VARCHAR2(50),                -- 快递单号（B-19）
    remark               VARCHAR2(500),               -- 订单备注（C-38）
    cancel_reason        VARCHAR2(200),               -- 取消原因（B-21）
    refund_amount        NUMBER(12,2)    DEFAULT 0,   -- 实际退款金额
    status               VARCHAR2(20)   DEFAULT 'PENDING_PAY',
    -- PENDING_PAY / PAID / PENDING_SHIP / SHIPPING / PENDING_RECEIVED / COMPLETED / CANCELLED / REFUNDED
    pay_time             TIMESTAMP,
    ship_time            TIMESTAMP,
    pickup_time          TIMESTAMP,                   -- 配送员取件时间
    deliver_time         TIMESTAMP,                   -- 送达时间
    confirm_time         TIMESTAMP,                   -- 顾客确认收货时间（C-45）
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
    CONSTRAINT chk_order_status CHECK (status IN (
        'PENDING_PAY','PAID','PENDING_SHIP','SHIPPING',
        'PENDING_RECEIVED','COMPLETED','CANCELLED','REFUNDED'
    )),
    CONSTRAINT chk_order_source CHECK (source IN ('ONLINE','CASHIER')),
    CONSTRAINT chk_order_pay CHECK (pay_method IN ('MOCK','CASH','MOCK_CARD'))
);
CREATE SEQUENCE seq_orders START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【16】订单明细表
-- 对应功能: C-42, B-17（商品明细）
-- 说明: 下单时快照商品名称/价格/图片，防止商品信息变更影响历史订单
-- ============================================================
CREATE TABLE order_items (
    item_id       NUMBER          NOT NULL,
    order_id      NUMBER          NOT NULL,
    product_id    NUMBER          NOT NULL,
    sku_id        NUMBER,                             -- 规格ID快照（C-18）
    product_name  VARCHAR2(100)   NOT NULL,           -- 商品名称快照
    sku_name      VARCHAR2(100),                      -- 规格名称快照
    product_image VARCHAR2(500),                      -- 封面图快照
    unit_price    NUMBER(10,2)    NOT NULL,           -- 成交单价快照
    cost_price    NUMBER(10,2)    DEFAULT 0,          -- 成本价快照（毛利分析）
    quantity      NUMBER          NOT NULL,
    subtotal      NUMBER(12,2)    NOT NULL,           -- 小计=单价×数量
    CONSTRAINT pk_order_items PRIMARY KEY (item_id),
    CONSTRAINT fk_oi_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_oi_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT chk_oi_qty CHECK (quantity >= 1),
    CONSTRAINT chk_oi_price CHECK (unit_price >= 0)
);
CREATE SEQUENCE seq_order_items START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【17】订单状态变更日志（操作时间线）
-- 对应功能: C-42（订单时间线）B-17（操作历史时间线）
-- 说明: 每次状态变更时插入一条记录，前端按create_time顺序展示时间线
-- ============================================================
CREATE TABLE order_status_logs (
    log_id        NUMBER          NOT NULL,
    order_id      NUMBER          NOT NULL,
    from_status   VARCHAR2(20),                       -- 变更前状态（NULL=初次创建）
    to_status     VARCHAR2(20)    NOT NULL,           -- 变更后状态
    operator_type VARCHAR2(10)    DEFAULT 'USER',     -- USER/ADMIN/SYSTEM/COURIER
    operator_id   NUMBER,                             -- 操作人ID（对应类型的ID）
    operator_name VARCHAR2(50),                       -- 操作人名称快照
    remark        VARCHAR2(200),                      -- 操作说明
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_order_status_logs PRIMARY KEY (log_id),
    CONSTRAINT fk_osl_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT chk_osl_op_type CHECK (operator_type IN ('USER','ADMIN','SYSTEM','COURIER'))
);
CREATE SEQUENCE seq_order_status_logs START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【18】售后申请表
-- 对应功能: C-47~C-48, B-22~B-25
-- ============================================================
CREATE TABLE after_sales (
    as_id         NUMBER          NOT NULL,
    as_no         VARCHAR2(30)    NOT NULL,           -- 售后单号（如AS202603230001）
    order_id      NUMBER          NOT NULL,
    item_id       NUMBER,                             -- 退哪个订单商品（NULL=整单退）
    user_id       NUMBER          NOT NULL,
    as_type       VARCHAR2(20)    DEFAULT 'REFUND',   -- REFUND/EXCHANGE
    reason        VARCHAR2(500)   NOT NULL,           -- 退款/退货原因
    images        VARCHAR2(2000),                     -- 凭证图片URL逗号分隔（C-47上传凭证）
    refund_amount NUMBER(12,2),                       -- 申请退款金额
    status        VARCHAR2(20)    DEFAULT 'PENDING',  -- PENDING/APPROVED/REJECTED/COMPLETED
    admin_remark  VARCHAR2(500),                      -- 处理备注（B-25拒绝原因）
    handler_id    NUMBER,                             -- 处理人管理员ID
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    handle_time   TIMESTAMP,
    CONSTRAINT pk_after_sales PRIMARY KEY (as_id),
    CONSTRAINT uq_as_no UNIQUE (as_no),
    CONSTRAINT fk_as_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_as_item FOREIGN KEY (item_id) REFERENCES order_items(item_id),
    CONSTRAINT fk_as_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT chk_as_type CHECK (as_type IN ('REFUND','EXCHANGE')),
    CONSTRAINT chk_as_status CHECK (status IN ('PENDING','APPROVED','REJECTED','COMPLETED'))
);
CREATE SEQUENCE seq_after_sales START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【19】商品评价表
-- 对应功能: C-20, C-49, B-37~B-39
-- ============================================================
CREATE TABLE reviews (
    review_id     NUMBER          NOT NULL,
    order_id      NUMBER          NOT NULL,
    order_item_id NUMBER          NOT NULL,           -- 评价哪个订单商品（防重复）
    product_id    NUMBER          NOT NULL,
    user_id       NUMBER          NOT NULL,
    rating        NUMBER(1)       NOT NULL,           -- 1~5星
    content       VARCHAR2(1000),
    images        VARCHAR2(2000),                     -- 图片URL逗号分隔
    tags          VARCHAR2(500),                      -- 评价标签逗号分隔（C-49/C-20，如"新鲜,包装好"）
    is_anonymous  NUMBER(1)       DEFAULT 0,          -- 1=匿名（C-49）
    is_hidden     NUMBER(1)       DEFAULT 0,          -- 1=管理员隐藏（B-38）
    reply         VARCHAR2(500),                      -- 商家回复内容（B-39）
    reply_time    TIMESTAMP,                          -- 商家回复时间
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_reviews PRIMARY KEY (review_id),
    CONSTRAINT fk_rev_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_rev_order_item FOREIGN KEY (order_item_id) REFERENCES order_items(item_id),
    CONSTRAINT fk_rev_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_rev_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT uq_rev_order_item UNIQUE (order_item_id),  -- 同一商品只能评价一次
    CONSTRAINT chk_rev_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT chk_rev_anon CHECK (is_anonymous IN (0,1)),
    CONSTRAINT chk_rev_hidden CHECK (is_hidden IN (0,1))
);
CREATE SEQUENCE seq_reviews START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【20】商品收藏表
-- 对应功能: C-53~C-54
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
-- 【21】站内消息表
-- 对应功能: C-55~C-56
-- 消息类型: ORDER(订单通知)/COUPON(促销活动)/SYSTEM(系统通知)/AFTER_SALES(退款通知)
-- ============================================================
CREATE TABLE messages (
    message_id    NUMBER          NOT NULL,
    user_id       NUMBER          NOT NULL,
    title         VARCHAR2(100)   NOT NULL,
    content       VARCHAR2(1000)  NOT NULL,
    msg_type      VARCHAR2(20)    DEFAULT 'SYSTEM',   -- SYSTEM/ORDER/COUPON/AFTER_SALES
    ref_id        NUMBER,                             -- 关联业务ID（如order_id/as_id）
    is_read       NUMBER(1)       DEFAULT 0,
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_messages PRIMARY KEY (message_id),
    CONSTRAINT fk_msg_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT chk_msg_type CHECK (msg_type IN ('SYSTEM','ORDER','COUPON','AFTER_SALES')),
    CONSTRAINT chk_msg_read CHECK (is_read IN (0,1))
);
CREATE SEQUENCE seq_messages START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【22】积分流水表
-- 对应功能: C-57, B-29
-- change_amount: 正=增加，负=扣减
-- reason枚举（业务层约定）: ORDER_REWARD/ORDER_DEDUCT/ADMIN_ADJUST/REFUND_ROLLBACK/REGISTER_GIFT
-- ============================================================
CREATE TABLE points_logs (
    log_id        NUMBER          NOT NULL,
    user_id       NUMBER          NOT NULL,
    change_amount NUMBER          NOT NULL,
    balance_after NUMBER          NOT NULL,
    reason        VARCHAR2(100)   NOT NULL,
    ref_id        NUMBER,                             -- 关联订单/操作ID
    operator_id   NUMBER,                             -- 管理员手动调整时的操作人（B-29）
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_points_logs PRIMARY KEY (log_id),
    CONSTRAINT fk_pl_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);
CREATE SEQUENCE seq_points_logs START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【23】库存变动日志表（全量流水）
-- 对应功能: W-08（入库/出库/报损/盘点/手动调整五类来源）
-- ============================================================
CREATE TABLE inventory_logs (
    log_id        NUMBER          NOT NULL,
    product_id    NUMBER          NOT NULL,
    sku_id        NUMBER,                             -- 有SKU时记录SKU级变动
    change_amount NUMBER          NOT NULL,           -- 正=入库，负=出库/报损
    balance_after NUMBER          NOT NULL,
    log_type      VARCHAR2(20)    NOT NULL,
    -- PURCHASE_IN(采购入库) / ORDER_OUT(销售出库) / DAMAGE(报损)
    -- CHECK_ADJUST(盘点调整) / MANUAL(手动调整)
    ref_id        NUMBER,                             -- 关联单据ID（po_id/order_id/damage_id/check_id）
    remark        VARCHAR2(200),
    operator_id   NUMBER,
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_inventory_logs PRIMARY KEY (log_id),
    CONSTRAINT fk_il_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_il_sku FOREIGN KEY (sku_id) REFERENCES product_skus(sku_id),
    CONSTRAINT chk_il_type CHECK (log_type IN (
        'PURCHASE_IN','ORDER_OUT','DAMAGE','CHECK_ADJUST','MANUAL'
    ))
);
CREATE SEQUENCE seq_inventory_logs START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【24】采购单主表
-- 对应功能: W-01~W-05
-- 状态流转: DRAFT → SUBMITTED → APPROVED → PARTIALLY_ARRIVED → COMPLETED
--                                                              → CANCELLED
-- ============================================================
CREATE TABLE purchase_orders (
    po_id               NUMBER          NOT NULL,
    po_no               VARCHAR2(30)    NOT NULL,     -- 采购单号 PO202603230001
    supplier_id         NUMBER,
    total_amount        NUMBER(12,2)    NOT NULL,
    status              VARCHAR2(20)    DEFAULT 'DRAFT',
    -- DRAFT / SUBMITTED / APPROVED / PARTIALLY_ARRIVED / COMPLETED / CANCELLED
    expected_arrive_time TIMESTAMP,                   -- 预计到货日期（W-02）
    operator_id         NUMBER          NOT NULL,     -- 创建人
    approved_by         NUMBER,                       -- 审批人ID（W-03）
    approved_time       TIMESTAMP,                    -- 审批时间
    remark              VARCHAR2(500),
    create_time         TIMESTAMP       DEFAULT SYSTIMESTAMP,
    complete_time       TIMESTAMP,
    CONSTRAINT pk_purchase_orders PRIMARY KEY (po_id),
    CONSTRAINT uq_po_no UNIQUE (po_no),
    CONSTRAINT fk_po_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id),
    CONSTRAINT chk_po_status CHECK (status IN (
        'DRAFT','SUBMITTED','APPROVED','PARTIALLY_ARRIVED','COMPLETED','CANCELLED'
    ))
);
CREATE SEQUENCE seq_purchase_orders START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【25】采购单明细表
-- 对应功能: W-02（商品明细）W-04（实际收货数量）
-- ============================================================
CREATE TABLE purchase_order_items (
    item_id          NUMBER          NOT NULL,
    po_id            NUMBER          NOT NULL,
    product_id       NUMBER          NOT NULL,
    sku_id           NUMBER,                          -- 指定SKU采购
    order_quantity   NUMBER          NOT NULL,        -- 采购数量
    arrived_quantity NUMBER          DEFAULT 0,       -- 已到货数量（W-04部分到货）
    unit_price       NUMBER(10,2)    NOT NULL,        -- 采购单价
    subtotal         NUMBER(12,2)    NOT NULL,        -- 小计（冗余，便于查询）
    CONSTRAINT pk_po_items PRIMARY KEY (item_id),
    CONSTRAINT fk_poi_po FOREIGN KEY (po_id) REFERENCES purchase_orders(po_id),
    CONSTRAINT fk_poi_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_poi_sku FOREIGN KEY (sku_id) REFERENCES product_skus(sku_id),
    CONSTRAINT chk_poi_qty CHECK (order_quantity >= 1),
    CONSTRAINT chk_poi_arrived CHECK (arrived_quantity >= 0)
);
CREATE SEQUENCE seq_po_items START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【26】库存盘点主表
-- 对应功能: W-10~W-14
-- 状态: DRAFT→IN_PROGRESS→PENDING_APPROVE→COMPLETED
-- ============================================================
CREATE TABLE inventory_checks (
    check_id      NUMBER          NOT NULL,
    check_no      VARCHAR2(30)    NOT NULL,           -- 盘点单号 IC202603230001
    check_scope   VARCHAR2(20)    DEFAULT 'ALL',      -- ALL/CATEGORY（W-10按分类盘点）
    scope_category_id NUMBER,                         -- 盘点分类ID（check_scope=CATEGORY时）
    status        VARCHAR2(20)    DEFAULT 'IN_PROGRESS',
    -- IN_PROGRESS / PENDING_APPROVE / COMPLETED
    operator_id   NUMBER          NOT NULL,           -- 盘点人
    approved_by   NUMBER,                             -- 审批店长ID（W-13）
    approved_time TIMESTAMP,
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    complete_time TIMESTAMP,
    CONSTRAINT pk_inventory_checks PRIMARY KEY (check_id),
    CONSTRAINT uq_check_no UNIQUE (check_no),
    CONSTRAINT chk_ic_status CHECK (status IN ('IN_PROGRESS','PENDING_APPROVE','COMPLETED')),
    CONSTRAINT chk_ic_scope CHECK (check_scope IN ('ALL','CATEGORY'))
);
CREATE SEQUENCE seq_inventory_checks START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【27】库存盘点明细表
-- 对应功能: W-12~W-13
-- ============================================================
CREATE TABLE inventory_check_items (
    item_id          NUMBER          NOT NULL,
    check_id         NUMBER          NOT NULL,
    product_id       NUMBER          NOT NULL,
    sku_id           NUMBER,
    system_quantity  NUMBER          NOT NULL,        -- 账面库存（快照）
    actual_quantity  NUMBER,                          -- 实际盘点数量（W-12录入）
    difference       NUMBER GENERATED ALWAYS AS       -- 差异=实际-账面（虚拟列自动计算）
                     (actual_quantity - system_quantity) VIRTUAL,
    remark           VARCHAR2(200),                   -- 差异备注/原因（W-13）
    CONSTRAINT pk_check_items PRIMARY KEY (item_id),
    CONSTRAINT fk_ci_check FOREIGN KEY (check_id) REFERENCES inventory_checks(check_id),
    CONSTRAINT fk_ci_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_ci_sku FOREIGN KEY (sku_id) REFERENCES product_skus(sku_id)
);
CREATE SEQUENCE seq_check_items START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【28】报损记录表
-- 对应功能: W-09, D-17（近期报损汇总）
-- ============================================================
CREATE TABLE damage_records (
    damage_id     NUMBER          NOT NULL,
    damage_no     VARCHAR2(30)    NOT NULL,           -- 报损单号 DR202603230001
    product_id    NUMBER          NOT NULL,
    sku_id        NUMBER,
    quantity      NUMBER          NOT NULL,
    unit_cost     NUMBER(10,2)    DEFAULT 0,          -- 报损时成本价快照
    total_cost    NUMBER(12,2)    DEFAULT 0,          -- 总报损金额=quantity×unit_cost（D-17）
    reason        VARCHAR2(200)   NOT NULL,           -- 过期/破损/其他
    operator_id   NUMBER          NOT NULL,
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_damage_records PRIMARY KEY (damage_id),
    CONSTRAINT uq_damage_no UNIQUE (damage_no),
    CONSTRAINT fk_dr_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_dr_sku FOREIGN KEY (sku_id) REFERENCES product_skus(sku_id),
    CONSTRAINT chk_dr_qty CHECK (quantity >= 1)
);
CREATE SEQUENCE seq_damage_records START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【29】配送员表
-- 对应功能: B-40~B-43, P-01~P-03
-- ============================================================
CREATE TABLE delivery_persons (
    courier_id          NUMBER          NOT NULL,
    real_name           VARCHAR2(50)    NOT NULL,
    phone               VARCHAR2(20)    NOT NULL,
    password            VARCHAR2(100)   NOT NULL,
    total_delivery_count NUMBER         DEFAULT 0,   -- 累计配送单数（P-02）
    status              VARCHAR2(10)    DEFAULT 'active', -- active/inactive
    create_time         TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_delivery_persons PRIMARY KEY (courier_id),
    CONSTRAINT uq_courier_phone UNIQUE (phone),
    CONSTRAINT chk_courier_status CHECK (status IN ('active','inactive'))
);
CREATE SEQUENCE seq_delivery_persons START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【30】配送任务表
-- 对应功能: B-18, B-43, P-04~P-09
-- 状态: ASSIGNED→PICKED_UP→DELIVERED→（顾客确认后订单COMPLETED）
--                          →FAILED（P-08）
-- ============================================================
CREATE TABLE delivery_tasks (
    task_id           NUMBER          NOT NULL,
    order_id          NUMBER          NOT NULL,
    courier_id        NUMBER,
    status            VARCHAR2(20)    DEFAULT 'ASSIGNED',
    -- ASSIGNED / PICKED_UP / DELIVERED / FAILED
    fail_reason       VARCHAR2(200),                  -- 失败原因（P-08）
    assign_time       TIMESTAMP       DEFAULT SYSTIMESTAMP,
    pickup_time       TIMESTAMP,                      -- 取件时间（P-06）
    deliver_time      TIMESTAMP,                      -- 送达时间（P-07）
    CONSTRAINT pk_delivery_tasks PRIMARY KEY (task_id),
    CONSTRAINT uq_task_order UNIQUE (order_id),
    CONSTRAINT fk_dt_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_dt_courier FOREIGN KEY (courier_id) REFERENCES delivery_persons(courier_id),
    CONSTRAINT chk_dt_status CHECK (status IN ('ASSIGNED','PICKED_UP','DELIVERED','FAILED'))
);
CREATE SEQUENCE seq_delivery_tasks START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【31】收银班次表
-- 对应功能: K-01~K-04
-- ============================================================
CREATE TABLE cashier_shifts (
    shift_id          NUMBER          NOT NULL,
    cashier_id        NUMBER          NOT NULL,
    start_cash        NUMBER(10,2)    DEFAULT 0,      -- 备用金（K-01）
    end_cash          NUMBER(10,2),                   -- 清点现金（K-03）
    total_cash_amount NUMBER(12,2)    DEFAULT 0,      -- 班次现金收款合计（K-02/K-03）
    total_mock_amount NUMBER(12,2)    DEFAULT 0,      -- 班次模拟支付合计（K-02/K-03）
    total_order_count NUMBER          DEFAULT 0,      -- 班次订单数（K-02/K-03）
    cash_diff         NUMBER(12,2),                   -- 差额=清点现金-备用金-现金收款（K-03）
    start_time        TIMESTAMP       NOT NULL,
    end_time          TIMESTAMP,
    status            VARCHAR2(10)    DEFAULT 'OPEN', -- OPEN/CLOSED
    CONSTRAINT pk_cashier_shifts PRIMARY KEY (shift_id),
    CONSTRAINT fk_cs_cashier FOREIGN KEY (cashier_id) REFERENCES admin_users(admin_id),
    CONSTRAINT chk_cs_status CHECK (status IN ('OPEN','CLOSED'))
);
CREATE SEQUENCE seq_cashier_shifts START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【32】收银记录主表（每笔结账一条）
-- 对应功能: K-10~K-12
-- ============================================================
CREATE TABLE cashier_records (
    record_id         NUMBER          NOT NULL,
    shift_id          NUMBER          NOT NULL,
    user_id           NUMBER,                         -- 会员可NULL（K-08识别会员）
    member_phone      VARCHAR2(20),                   -- 会员手机号快照（非会员为NULL）
    total_amount      NUMBER(12,2)    NOT NULL,       -- 商品合计
    discount_amount   NUMBER(12,2)    DEFAULT 0,      -- 优惠抵扣
    coupon_id         NUMBER,                         -- 使用的优惠券（K-09）
    uc_id             NUMBER,                         -- 用户优惠券ID（K-09核销）
    pay_amount        NUMBER(12,2)    NOT NULL,       -- 实付金额
    pay_method        VARCHAR2(20)    DEFAULT 'CASH', -- CASH/MOCK_CARD
    received_amount   NUMBER(12,2),                   -- 实收金额（K-10，现金时必填）
    change_amount     NUMBER(12,2)    DEFAULT 0,      -- 找零金额（K-10）
    cashier_id        NUMBER          NOT NULL,
    create_time       TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_cashier_records PRIMARY KEY (record_id),
    CONSTRAINT fk_cr_shift FOREIGN KEY (shift_id) REFERENCES cashier_shifts(shift_id),
    CONSTRAINT fk_cr_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_cr_cashier FOREIGN KEY (cashier_id) REFERENCES admin_users(admin_id),
    CONSTRAINT fk_cr_coupon FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id),
    CONSTRAINT chk_cr_pay CHECK (pay_method IN ('CASH','MOCK_CARD'))
);
CREATE SEQUENCE seq_cashier_records START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 【33】收银明细表（每笔结账的商品明细）
-- 对应功能: K-06/K-07 收银清单明细
-- ============================================================
CREATE TABLE cashier_record_items (
    item_id        NUMBER          NOT NULL,
    record_id      NUMBER          NOT NULL,
    product_id     NUMBER          NOT NULL,
    sku_id         NUMBER,
    product_name   VARCHAR2(100)   NOT NULL,          -- 商品名快照
    sku_name       VARCHAR2(100),                     -- 规格名快照
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
-- 【34】操作审计日志表
-- 对应功能: B-44（增删改操作记录，含前后数据快照）
-- ============================================================
CREATE TABLE audit_logs (
    log_id        NUMBER          NOT NULL,
    operator_id   NUMBER          NOT NULL,
    operator_name VARCHAR2(50)    NOT NULL,
    module        VARCHAR2(50)    NOT NULL,           -- 操作模块（PRODUCT/ORDER/USER...）
    action        VARCHAR2(20)    NOT NULL,           -- CREATE/UPDATE/DELETE/STATUS_CHANGE
    target_table  VARCHAR2(50),                       -- 操作的表名
    target_id     NUMBER,                             -- 操作的记录ID
    before_data   CLOB,                               -- 操作前JSON快照
    after_data    CLOB,                               -- 操作后JSON快照
    ip_address    VARCHAR2(50),
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_audit_logs PRIMARY KEY (log_id),
    CONSTRAINT chk_al_action CHECK (action IN ('CREATE','UPDATE','DELETE','STATUS_CHANGE'))
);
CREATE SEQUENCE seq_audit_logs START WITH 1 INCREMENT BY 1 NOCACHE;

PROMPT ============================================================
PROMPT STEP 3: 创建索引
PROMPT ============================================================

-- 商品相关
CREATE INDEX idx_prod_category    ON products(category_id);
CREATE INDEX idx_prod_brand       ON products(brand_id);
CREATE INDEX idx_prod_supplier    ON products(supplier_id);
CREATE INDEX idx_prod_status      ON products(status, is_deleted);
CREATE INDEX idx_prod_name        ON products(product_name);
CREATE INDEX idx_prod_barcode     ON products(barcode);
CREATE INDEX idx_prod_recommend   ON products(is_recommend);
CREATE INDEX idx_prod_stock_warn  ON products(stock, stock_warning);  -- 低库存查询

-- SKU
CREATE INDEX idx_sku_product      ON product_skus(product_id);

-- 订单相关
CREATE INDEX idx_order_user       ON orders(user_id);
CREATE INDEX idx_order_status     ON orders(status);
CREATE INDEX idx_order_time       ON orders(create_time);
CREATE INDEX idx_order_no         ON orders(order_no);
CREATE INDEX idx_order_source     ON orders(source);
CREATE INDEX idx_order_pay_time   ON orders(pay_time);               -- 销售统计按日期查询

-- 订单明细
CREATE INDEX idx_oi_order         ON order_items(order_id);
CREATE INDEX idx_oi_product       ON order_items(product_id);        -- 商品销售排行 D-08

-- 订单状态日志
CREATE INDEX idx_osl_order        ON order_status_logs(order_id);

-- 售后
CREATE INDEX idx_as_order         ON after_sales(order_id);
CREATE INDEX idx_as_status        ON after_sales(status);

-- 购物车
CREATE INDEX idx_cart_user        ON cart(user_id);

-- 库存日志
CREATE INDEX idx_il_product       ON inventory_logs(product_id);
CREATE INDEX idx_il_type_time     ON inventory_logs(log_type, create_time);

-- 消息
CREATE INDEX idx_msg_user_read    ON messages(user_id, is_read);

-- 评价
CREATE INDEX idx_rev_product      ON reviews(product_id, is_hidden);

-- 积分
CREATE INDEX idx_pl_user          ON points_logs(user_id);

-- 收藏
CREATE INDEX idx_fav_user         ON favorites(user_id);

-- 用户优惠券
CREATE INDEX idx_uc_user_status   ON user_coupons(user_id, status);
CREATE INDEX idx_uc_coupon        ON user_coupons(coupon_id);

-- 采购
CREATE INDEX idx_po_supplier      ON purchase_orders(supplier_id);
CREATE INDEX idx_po_status        ON purchase_orders(status);
CREATE INDEX idx_poi_po           ON purchase_order_items(po_id);

-- 库存盘点
CREATE INDEX idx_ci_check         ON inventory_check_items(check_id);

-- 报损
CREATE INDEX idx_dr_product       ON damage_records(product_id);
CREATE INDEX idx_dr_time          ON damage_records(create_time);    -- D-17近30天统计

-- 配送任务
CREATE INDEX idx_dt_courier       ON delivery_tasks(courier_id);
CREATE INDEX idx_dt_status        ON delivery_tasks(status);

-- 收银
CREATE INDEX idx_cr_shift         ON cashier_records(shift_id);
CREATE INDEX idx_cri_record       ON cashier_record_items(record_id);

-- 审计
CREATE INDEX idx_al_operator      ON audit_logs(operator_id);
CREATE INDEX idx_al_module_time   ON audit_logs(module, create_time);

-- 活动
CREATE INDEX idx_act_type_status  ON activities(activity_type, status, start_time, end_time);
CREATE INDEX idx_ap_activity      ON activity_products(activity_id);

-- 满减规则
CREATE INDEX idx_frr_activity     ON full_reduce_rules(activity_id);

PROMPT ============================================================
PROMPT STEP 4: 初始化基础数据
PROMPT ============================================================

-- ============================================================
-- ① 管理员账户（实际部署时后端对密码进行MD5加密，此处为演示MD5密文）
-- ============================================================
INSERT INTO admin_users (admin_id, username, password, real_name, phone, role, status)
VALUES (seq_admin_users.NEXTVAL, 'admin', '0192023a7bbd73250516f069df18b500', '系统管理员', '13000000001', 'SUPER_ADMIN', 'active');

INSERT INTO admin_users (admin_id, username, password, real_name, phone, role, status)
VALUES (seq_admin_users.NEXTVAL, 'manager', '0795151defba7a4b5dfa89170de46277', '张店长', '13000000002', 'MANAGER', 'active');

INSERT INTO admin_users (admin_id, username, password, real_name, phone, role, status)
VALUES (seq_admin_users.NEXTVAL, 'cashier01', 'dbb8c54ee649f8af049357a5f99cede6', '收银员小王', '13000000003', 'CASHIER', 'active');

INSERT INTO admin_users (admin_id, username, password, real_name, phone, role, status)
VALUES (seq_admin_users.NEXTVAL, 'warehouse01', 'e7bca0b30b7fdb9f0ecbb7832c5f5348', '仓管小李', '13000000004', 'WAREHOUSE', 'active');

INSERT INTO admin_users (admin_id, username, password, real_name, phone, role, status)
VALUES (seq_admin_users.NEXTVAL, 'product01', '4751368fbef4cc9420716a698d0c393a', '商品专员小陈', '13000000005', 'PRODUCT', 'active');

INSERT INTO admin_users (admin_id, username, password, real_name, phone, role, status)
VALUES (seq_admin_users.NEXTVAL, 'service01', '3df2c034f564ae53106c928b7278d1ca', '客服小赵', '13000000006', 'SERVICE', 'active');

-- ============================================================
-- ② 配送员
-- ============================================================
INSERT INTO delivery_persons (courier_id, real_name, phone, password, status)
VALUES (seq_delivery_persons.NEXTVAL, '张配送', '13900000001', '1ed4a8186a0606351d36115f28cc3793', 'active');

INSERT INTO delivery_persons (courier_id, real_name, phone, password, status)
VALUES (seq_delivery_persons.NEXTVAL, '李配送', '13900000002', '1ed4a8186a0606351d36115f28cc3793', 'active');

INSERT INTO delivery_persons (courier_id, real_name, phone, password, status)
VALUES (seq_delivery_persons.NEXTVAL, '王配送', '13900000003', '1ed4a8186a0606351d36115f28cc3793', 'active');

-- ============================================================
-- ③ 顾客用户
-- ============================================================
INSERT INTO users (user_id, username, password, nickname, real_name, phone, member_level, points, status)
VALUES (seq_users.NEXTVAL, '13800138001', '6ad14ba9986e3615423dfca256d04e3f', '小明', '张三', '13800138001', 'SILVER', 520, 'active');

INSERT INTO users (user_id, username, password, nickname, real_name, phone, member_level, points, status)
VALUES (seq_users.NEXTVAL, '13800138002', '6ad14ba9986e3615423dfca256d04e3f', '小红', '李四', '13800138002', 'GOLD', 1200, 'active');

INSERT INTO users (user_id, username, password, nickname, real_name, phone, member_level, points, status)
VALUES (seq_users.NEXTVAL, '13800138003', '6ad14ba9986e3615423dfca256d04e3f', '测试用户', '王五', '13800138003', 'NORMAL', 0, 'active');

-- ============================================================
-- ④ 一级分类
-- ============================================================
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

-- ⑤ 二级分类（parent_id = 1~5）
INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 1, '饼干糕点', 1, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 1, '方便速食', 2, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 2, '碳酸饮料', 1, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 2, '矿泉水', 2, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 2, '茶饮料', 3, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 3, '洗护用品', 1, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 3, '清洁用品', 2, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 4, '蔬菜', 1, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 4, '水果', 2, 'active');

-- ============================================================
-- ⑥ 品牌
-- ============================================================
INSERT INTO brands (brand_id, brand_name, status) VALUES (seq_brands.NEXTVAL, '可口可乐', 'active');
INSERT INTO brands (brand_id, brand_name, status) VALUES (seq_brands.NEXTVAL, '农夫山泉', 'active');
INSERT INTO brands (brand_id, brand_name, status) VALUES (seq_brands.NEXTVAL, '康师傅', 'active');
INSERT INTO brands (brand_id, brand_name, status) VALUES (seq_brands.NEXTVAL, '统一', 'active');
INSERT INTO brands (brand_id, brand_name, status) VALUES (seq_brands.NEXTVAL, '海飞丝', 'active');
INSERT INTO brands (brand_id, brand_name, status) VALUES (seq_brands.NEXTVAL, '高露洁', 'active');
INSERT INTO brands (brand_id, brand_name, status) VALUES (seq_brands.NEXTVAL, '奥利奥', 'active');
INSERT INTO brands (brand_id, brand_name, status) VALUES (seq_brands.NEXTVAL, '乐事', 'active');

-- ============================================================
-- ⑦ 供应商
-- ============================================================
INSERT INTO suppliers (supplier_id, supplier_name, contact_name, contact_phone, email, address, bank_account, payment_days, status)
VALUES (seq_suppliers.NEXTVAL, '可口可乐公司', '王经理', '020-88888881',
        'wangmgr@coke.com', '广州市天河区可口可乐路1号', '建行6217000000000001', 30, 'active');

INSERT INTO suppliers (supplier_id, supplier_name, contact_name, contact_phone, email, address, bank_account, payment_days, status)
VALUES (seq_suppliers.NEXTVAL, '农夫山泉股份', '李经理', '0571-88888882',
        'limgr@nongfu.com', '杭州市拱墅区农夫山泉路2号', '工行6222000000000002', 30, 'active');

INSERT INTO suppliers (supplier_id, supplier_name, contact_name, contact_phone, email, address, bank_account, payment_days, status)
VALUES (seq_suppliers.NEXTVAL, '顶益食品有限公司', '张经理', '022-88888883',
        'zhangmgr@master.com', '天津市河西区康师傅路3号', '招行6225000000000003', 45, 'active');

-- ============================================================
-- ⑧ 商品（各品类示例，category_id基于序列从1开始）
-- 一级分类：食品=1, 饮料=2, 日用品=3, 生鲜=4, 酒水=5
-- 二级分类：饼干糕点=6, 方便速食=7, 碳酸饮料=8, 矿泉水=9, 茶饮料=10, 洗护用品=11
-- 品牌：可口可乐=1, 农夫山泉=2, 康师傅=3, 统一=4, 海飞丝=5, 高露洁=6, 奥利奥=7, 乐事=8
-- 供应商：可口可乐公司=1, 农夫山泉股份=2, 顶益食品=3
-- ============================================================
INSERT INTO products (product_id, product_name, barcode, category_id, brand_id, supplier_id,
    cost_price, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, '可口可乐 330ml', '6901234500001', 8, 1, 1,
    2.00, 4.00, 3.50, 500, 50, '罐', 9999, 4.8, 1, 'active');

INSERT INTO products (product_id, product_name, barcode, category_id, brand_id, supplier_id,
    cost_price, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, '农夫山泉 550ml', '6901234500002', 9, 2, 2,
    1.00, 2.50, 2.00, 1000, 100, '瓶', 8888, 4.9, 1, 'active');

INSERT INTO products (product_id, product_name, barcode, category_id, brand_id, supplier_id,
    cost_price, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, '康师傅方便面 红烧牛肉', '6901234500003', 7, 3, 3,
    2.50, 5.50, 4.50, 300, 30, '包', 5000, 4.7, 1, 'active');

INSERT INTO products (product_id, product_name, barcode, category_id, brand_id, supplier_id,
    cost_price, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, '统一冰红茶 500ml', '6901234500004', 10, 4, 2,
    1.50, 3.50, 3.00, 400, 40, '瓶', 3000, 4.6, 0, 'active');

INSERT INTO products (product_id, product_name, barcode, category_id, brand_id, supplier_id,
    cost_price, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, '海飞丝洗发水 400ml', '6901234500005', 11, 5, 1,
    15.00, 35.00, 29.90, 150, 20, '瓶', 1200, 4.5, 0, 'active');

INSERT INTO products (product_id, product_name, barcode, category_id, brand_id, supplier_id,
    cost_price, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, '高露洁牙膏 120g', '6901234500006', 11, 6, 1,
    6.00, 15.00, 12.00, 200, 20, '支', 2000, 4.7, 0, 'active');

INSERT INTO products (product_id, product_name, barcode, category_id, brand_id, supplier_id,
    cost_price, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, '奥利奥饼干 388g', '6901234500007', 6, 7, 1,
    8.00, 20.00, 16.90, 250, 25, '盒', 4500, 4.8, 1, 'active');

INSERT INTO products (product_id, product_name, barcode, category_id, brand_id, supplier_id,
    cost_price, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, '乐事薯片 原味 104g', '6901234500008', 6, 8, 1,
    3.00, 8.00, 6.50, 300, 30, '包', 6000, 4.6, 0, 'active');

-- 多规格商品示例（可乐多规格：330ml/500ml/1.25L）
INSERT INTO products (product_id, product_name, barcode, category_id, brand_id, supplier_id,
    cost_price, original_price, price, stock, stock_warning, unit, sales_count, avg_rating,
    is_recommend, has_sku, status)
VALUES (seq_products.NEXTVAL, '可口可乐（多规格）', NULL, 8, 1, 1,
    0, 5.00, 3.50, 0, 0, '瓶/罐', 500, 4.8, 0, 1, 'active');

-- 多规格商品的SKU（product_id=1009，即第9个商品）
INSERT INTO product_skus (sku_id, product_id, sku_name, sku_spec, price, original_price, cost_price, stock, barcode, sort_order)
VALUES (seq_product_skus.NEXTVAL, 1008, '330ml 罐装', '{"容量":"330ml","包装":"罐装"}', 3.50, 4.00, 2.00, 200, '6901234509001', 1);

INSERT INTO product_skus (sku_id, product_id, sku_name, sku_spec, price, original_price, cost_price, stock, barcode, sort_order)
VALUES (seq_product_skus.NEXTVAL, 1008, '500ml 瓶装', '{"容量":"500ml","包装":"瓶装"}', 4.50, 5.50, 2.50, 200, '6901234509002', 2);

INSERT INTO product_skus (sku_id, product_id, sku_name, sku_spec, price, original_price, cost_price, stock, barcode, sort_order)
VALUES (seq_product_skus.NEXTVAL, 1008, '1.25L 大瓶', '{"容量":"1.25L","包装":"瓶装"}', 6.50, 8.00, 3.50, 100, '6901234509003', 3);

-- ============================================================
-- ⑨ 轮播图
-- ============================================================
INSERT INTO banners (banner_id, image_url, title, link_type, sort_order, status)
VALUES (seq_banners.NEXTVAL, '/uploads/banner/banner1.jpg', '夏日饮品大促', 'CATEGORY', 1, 'active');
INSERT INTO banners (banner_id, image_url, title, link_type, sort_order, status)
VALUES (seq_banners.NEXTVAL, '/uploads/banner/banner2.jpg', '新品上市', 'NONE', 2, 'active');
INSERT INTO banners (banner_id, image_url, title, link_type, sort_order, status)
VALUES (seq_banners.NEXTVAL, '/uploads/banner/banner3.jpg', '会员专享', 'NONE', 3, 'active');

-- ============================================================
-- ⑩ 优惠券示例（含新人券、折扣券、品类券）
-- ============================================================
-- 新人满减券
INSERT INTO coupons (coupon_id, coupon_name, description, coupon_type, face_value, min_amount,
    total_count, per_limit, start_time, end_time, status)
VALUES (seq_coupons.NEXTVAL, '新人满50减10', '新用户注册专享，满50元可用',
    'FULL_REDUCE', 10.00, 50.00, 1000, 1,
    SYSTIMESTAMP, SYSTIMESTAMP + INTERVAL '30' DAY, 'active');

-- 全场九折券
INSERT INTO coupons (coupon_id, coupon_name, description, coupon_type, face_value, min_amount,
    total_count, per_limit, start_time, end_time, status)
VALUES (seq_coupons.NEXTVAL, '全场九折券', '全场商品九折优惠，无门槛',
    'DISCOUNT', 0.9, 0.00, -1, 1,
    SYSTIMESTAMP, SYSTIMESTAMP + INTERVAL '7' DAY, 'active');

-- 饮料品类满20减5
INSERT INTO coupons (coupon_id, coupon_name, description, coupon_type, face_value, min_amount,
    category_id, total_count, per_limit, start_time, end_time, status)
VALUES (seq_coupons.NEXTVAL, '饮料专区满20减5', '饮料品类专享，满20元减5元',
    'CATEGORY', 5.00, 20.00, 2, 500, 2,
    SYSTIMESTAMP, SYSTIMESTAMP + INTERVAL '14' DAY, 'active');

-- ============================================================
-- ⑪ 秒杀活动示例（含满减阶梯）
-- ============================================================
-- 秒杀活动
INSERT INTO activities (activity_id, activity_name, activity_type, scope_type,
    start_time, end_time, status)
VALUES (seq_activities.NEXTVAL, '周末秒杀特惠', 'SECKILL', 'ALL',
    SYSTIMESTAMP, SYSTIMESTAMP + INTERVAL '2' DAY, 'active');

-- 秒杀关联商品（可口可乐秒杀价2.9元）
INSERT INTO activity_products (id, activity_id, product_id, activity_price, activity_stock, sold_count)
VALUES (seq_activity_products.NEXTVAL, 1, 1000, 2.90, 100, 0);

-- 满减活动（全场满100减15，满200减35）
INSERT INTO activities (activity_id, activity_name, activity_type, scope_type,
    start_time, end_time, status)
VALUES (seq_activities.NEXTVAL, '全场阶梯满减', 'FULL_REDUCE', 'ALL',
    SYSTIMESTAMP, SYSTIMESTAMP + INTERVAL '30' DAY, 'active');

-- 满减阶梯规则
INSERT INTO full_reduce_rules (rule_id, activity_id, threshold, reduce_amount, sort_order)
VALUES (seq_full_reduce_rules.NEXTVAL, 2, 100.00, 15.00, 1);

INSERT INTO full_reduce_rules (rule_id, activity_id, threshold, reduce_amount, sort_order)
VALUES (seq_full_reduce_rules.NEXTVAL, 2, 200.00, 35.00, 2);

INSERT INTO full_reduce_rules (rule_id, activity_id, threshold, reduce_amount, sort_order)
VALUES (seq_full_reduce_rules.NEXTVAL, 2, 300.00, 60.00, 3);

COMMIT;

PROMPT ============================================================
PROMPT STEP 5: 验证建表结果
PROMPT ============================================================

SELECT
    table_name       AS "表名",
    num_rows         AS "行数(统计值)"
FROM user_tables
WHERE table_name IN (
    'USERS','ADMIN_USERS','CATEGORIES','BRANDS','SUPPLIERS',
    'PRODUCTS','PRODUCT_SKUS','PRODUCT_IMAGES',
    'ADDRESSES','CART',
    'COUPONS','USER_COUPONS',
    'ACTIVITIES','ACTIVITY_PRODUCTS','FULL_REDUCE_RULES',
    'BANNERS',
    'ORDERS','ORDER_ITEMS','ORDER_STATUS_LOGS',
    'AFTER_SALES','REVIEWS','FAVORITES',
    'MESSAGES','POINTS_LOGS',
    'INVENTORY_LOGS',
    'PURCHASE_ORDERS','PURCHASE_ORDER_ITEMS',
    'INVENTORY_CHECKS','INVENTORY_CHECK_ITEMS',
    'DAMAGE_RECORDS',
    'DELIVERY_PERSONS','DELIVERY_TASKS',
    'CASHIER_SHIFTS','CASHIER_RECORDS','CASHIER_RECORD_ITEMS',
    'AUDIT_LOGS'
)
ORDER BY table_name;

-- 统计各表实际行数
SELECT '=== 初始化数据验证 ===' AS INFO FROM DUAL;
SELECT 'admin_users(管理员):    ' || COUNT(*) FROM admin_users;
SELECT 'users(顾客):            ' || COUNT(*) FROM users;
SELECT 'categories(分类):       ' || COUNT(*) FROM categories;
SELECT 'brands(品牌):           ' || COUNT(*) FROM brands;
SELECT 'suppliers(供应商):      ' || COUNT(*) FROM suppliers;
SELECT 'products(商品):         ' || COUNT(*) FROM products;
SELECT 'product_skus(SKU):      ' || COUNT(*) FROM product_skus;
SELECT 'coupons(优惠券):        ' || COUNT(*) FROM coupons;
SELECT 'activities(活动):       ' || COUNT(*) FROM activities;
SELECT 'full_reduce_rules(满减规则): ' || COUNT(*) FROM full_reduce_rules;
SELECT 'banners(轮播图):        ' || COUNT(*) FROM banners;
SELECT 'delivery_persons(配送员): ' || COUNT(*) FROM delivery_persons;

PROMPT ============================================================
PROMPT 全部完成！共创建 34 张表 + 34 条序列
PROMPT 对应功能清单 v3.0 · 六端 · 32模块 · 162功能点
PROMPT ============================================================
