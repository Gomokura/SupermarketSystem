SET FEEDBACK OFF
SET ECHO OFF

-- ============================================================
-- STEP 1: Drop old tables (leaf to root order)
-- ============================================================
DECLARE
  PROCEDURE drop_if_exists(p_name VARCHAR2) IS
  BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE ' || p_name || ' CASCADE CONSTRAINTS PURGE';
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

-- Drop old sequences
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
PROMPT STEP 2: Create tables (v3.0)
PROMPT ============================================================

-- [1] Users table
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

-- [2] Admin users table
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
    CONSTRAINT chk_admin_role CHECK (role IN (
        'SUPER_ADMIN','MANAGER','PRODUCT','FINANCE','SERVICE','WAREHOUSE','CASHIER'
    )),
    CONSTRAINT chk_admin_status CHECK (status IN ('active','inactive'))
);
CREATE SEQUENCE seq_admin_users START WITH 1 INCREMENT BY 1 NOCACHE;

-- [3] Categories table (2-level: parent_id=0 is root)
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

-- [4] Brands table
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

-- [5] Suppliers table
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

-- [6] Products table
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

-- [7] Product SKUs table
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

-- [8] Product images table
CREATE TABLE product_images (
    image_id      NUMBER          NOT NULL,
    product_id    NUMBER          NOT NULL,
    image_url     VARCHAR2(500)   NOT NULL,
    sort_order    NUMBER          DEFAULT 0,
    CONSTRAINT pk_product_images PRIMARY KEY (image_id),
    CONSTRAINT fk_img_product FOREIGN KEY (product_id) REFERENCES products(product_id)
);
CREATE SEQUENCE seq_product_images START WITH 1 INCREMENT BY 1 NOCACHE;

-- [9] Addresses table
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

-- [10] Cart table
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

-- [11] Coupons table
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

-- [12] User coupons table
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

-- [13] Activities table (seckill / full-reduce)
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
    CONSTRAINT chk_act_type CHECK (activity_type IN ('SECKILL','FULL_REDUCE')),
    CONSTRAINT chk_act_status CHECK (status IN ('active','inactive')),
    CONSTRAINT chk_act_scope CHECK (scope_type IN ('ALL','CATEGORY'))
);
CREATE SEQUENCE seq_activities START WITH 1 INCREMENT BY 1 NOCACHE;

-- [13-1] Full-reduce rules table
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

-- [13-2] Activity products table (seckill items)
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

-- [14] Banners table
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

-- [15] Orders table
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
    CONSTRAINT chk_order_status CHECK (status IN (
        'PENDING_PAY','PAID','PENDING_SHIP','SHIPPING',
        'PENDING_RECEIVED','COMPLETED','CANCELLED','REFUNDED'
    )),
    CONSTRAINT chk_order_source CHECK (source IN ('ONLINE','CASHIER')),
    CONSTRAINT chk_order_pay CHECK (pay_method IN ('MOCK','CASH','MOCK_CARD'))
);
CREATE SEQUENCE seq_orders START WITH 1 INCREMENT BY 1 NOCACHE;

-- [16] Order items table
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

-- [17] Order status logs table
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

-- [18] After-sales table
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
    CONSTRAINT chk_as_type CHECK (as_type IN ('REFUND','EXCHANGE')),
    CONSTRAINT chk_as_status CHECK (status IN ('PENDING','APPROVED','REJECTED','COMPLETED'))
);
CREATE SEQUENCE seq_after_sales START WITH 1 INCREMENT BY 1 NOCACHE;

-- [19] Reviews table
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

-- [20] Favorites table
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

-- [21] Messages table
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

-- [22] Points logs table
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
    CONSTRAINT fk_pl_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);
CREATE SEQUENCE seq_points_logs START WITH 1 INCREMENT BY 1 NOCACHE;

-- [23] Inventory logs table
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
    CONSTRAINT chk_il_type CHECK (log_type IN (
        'PURCHASE_IN','ORDER_OUT','DAMAGE','CHECK_ADJUST','MANUAL'
    ))
);
CREATE SEQUENCE seq_inventory_logs START WITH 1 INCREMENT BY 1 NOCACHE;

-- [24] Purchase orders table
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
    CONSTRAINT chk_po_status CHECK (status IN (
        'DRAFT','SUBMITTED','APPROVED','PARTIALLY_ARRIVED','COMPLETED','CANCELLED'
    ))
);
CREATE SEQUENCE seq_purchase_orders START WITH 1 INCREMENT BY 1 NOCACHE;

-- [25] Purchase order items table
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

-- [26] Inventory checks table
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
    CONSTRAINT chk_ic_status CHECK (status IN ('IN_PROGRESS','PENDING_APPROVE','COMPLETED')),
    CONSTRAINT chk_ic_scope CHECK (check_scope IN ('ALL','CATEGORY'))
);
CREATE SEQUENCE seq_inventory_checks START WITH 1 INCREMENT BY 1 NOCACHE;

-- [27] Inventory check items table
CREATE TABLE inventory_check_items (
    item_id          NUMBER          NOT NULL,
    check_id         NUMBER          NOT NULL,
    product_id       NUMBER          NOT NULL,
    sku_id           NUMBER,
    system_quantity  NUMBER          NOT NULL,
    actual_quantity  NUMBER,
    difference       NUMBER GENERATED ALWAYS AS
                     (actual_quantity - system_quantity) VIRTUAL,
    remark           VARCHAR2(200),
    CONSTRAINT pk_check_items PRIMARY KEY (item_id),
    CONSTRAINT fk_ci_check FOREIGN KEY (check_id) REFERENCES inventory_checks(check_id),
    CONSTRAINT fk_ci_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_ci_sku FOREIGN KEY (sku_id) REFERENCES product_skus(sku_id)
);
CREATE SEQUENCE seq_check_items START WITH 1 INCREMENT BY 1 NOCACHE;

-- [28] Damage records table
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
    CONSTRAINT chk_dr_qty CHECK (quantity >= 1)
);
CREATE SEQUENCE seq_damage_records START WITH 1 INCREMENT BY 1 NOCACHE;

-- [29] Delivery persons table
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

-- [30] Delivery tasks table
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

-- [31] Cashier shifts table
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

-- [32] Cashier records table
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
    CONSTRAINT chk_cr_pay CHECK (pay_method IN ('CASH','MOCK_CARD'))
);
CREATE SEQUENCE seq_cashier_records START WITH 1 INCREMENT BY 1 NOCACHE;

-- [33] Cashier record items table
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

-- [34] Audit logs table
CREATE TABLE audit_logs (
    log_id        NUMBER          NOT NULL,
    operator_id   NUMBER          NOT NULL,
    operator_name VARCHAR2(50)    NOT NULL,
    module        VARCHAR2(50)    NOT NULL,
    action        VARCHAR2(20)    NOT NULL,
    target_table  VARCHAR2(50),
    target_id     NUMBER,
    before_data   CLOB,
    after_data    CLOB,
    ip_address    VARCHAR2(50),
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_audit_logs PRIMARY KEY (log_id),
    CONSTRAINT chk_al_action CHECK (action IN ('CREATE','UPDATE','DELETE','STATUS_CHANGE'))
);
CREATE SEQUENCE seq_audit_logs START WITH 1 INCREMENT BY 1 NOCACHE;

PROMPT ============================================================
PROMPT STEP 3: Create indexes
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
CREATE INDEX idx_order_no         ON orders(order_no);
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
CREATE INDEX idx_al_operator      ON audit_logs(operator_id);
CREATE INDEX idx_al_module_time   ON audit_logs(module, create_time);
CREATE INDEX idx_act_type_status  ON activities(activity_type, status, start_time, end_time);
CREATE INDEX idx_ap_activity      ON activity_products(activity_id);
CREATE INDEX idx_frr_activity     ON full_reduce_rules(activity_id);

PROMPT ============================================================
PROMPT STEP 4: Insert initial data
PROMPT ============================================================

-- Admin accounts (plaintext passwords)
INSERT INTO admin_users (admin_id, username, password, real_name, phone, role, status)
VALUES (seq_admin_users.NEXTVAL, 'admin', 'admin123', 'System Admin', '13000000001', 'SUPER_ADMIN', 'active');

INSERT INTO admin_users (admin_id, username, password, real_name, phone, role, status)
VALUES (seq_admin_users.NEXTVAL, 'manager', 'admin123', 'Manager Zhang', '13000000002', 'MANAGER', 'active');

INSERT INTO admin_users (admin_id, username, password, real_name, phone, role, status)
VALUES (seq_admin_users.NEXTVAL, 'cashier01', 'admin123', 'Cashier Wang', '13000000003', 'CASHIER', 'active');

INSERT INTO admin_users (admin_id, username, password, real_name, phone, role, status)
VALUES (seq_admin_users.NEXTVAL, 'warehouse01', 'admin123', 'Warehouse Li', '13000000004', 'WAREHOUSE', 'active');

INSERT INTO admin_users (admin_id, username, password, real_name, phone, role, status)
VALUES (seq_admin_users.NEXTVAL, 'product01', 'admin123', 'Product Chen', '13000000005', 'PRODUCT', 'active');

INSERT INTO admin_users (admin_id, username, password, real_name, phone, role, status)
VALUES (seq_admin_users.NEXTVAL, 'service01', 'admin123', 'Service Zhao', '13000000006', 'SERVICE', 'active');

-- Delivery persons (plaintext passwords)
INSERT INTO delivery_persons (courier_id, real_name, phone, password, status)
VALUES (seq_delivery_persons.NEXTVAL, 'Zhang Courier', '13900000001', '123456', 'active');

INSERT INTO delivery_persons (courier_id, real_name, phone, password, status)
VALUES (seq_delivery_persons.NEXTVAL, 'Li Courier', '13900000002', '123456', 'active');

INSERT INTO delivery_persons (courier_id, real_name, phone, password, status)
VALUES (seq_delivery_persons.NEXTVAL, 'Wang Courier', '13900000003', '123456', 'active');

-- Customer users (plaintext password: 123456)
INSERT INTO users (user_id, username, password, nickname, real_name, phone, member_level, points, status)
VALUES (seq_users.NEXTVAL, '13800138001', '123456', 'Xiaoming', 'Zhang San', '13800138001', 'SILVER', 520, 'active');

INSERT INTO users (user_id, username, password, nickname, real_name, phone, member_level, points, status)
VALUES (seq_users.NEXTVAL, '13800138002', '123456', 'Xiaohong', 'Li Si', '13800138002', 'GOLD', 1200, 'active');

INSERT INTO users (user_id, username, password, nickname, real_name, phone, member_level, points, status)
VALUES (seq_users.NEXTVAL, '13800138003', '123456', 'Test User', 'Wang Wu', '13800138003', 'NORMAL', 0, 'active');

-- Categories (root level)
INSERT INTO categories (category_id, parent_id, category_name, sort_order, status, description)
VALUES (seq_categories.NEXTVAL, 0, 'Food', 1, 'active', 'Snacks and food items');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status, description)
VALUES (seq_categories.NEXTVAL, 0, 'Drinks', 2, 'active', 'Beverages');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status, description)
VALUES (seq_categories.NEXTVAL, 0, 'Daily Goods', 3, 'active', 'Daily necessities');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status, description)
VALUES (seq_categories.NEXTVAL, 0, 'Fresh', 4, 'active', 'Fresh produce and meat');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status, description)
VALUES (seq_categories.NEXTVAL, 0, 'Alcohol', 5, 'active', 'Beer wine spirits');

-- Sub-categories (parent_id = 1..5)
INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 1, 'Biscuits', 1, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 1, 'Instant Noodles', 2, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 2, 'Soda', 1, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 2, 'Water', 2, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 2, 'Tea Drinks', 3, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 3, 'Personal Care', 1, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 3, 'Cleaning', 2, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 4, 'Vegetables', 1, 'active');

INSERT INTO categories (category_id, parent_id, category_name, sort_order, status)
VALUES (seq_categories.NEXTVAL, 4, 'Fruits', 2, 'active');

-- Brands
INSERT INTO brands (brand_id, brand_name, status) VALUES (seq_brands.NEXTVAL, 'Coca-Cola', 'active');
INSERT INTO brands (brand_id, brand_name, status) VALUES (seq_brands.NEXTVAL, 'Nongfu Spring', 'active');
INSERT INTO brands (brand_id, brand_name, status) VALUES (seq_brands.NEXTVAL, 'Master Kong', 'active');
INSERT INTO brands (brand_id, brand_name, status) VALUES (seq_brands.NEXTVAL, 'Uni-President', 'active');
INSERT INTO brands (brand_id, brand_name, status) VALUES (seq_brands.NEXTVAL, 'Head Shoulders', 'active');
INSERT INTO brands (brand_id, brand_name, status) VALUES (seq_brands.NEXTVAL, 'Colgate', 'active');
INSERT INTO brands (brand_id, brand_name, status) VALUES (seq_brands.NEXTVAL, 'Oreo', 'active');
INSERT INTO brands (brand_id, brand_name, status) VALUES (seq_brands.NEXTVAL, 'Lays', 'active');

-- Suppliers
INSERT INTO suppliers (supplier_id, supplier_name, contact_name, contact_phone, email, address, bank_account, payment_days, status)
VALUES (seq_suppliers.NEXTVAL, 'Coca-Cola Co', 'Manager Wang', '020-88888881',
        'wangmgr@coke.com', 'Guangzhou Tianhe District No.1', 'CCB6217000000000001', 30, 'active');

INSERT INTO suppliers (supplier_id, supplier_name, contact_name, contact_phone, email, address, bank_account, payment_days, status)
VALUES (seq_suppliers.NEXTVAL, 'Nongfu Spring Co', 'Manager Li', '0571-88888882',
        'limgr@nongfu.com', 'Hangzhou Gongshui District No.2', 'ICBC6222000000000002', 30, 'active');

INSERT INTO suppliers (supplier_id, supplier_name, contact_name, contact_phone, email, address, bank_account, payment_days, status)
VALUES (seq_suppliers.NEXTVAL, 'Master Kong Food Co', 'Manager Zhang', '022-88888883',
        'zhangmgr@master.com', 'Tianjin Hexi District No.3', 'CMB6225000000000003', 45, 'active');

-- Products
-- category: Food=1, Drinks=2, DailyGoods=3, Fresh=4, Alcohol=5
-- sub: Biscuits=6, InstantNoodles=7, Soda=8, Water=9, TeaDrinks=10, PersonalCare=11
-- brands: Coca-Cola=1, NongfuSpring=2, MasterKong=3, UniPresident=4, HeadShoulders=5, Colgate=6, Oreo=7, Lays=8
-- suppliers: CocaColaCo=1, NongfuSpringCo=2, MasterKongCo=3
INSERT INTO products (product_id, product_name, barcode, category_id, brand_id, supplier_id,
    cost_price, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, 'Coca-Cola 330ml', '6901234500001', 8, 1, 1,
    2.00, 4.00, 3.50, 500, 50, 'Can', 9999, 4.8, 1, 'active');

INSERT INTO products (product_id, product_name, barcode, category_id, brand_id, supplier_id,
    cost_price, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, 'Nongfu Spring 550ml', '6901234500002', 9, 2, 2,
    1.00, 2.50, 2.00, 1000, 100, 'Bottle', 8888, 4.9, 1, 'active');

INSERT INTO products (product_id, product_name, barcode, category_id, brand_id, supplier_id,
    cost_price, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, 'Master Kong Braised Beef Noodles', '6901234500003', 7, 3, 3,
    2.50, 5.50, 4.50, 300, 30, 'Pack', 5000, 4.7, 1, 'active');

INSERT INTO products (product_id, product_name, barcode, category_id, brand_id, supplier_id,
    cost_price, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, 'Uni-President Ice Tea 500ml', '6901234500004', 10, 4, 2,
    1.50, 3.50, 3.00, 400, 40, 'Bottle', 3000, 4.6, 0, 'active');

INSERT INTO products (product_id, product_name, barcode, category_id, brand_id, supplier_id,
    cost_price, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, 'Head Shoulders Shampoo 400ml', '6901234500005', 11, 5, 1,
    15.00, 35.00, 29.90, 150, 20, 'Bottle', 1200, 4.5, 0, 'active');

INSERT INTO products (product_id, product_name, barcode, category_id, brand_id, supplier_id,
    cost_price, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, 'Colgate Toothpaste 120g', '6901234500006', 11, 6, 1,
    6.00, 15.00, 12.00, 200, 20, 'Tube', 2000, 4.7, 0, 'active');

INSERT INTO products (product_id, product_name, barcode, category_id, brand_id, supplier_id,
    cost_price, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, 'Oreo Cookies 388g', '6901234500007', 6, 7, 1,
    8.00, 20.00, 16.90, 250, 25, 'Box', 4500, 4.8, 1, 'active');

INSERT INTO products (product_id, product_name, barcode, category_id, brand_id, supplier_id,
    cost_price, original_price, price, stock, stock_warning, unit, sales_count, avg_rating, is_recommend, status)
VALUES (seq_products.NEXTVAL, 'Lays Original Chips 104g', '6901234500008', 6, 8, 1,
    3.00, 8.00, 6.50, 300, 30, 'Pack', 6000, 4.6, 0, 'active');

-- Multi-SKU product (Coca-Cola in multiple sizes)
INSERT INTO products (product_id, product_name, barcode, category_id, brand_id, supplier_id,
    cost_price, original_price, price, stock, stock_warning, unit, sales_count, avg_rating,
    is_recommend, has_sku, status)
VALUES (seq_products.NEXTVAL, 'Coca-Cola Multi-Size', NULL, 8, 1, 1,
    0, 5.00, 3.50, 0, 0, 'Bottle/Can', 500, 4.8, 0, 1, 'active');

-- SKUs for product_id=1008 (9th product, seq starts at 1000)
INSERT INTO product_skus (sku_id, product_id, sku_name, sku_spec, price, original_price, cost_price, stock, barcode, sort_order)
VALUES (seq_product_skus.NEXTVAL, 1008, '330ml Can', '{"size":"330ml","package":"Can"}', 3.50, 4.00, 2.00, 200, '6901234509001', 1);

INSERT INTO product_skus (sku_id, product_id, sku_name, sku_spec, price, original_price, cost_price, stock, barcode, sort_order)
VALUES (seq_product_skus.NEXTVAL, 1008, '500ml Bottle', '{"size":"500ml","package":"Bottle"}', 4.50, 5.50, 2.50, 200, '6901234509002', 2);

INSERT INTO product_skus (sku_id, product_id, sku_name, sku_spec, price, original_price, cost_price, stock, barcode, sort_order)
VALUES (seq_product_skus.NEXTVAL, 1008, '1.25L Large', '{"size":"1.25L","package":"Bottle"}', 6.50, 8.00, 3.50, 100, '6901234509003', 3);

-- Banners
INSERT INTO banners (banner_id, image_url, title, link_type, sort_order, status)
VALUES (seq_banners.NEXTVAL, '/uploads/banner/banner1.jpg', 'Summer Drinks Sale', 'CATEGORY', 1, 'active');
INSERT INTO banners (banner_id, image_url, title, link_type, sort_order, status)
VALUES (seq_banners.NEXTVAL, '/uploads/banner/banner2.jpg', 'New Arrivals', 'NONE', 2, 'active');
INSERT INTO banners (banner_id, image_url, title, link_type, sort_order, status)
VALUES (seq_banners.NEXTVAL, '/uploads/banner/banner3.jpg', 'Member Exclusive', 'NONE', 3, 'active');

-- Coupons
INSERT INTO coupons (coupon_id, coupon_name, description, coupon_type, face_value, min_amount,
    total_count, per_limit, start_time, end_time, status)
VALUES (seq_coupons.NEXTVAL, 'New User 50-10', 'New user coupon: 10 off when spend 50+',
    'FULL_REDUCE', 10.00, 50.00, 1000, 1,
    SYSTIMESTAMP, SYSTIMESTAMP + INTERVAL '30' DAY, 'active');

INSERT INTO coupons (coupon_id, coupon_name, description, coupon_type, face_value, min_amount,
    total_count, per_limit, start_time, end_time, status)
VALUES (seq_coupons.NEXTVAL, '10% Off All', 'All products 10% off, no minimum',
    'DISCOUNT', 0.9, 0.00, -1, 1,
    SYSTIMESTAMP, SYSTIMESTAMP + INTERVAL '7' DAY, 'active');

INSERT INTO coupons (coupon_id, coupon_name, description, coupon_type, face_value, min_amount,
    category_id, total_count, per_limit, start_time, end_time, status)
VALUES (seq_coupons.NEXTVAL, 'Drinks 20-5', 'Drinks category: 5 off when spend 20+',
    'CATEGORY', 5.00, 20.00, 2, 500, 2,
    SYSTIMESTAMP, SYSTIMESTAMP + INTERVAL '14' DAY, 'active');

-- Activities (seckill)
INSERT INTO activities (activity_id, activity_name, activity_type, scope_type,
    start_time, end_time, status)
VALUES (seq_activities.NEXTVAL, 'Weekend Seckill', 'SECKILL', 'ALL',
    SYSTIMESTAMP, SYSTIMESTAMP + INTERVAL '2' DAY, 'active');

-- Seckill product (Coca-Cola at 2.9)
INSERT INTO activity_products (id, activity_id, product_id, activity_price, activity_stock, sold_count)
VALUES (seq_activity_products.NEXTVAL, 1, 1000, 2.90, 100, 0);

-- Full-reduce activity
INSERT INTO activities (activity_id, activity_name, activity_type, scope_type,
    start_time, end_time, status)
VALUES (seq_activities.NEXTVAL, 'Full Reduce All', 'FULL_REDUCE', 'ALL',
    SYSTIMESTAMP, SYSTIMESTAMP + INTERVAL '30' DAY, 'active');

-- Full-reduce rules
INSERT INTO full_reduce_rules (rule_id, activity_id, threshold, reduce_amount, sort_order)
VALUES (seq_full_reduce_rules.NEXTVAL, 2, 100.00, 15.00, 1);

INSERT INTO full_reduce_rules (rule_id, activity_id, threshold, reduce_amount, sort_order)
VALUES (seq_full_reduce_rules.NEXTVAL, 2, 200.00, 35.00, 2);

INSERT INTO full_reduce_rules (rule_id, activity_id, threshold, reduce_amount, sort_order)
VALUES (seq_full_reduce_rules.NEXTVAL, 2, 300.00, 60.00, 3);

COMMIT;

PROMPT ============================================================
PROMPT STEP 5: Verify results
PROMPT ============================================================

SELECT table_name FROM user_tables
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

SELECT 'admin_users: ' || COUNT(*) FROM admin_users;
SELECT 'users: ' || COUNT(*) FROM users;
SELECT 'categories: ' || COUNT(*) FROM categories;
SELECT 'brands: ' || COUNT(*) FROM brands;
SELECT 'suppliers: ' || COUNT(*) FROM suppliers;
SELECT 'products: ' || COUNT(*) FROM products;
SELECT 'product_skus: ' || COUNT(*) FROM product_skus;
SELECT 'coupons: ' || COUNT(*) FROM coupons;
SELECT 'activities: ' || COUNT(*) FROM activities;
SELECT 'banners: ' || COUNT(*) FROM banners;
SELECT 'delivery_persons: ' || COUNT(*) FROM delivery_persons;

PROMPT Done! 34 tables + 34 sequences created.
