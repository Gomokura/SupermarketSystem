"""
run_sql.py  —  用 cx_Oracle / oracledb 执行建表脚本，完全绕开 sqlplus 编码问题
执行方式: python run_sql.py
"""
import sys

# ── 优先用 oracledb（Thin 模式，无需 Oracle Client）──────────────────────────
try:
    import oracledb as cx
    cx.init_oracle_client()          # Thin 模式不需要这行，报错也没关系
except Exception:
    try:
        import oracledb as cx
    except ImportError:
        try:
            import cx_Oracle as cx
        except ImportError:
            print("正在安装 oracledb ...")
            import subprocess
            subprocess.check_call([sys.executable, "-m", "pip", "install", "oracledb"])
            import oracledb as cx

# ── 连接参数 ──────────────────────────────────────────────────────────────────
DSN  = "127.0.0.1:1521/XE"
USER = "system"
PWD  = "123456"

print(f"连接 Oracle {DSN} ...")
con = cx.connect(user=USER, password=PWD, dsn=DSN)
cur = con.cursor()
print("连接成功！\n")

# ── 工具函数 ──────────────────────────────────────────────────────────────────
def exec_ddl(sql: str, label: str = ""):
    """执行一条 DDL/DML，忽略'已存在'类错误，打印其他错误但继续"""
    sql = sql.strip()
    if not sql:
        return
    try:
        cur.execute(sql)
        if label:
            print(f"  ✓  {label}")
    except cx.DatabaseError as e:
        code = e.args[0].code if e.args else 0
        # ORA-00955 对象已存在 | ORA-02260 表已有主键 | ORA-01430 列已存在
        if code in (955, 2260, 1430, 1408):
            print(f"  ⚠  已存在，跳过: {label or sql[:60]}")
        else:
            print(f"  ✗  错误 ORA-{code:05d}: {label or sql[:80]}")
            print(f"         {e}")

def drop_table(name):
    try:
        cur.execute(f"DROP TABLE {name} CASCADE CONSTRAINTS PURGE")
        print(f"  🗑  删除表: {name}")
    except Exception:
        pass

def drop_seq(name):
    try:
        cur.execute(f"DROP SEQUENCE {name}")
        print(f"  🗑  删除序列: {name}")
    except Exception:
        pass

# ══════════════════════════════════════════════════════════════════════════════
# STEP 1  清理旧对象
# ══════════════════════════════════════════════════════════════════════════════
print("=" * 60)
print("STEP 1: 清理旧表和序列")
print("=" * 60)

tables = [
    "AUDIT_LOGS","CASHIER_RECORDS","CASHIER_SHIFTS","DAMAGE_RECORDS",
    "INVENTORY_CHECK_ITEMS","INVENTORY_CHECKS",
    "PURCHASE_ORDER_ITEMS","PURCHASE_ORDERS","INVENTORY_LOGS",
    "DELIVERY_TASKS","DELIVERY_PERSONS","BANNERS",
    "ACTIVITY_PRODUCTS","ACTIVITIES","USER_COUPONS","COUPONS",
    "FAVORITES","POINTS_LOGS","MESSAGES","REVIEWS",
    "AFTER_SALES","ORDER_ITEMS","ORDERS","CART","ADDRESSES",
    "PRODUCT_IMAGES","PRODUCTS","SUPPLIERS","BRANDS","CATEGORIES",
    "ADMIN_USERS","USERS",
]
seqs = [
    "SEQ_USERS","SEQ_ADMIN_USERS","SEQ_CATEGORIES","SEQ_BRANDS","SEQ_SUPPLIERS",
    "SEQ_PRODUCTS","SEQ_PRODUCT_IMAGES","SEQ_ADDRESSES","SEQ_CART",
    "SEQ_COUPONS","SEQ_USER_COUPONS","SEQ_ACTIVITIES","SEQ_ACTIVITY_PRODUCTS","SEQ_BANNERS",
    "SEQ_ORDERS","SEQ_ORDER_ITEMS","SEQ_AFTER_SALES","SEQ_REVIEWS",
    "SEQ_FAVORITES","SEQ_MESSAGES","SEQ_POINTS_LOGS","SEQ_INVENTORY_LOGS",
    "SEQ_PURCHASE_ORDERS","SEQ_PO_ITEMS",
    "SEQ_INVENTORY_CHECKS","SEQ_CHECK_ITEMS","SEQ_DAMAGE_RECORDS",
    "SEQ_DELIVERY_PERSONS","SEQ_DELIVERY_TASKS",
    "SEQ_CASHIER_SHIFTS","SEQ_CASHIER_RECORDS","SEQ_AUDIT_LOGS",
    # 旧序列名
    "SEQ_USER","SEQ_CATEGORY","SEQ_PRODUCT","SEQ_ORDER","SEQ_ORDER_ITEM",
    "SEQ_INVENTORY_LOG","SEQ_CART","SEQ_PROMOTION","SEQ_PROMO_PRODUCT",
    "SEQ_DELIVERY","SEQ_PAYMENT","SEQ_WAREHOUSING","SEQ_OUTBOUND","SEQ_ADDRESS","SEQ_BEHAVIOR",
]
for t in tables:
    drop_table(t)
for s in seqs:
    drop_seq(s)

# ══════════════════════════════════════════════════════════════════════════════
# STEP 2  建表
# ══════════════════════════════════════════════════════════════════════════════
print("\n" + "=" * 60)
print("STEP 2: 创建表结构（32张表）")
print("=" * 60)

ddl_list = [

# ── 1. users ────────────────────────────────────────────────────────────────
("""CREATE TABLE users (
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
    member_level  VARCHAR2(20)    DEFAULT 'NORMAL',
    points        NUMBER          DEFAULT 0,
    status        VARCHAR2(10)    DEFAULT 'active',
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    update_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT chk_users_gender CHECK (gender IN ('M','F','U')),
    CONSTRAINT chk_users_level CHECK (member_level IN ('NORMAL','SILVER','GOLD','DIAMOND')),
    CONSTRAINT chk_users_status CHECK (status IN ('active','banned'))
)""", "users"),
("CREATE SEQUENCE seq_users START WITH 1000 INCREMENT BY 1 NOCACHE", "seq_users"),

# ── 2. admin_users ──────────────────────────────────────────────────────────
("""CREATE TABLE admin_users (
    admin_id      NUMBER          NOT NULL,
    username      VARCHAR2(50)    NOT NULL,
    password      VARCHAR2(100)   NOT NULL,
    real_name     VARCHAR2(50),
    role          VARCHAR2(30)    NOT NULL,
    status        VARCHAR2(10)    DEFAULT 'active',
    last_login    TIMESTAMP,
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_admin_users PRIMARY KEY (admin_id),
    CONSTRAINT uq_admin_username UNIQUE (username),
    CONSTRAINT chk_admin_role CHECK (role IN ('SUPER_ADMIN','MANAGER','PRODUCT','FINANCE','SERVICE','WAREHOUSE','CASHIER')),
    CONSTRAINT chk_admin_status CHECK (status IN ('active','inactive'))
)""", "admin_users"),
("CREATE SEQUENCE seq_admin_users START WITH 1 INCREMENT BY 1 NOCACHE", "seq_admin_users"),

# ── 3. categories ───────────────────────────────────────────────────────────
("""CREATE TABLE categories (
    category_id   NUMBER          NOT NULL,
    parent_id     NUMBER          DEFAULT 0,
    category_name VARCHAR2(50)    NOT NULL,
    icon_url      VARCHAR2(500),
    sort_order    NUMBER          DEFAULT 0,
    status        VARCHAR2(10)    DEFAULT 'active',
    description   VARCHAR2(200),
    CONSTRAINT pk_categories PRIMARY KEY (category_id),
    CONSTRAINT chk_cat_status CHECK (status IN ('active','inactive'))
)""", "categories"),
("CREATE SEQUENCE seq_categories START WITH 1 INCREMENT BY 1 NOCACHE", "seq_categories"),

# ── 4. brands ───────────────────────────────────────────────────────────────
("""CREATE TABLE brands (
    brand_id      NUMBER          NOT NULL,
    brand_name    VARCHAR2(100)   NOT NULL,
    logo_url      VARCHAR2(500),
    description   VARCHAR2(500),
    status        VARCHAR2(10)    DEFAULT 'active',
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_brands PRIMARY KEY (brand_id),
    CONSTRAINT chk_brands_status CHECK (status IN ('active','inactive'))
)""", "brands"),
("CREATE SEQUENCE seq_brands START WITH 1 INCREMENT BY 1 NOCACHE", "seq_brands"),

# ── 5. suppliers ────────────────────────────────────────────────────────────
("""CREATE TABLE suppliers (
    supplier_id   NUMBER          NOT NULL,
    supplier_name VARCHAR2(100)   NOT NULL,
    contact_name  VARCHAR2(50),
    contact_phone VARCHAR2(20),
    address       VARCHAR2(200),
    status        VARCHAR2(10)    DEFAULT 'active',
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_suppliers PRIMARY KEY (supplier_id),
    CONSTRAINT chk_sup_status CHECK (status IN ('active','inactive'))
)""", "suppliers"),
("CREATE SEQUENCE seq_suppliers START WITH 1 INCREMENT BY 1 NOCACHE", "seq_suppliers"),

# ── 6. products ─────────────────────────────────────────────────────────────
("""CREATE TABLE products (
    product_id     NUMBER          NOT NULL,
    product_name   VARCHAR2(100)   NOT NULL,
    category_id    NUMBER,
    brand_id       NUMBER,
    supplier_id    NUMBER,
    description    CLOB,
    cover_image    VARCHAR2(500),
    unit           VARCHAR2(20),
    original_price NUMBER(10,2)   NOT NULL,
    price          NUMBER(10,2)   NOT NULL,
    stock          NUMBER          DEFAULT 0,
    stock_warning  NUMBER          DEFAULT 10,
    sales_count    NUMBER          DEFAULT 0,
    avg_rating     NUMBER(3,1)     DEFAULT 5.0,
    is_recommend   NUMBER(1)       DEFAULT 0,
    status         VARCHAR2(10)    DEFAULT 'active',
    create_time    TIMESTAMP       DEFAULT SYSTIMESTAMP,
    update_time    TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_products PRIMARY KEY (product_id),
    CONSTRAINT fk_prod_category FOREIGN KEY (category_id) REFERENCES categories(category_id),
    CONSTRAINT fk_prod_brand    FOREIGN KEY (brand_id)    REFERENCES brands(brand_id),
    CONSTRAINT fk_prod_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id),
    CONSTRAINT chk_prod_status  CHECK (status IN ('active','off_shelf')),
    CONSTRAINT chk_prod_price   CHECK (price >= 0),
    CONSTRAINT chk_prod_stock   CHECK (stock >= 0)
)""", "products"),
("CREATE SEQUENCE seq_products START WITH 1000 INCREMENT BY 1 NOCACHE", "seq_products"),

# ── 7. product_images ───────────────────────────────────────────────────────
("""CREATE TABLE product_images (
    image_id      NUMBER          NOT NULL,
    product_id    NUMBER          NOT NULL,
    image_url     VARCHAR2(500)   NOT NULL,
    sort_order    NUMBER          DEFAULT 0,
    CONSTRAINT pk_product_images PRIMARY KEY (image_id),
    CONSTRAINT fk_img_product FOREIGN KEY (product_id) REFERENCES products(product_id)
)""", "product_images"),
("CREATE SEQUENCE seq_product_images START WITH 1 INCREMENT BY 1 NOCACHE", "seq_product_images"),

# ── 8. addresses ────────────────────────────────────────────────────────────
("""CREATE TABLE addresses (
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
)""", "addresses"),
("CREATE SEQUENCE seq_addresses START WITH 1 INCREMENT BY 1 NOCACHE", "seq_addresses"),

# ── 9. cart ─────────────────────────────────────────────────────────────────
("""CREATE TABLE cart (
    cart_id       NUMBER          NOT NULL,
    user_id       NUMBER          NOT NULL,
    product_id    NUMBER          NOT NULL,
    quantity      NUMBER          DEFAULT 1,
    is_checked    NUMBER(1)       DEFAULT 1,
    add_time      TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_cart PRIMARY KEY (cart_id),
    CONSTRAINT fk_cart_user    FOREIGN KEY (user_id)    REFERENCES users(user_id),
    CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT uq_cart_user_product UNIQUE (user_id, product_id),
    CONSTRAINT chk_cart_qty     CHECK (quantity >= 1),
    CONSTRAINT chk_cart_checked CHECK (is_checked IN (0,1))
)""", "cart"),
("CREATE SEQUENCE seq_cart START WITH 1 INCREMENT BY 1 NOCACHE", "seq_cart"),

# ── 10. coupons ─────────────────────────────────────────────────────────────
("""CREATE TABLE coupons (
    coupon_id     NUMBER          NOT NULL,
    coupon_name   VARCHAR2(100)   NOT NULL,
    coupon_type   VARCHAR2(20)    NOT NULL,
    face_value    NUMBER(10,2),
    min_amount    NUMBER(10,2)    DEFAULT 0,
    category_id   NUMBER,
    total_count   NUMBER          DEFAULT -1,
    issued_count  NUMBER          DEFAULT 0,
    start_time    TIMESTAMP       NOT NULL,
    end_time      TIMESTAMP       NOT NULL,
    status        VARCHAR2(10)    DEFAULT 'active',
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_coupons PRIMARY KEY (coupon_id),
    CONSTRAINT chk_coup_type   CHECK (coupon_type IN ('FULL_REDUCE','DISCOUNT','CATEGORY')),
    CONSTRAINT chk_coup_status CHECK (status IN ('active','inactive'))
)""", "coupons"),
("CREATE SEQUENCE seq_coupons START WITH 1 INCREMENT BY 1 NOCACHE", "seq_coupons"),

# ── 11. user_coupons ────────────────────────────────────────────────────────
("""CREATE TABLE user_coupons (
    uc_id         NUMBER          NOT NULL,
    user_id       NUMBER          NOT NULL,
    coupon_id     NUMBER          NOT NULL,
    status        VARCHAR2(10)    DEFAULT 'unused',
    get_time      TIMESTAMP       DEFAULT SYSTIMESTAMP,
    use_time      TIMESTAMP,
    order_id      NUMBER,
    CONSTRAINT pk_user_coupons PRIMARY KEY (uc_id),
    CONSTRAINT fk_uc_user   FOREIGN KEY (user_id)   REFERENCES users(user_id),
    CONSTRAINT fk_uc_coupon FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id),
    CONSTRAINT chk_uc_status CHECK (status IN ('unused','used','expired'))
)""", "user_coupons"),
("CREATE SEQUENCE seq_user_coupons START WITH 1 INCREMENT BY 1 NOCACHE", "seq_user_coupons"),

# ── 12. activities ──────────────────────────────────────────────────────────
("""CREATE TABLE activities (
    activity_id   NUMBER          NOT NULL,
    activity_name VARCHAR2(100)   NOT NULL,
    activity_type VARCHAR2(20)    NOT NULL,
    rules         CLOB,
    start_time    TIMESTAMP       NOT NULL,
    end_time      TIMESTAMP       NOT NULL,
    status        VARCHAR2(10)    DEFAULT 'active',
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_activities PRIMARY KEY (activity_id),
    CONSTRAINT chk_act_type   CHECK (activity_type IN ('SECKILL','FULL_REDUCE')),
    CONSTRAINT chk_act_status CHECK (status IN ('active','inactive'))
)""", "activities"),
("CREATE SEQUENCE seq_activities START WITH 1 INCREMENT BY 1 NOCACHE", "seq_activities"),

# ── 13. activity_products ───────────────────────────────────────────────────
("""CREATE TABLE activity_products (
    id             NUMBER          NOT NULL,
    activity_id    NUMBER          NOT NULL,
    product_id     NUMBER          NOT NULL,
    activity_price NUMBER(10,2),
    CONSTRAINT pk_activity_products PRIMARY KEY (id),
    CONSTRAINT fk_ap_activity FOREIGN KEY (activity_id) REFERENCES activities(activity_id),
    CONSTRAINT fk_ap_product  FOREIGN KEY (product_id)  REFERENCES products(product_id),
    CONSTRAINT uq_ap UNIQUE (activity_id, product_id)
)""", "activity_products"),
("CREATE SEQUENCE seq_activity_products START WITH 1 INCREMENT BY 1 NOCACHE", "seq_activity_products"),

# ── 14. banners ─────────────────────────────────────────────────────────────
("""CREATE TABLE banners (
    banner_id     NUMBER          NOT NULL,
    image_url     VARCHAR2(500)   NOT NULL,
    link_type     VARCHAR2(20)    DEFAULT 'NONE',
    link_id       NUMBER,
    sort_order    NUMBER          DEFAULT 0,
    status        VARCHAR2(10)    DEFAULT 'active',
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_banners PRIMARY KEY (banner_id),
    CONSTRAINT chk_banner_link   CHECK (link_type IN ('PRODUCT','CATEGORY','ACTIVITY','NONE')),
    CONSTRAINT chk_banner_status CHECK (status IN ('active','inactive'))
)""", "banners"),
("CREATE SEQUENCE seq_banners START WITH 1 INCREMENT BY 1 NOCACHE", "seq_banners"),

# ── 15. orders ──────────────────────────────────────────────────────────────
("""CREATE TABLE orders (
    order_id            NUMBER          NOT NULL,
    order_no            VARCHAR2(30)    NOT NULL,
    user_id             NUMBER          NOT NULL,
    address_id          NUMBER,
    total_amount        NUMBER(12,2)    NOT NULL,
    discount_amount     NUMBER(12,2)    DEFAULT 0,
    pay_amount          NUMBER(12,2)    NOT NULL,
    pay_method          VARCHAR2(20)    DEFAULT 'MOCK',
    coupon_id           NUMBER,
    points_used         NUMBER          DEFAULT 0,
    status              VARCHAR2(20)    DEFAULT 'PENDING_PAY',
    delivery_person_id  NUMBER,
    remark              VARCHAR2(500),
    pay_time            TIMESTAMP,
    ship_time           TIMESTAMP,
    complete_time       TIMESTAMP,
    cancel_time         TIMESTAMP,
    create_time         TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_orders       PRIMARY KEY (order_id),
    CONSTRAINT uq_order_no     UNIQUE (order_no),
    CONSTRAINT fk_order_user   FOREIGN KEY (user_id)     REFERENCES users(user_id),
    CONSTRAINT fk_order_addr   FOREIGN KEY (address_id)  REFERENCES addresses(address_id),
    CONSTRAINT chk_order_status CHECK (status IN ('PENDING_PAY','PAID','PENDING_SHIP','SHIPPING','COMPLETED','CANCELLED')),
    CONSTRAINT chk_order_pay   CHECK (pay_method IN ('MOCK','CASH','MOCK_CARD'))
)""", "orders"),
("CREATE SEQUENCE seq_orders START WITH 1 INCREMENT BY 1 NOCACHE", "seq_orders"),

# ── 16. order_items ─────────────────────────────────────────────────────────
("""CREATE TABLE order_items (
    item_id       NUMBER          NOT NULL,
    order_id      NUMBER          NOT NULL,
    product_id    NUMBER          NOT NULL,
    product_name  VARCHAR2(100)   NOT NULL,
    product_image VARCHAR2(500),
    unit_price    NUMBER(10,2)    NOT NULL,
    quantity      NUMBER          NOT NULL,
    subtotal      NUMBER(12,2)    NOT NULL,
    CONSTRAINT pk_order_items PRIMARY KEY (item_id),
    CONSTRAINT fk_oi_order   FOREIGN KEY (order_id)   REFERENCES orders(order_id),
    CONSTRAINT fk_oi_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT chk_oi_qty    CHECK (quantity >= 1)
)""", "order_items"),
("CREATE SEQUENCE seq_order_items START WITH 1 INCREMENT BY 1 NOCACHE", "seq_order_items"),

# ── 17. after_sales ─────────────────────────────────────────────────────────
("""CREATE TABLE after_sales (
    as_id         NUMBER          NOT NULL,
    order_id      NUMBER          NOT NULL,
    user_id       NUMBER          NOT NULL,
    as_type       VARCHAR2(20)    DEFAULT 'REFUND',
    reason        VARCHAR2(500)   NOT NULL,
    status        VARCHAR2(20)    DEFAULT 'PENDING',
    refund_amount NUMBER(12,2),
    admin_remark  VARCHAR2(500),
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    handle_time   TIMESTAMP,
    CONSTRAINT pk_after_sales PRIMARY KEY (as_id),
    CONSTRAINT fk_as_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_as_user  FOREIGN KEY (user_id)  REFERENCES users(user_id),
    CONSTRAINT chk_as_type   CHECK (as_type IN ('REFUND','EXCHANGE')),
    CONSTRAINT chk_as_status CHECK (status IN ('PENDING','APPROVED','REJECTED','COMPLETED'))
)""", "after_sales"),
("CREATE SEQUENCE seq_after_sales START WITH 1 INCREMENT BY 1 NOCACHE", "seq_after_sales"),

# ── 18. reviews ─────────────────────────────────────────────────────────────
("""CREATE TABLE reviews (
    review_id     NUMBER          NOT NULL,
    order_id      NUMBER          NOT NULL,
    product_id    NUMBER          NOT NULL,
    user_id       NUMBER          NOT NULL,
    rating        NUMBER(1)       NOT NULL,
    content       VARCHAR2(500),
    images        VARCHAR2(2000),
    is_anonymous  NUMBER(1)       DEFAULT 0,
    is_hidden     NUMBER(1)       DEFAULT 0,
    reply         VARCHAR2(500),
    reply_time    TIMESTAMP,
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_reviews    PRIMARY KEY (review_id),
    CONSTRAINT fk_rev_order   FOREIGN KEY (order_id)   REFERENCES orders(order_id),
    CONSTRAINT fk_rev_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_rev_user    FOREIGN KEY (user_id)    REFERENCES users(user_id),
    CONSTRAINT chk_rev_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT chk_rev_anon   CHECK (is_anonymous IN (0,1)),
    CONSTRAINT chk_rev_hidden CHECK (is_hidden IN (0,1))
)""", "reviews"),
("CREATE SEQUENCE seq_reviews START WITH 1 INCREMENT BY 1 NOCACHE", "seq_reviews"),

# ── 19. favorites ───────────────────────────────────────────────────────────
("""CREATE TABLE favorites (
    fav_id        NUMBER          NOT NULL,
    user_id       NUMBER          NOT NULL,
    product_id    NUMBER          NOT NULL,
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_favorites PRIMARY KEY (fav_id),
    CONSTRAINT fk_fav_user    FOREIGN KEY (user_id)    REFERENCES users(user_id),
    CONSTRAINT fk_fav_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT uq_fav UNIQUE (user_id, product_id)
)""", "favorites"),
("CREATE SEQUENCE seq_favorites START WITH 1 INCREMENT BY 1 NOCACHE", "seq_favorites"),

# ── 20. messages ────────────────────────────────────────────────────────────
("""CREATE TABLE messages (
    message_id    NUMBER          NOT NULL,
    user_id       NUMBER          NOT NULL,
    title         VARCHAR2(100)   NOT NULL,
    content       VARCHAR2(500)   NOT NULL,
    msg_type      VARCHAR2(20)    DEFAULT 'SYSTEM',
    is_read       NUMBER(1)       DEFAULT 0,
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_messages PRIMARY KEY (message_id),
    CONSTRAINT fk_msg_user   FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT chk_msg_type  CHECK (msg_type IN ('SYSTEM','ORDER','COUPON','AFTER_SALES')),
    CONSTRAINT chk_msg_read  CHECK (is_read IN (0,1))
)""", "messages"),
("CREATE SEQUENCE seq_messages START WITH 1 INCREMENT BY 1 NOCACHE", "seq_messages"),

# ── 21. points_logs ─────────────────────────────────────────────────────────
("""CREATE TABLE points_logs (
    log_id        NUMBER          NOT NULL,
    user_id       NUMBER          NOT NULL,
    change_amount NUMBER          NOT NULL,
    balance_after NUMBER          NOT NULL,
    reason        VARCHAR2(100)   NOT NULL,
    ref_id        NUMBER,
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_points_logs PRIMARY KEY (log_id),
    CONSTRAINT fk_pl_user FOREIGN KEY (user_id) REFERENCES users(user_id)
)""", "points_logs"),
("CREATE SEQUENCE seq_points_logs START WITH 1 INCREMENT BY 1 NOCACHE", "seq_points_logs"),

# ── 22. inventory_logs ──────────────────────────────────────────────────────
("""CREATE TABLE inventory_logs (
    log_id        NUMBER          NOT NULL,
    product_id    NUMBER          NOT NULL,
    change_amount NUMBER          NOT NULL,
    balance_after NUMBER          NOT NULL,
    log_type      VARCHAR2(20)    NOT NULL,
    ref_id        NUMBER,
    remark        VARCHAR2(200),
    operator_id   NUMBER,
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_inventory_logs PRIMARY KEY (log_id),
    CONSTRAINT fk_il_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT chk_il_type CHECK (log_type IN ('PURCHASE_IN','ORDER_OUT','DAMAGE','CHECK_ADJUST','MANUAL'))
)""", "inventory_logs"),
("CREATE SEQUENCE seq_inventory_logs START WITH 1 INCREMENT BY 1 NOCACHE", "seq_inventory_logs"),

# ── 23. purchase_orders ─────────────────────────────────────────────────────
("""CREATE TABLE purchase_orders (
    po_id         NUMBER          NOT NULL,
    po_no         VARCHAR2(30)    NOT NULL,
    supplier_id   NUMBER,
    total_amount  NUMBER(12,2)    NOT NULL,
    status        VARCHAR2(20)    DEFAULT 'DRAFT',
    operator_id   NUMBER          NOT NULL,
    remark        VARCHAR2(500),
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    complete_time TIMESTAMP,
    CONSTRAINT pk_purchase_orders PRIMARY KEY (po_id),
    CONSTRAINT uq_po_no      UNIQUE (po_no),
    CONSTRAINT fk_po_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id),
    CONSTRAINT chk_po_status CHECK (status IN ('DRAFT','SUBMITTED','PARTIALLY_ARRIVED','COMPLETED'))
)""", "purchase_orders"),
("CREATE SEQUENCE seq_purchase_orders START WITH 1 INCREMENT BY 1 NOCACHE", "seq_purchase_orders"),

# ── 24. purchase_order_items ────────────────────────────────────────────────
("""CREATE TABLE purchase_order_items (
    item_id          NUMBER          NOT NULL,
    po_id            NUMBER          NOT NULL,
    product_id       NUMBER          NOT NULL,
    order_quantity   NUMBER          NOT NULL,
    arrived_quantity NUMBER          DEFAULT 0,
    unit_price       NUMBER(10,2)    NOT NULL,
    CONSTRAINT pk_po_items   PRIMARY KEY (item_id),
    CONSTRAINT fk_poi_po      FOREIGN KEY (po_id)       REFERENCES purchase_orders(po_id),
    CONSTRAINT fk_poi_product FOREIGN KEY (product_id)  REFERENCES products(product_id),
    CONSTRAINT chk_poi_qty    CHECK (order_quantity >= 1)
)""", "purchase_order_items"),
("CREATE SEQUENCE seq_po_items START WITH 1 INCREMENT BY 1 NOCACHE", "seq_po_items"),

# ── 25. inventory_checks ────────────────────────────────────────────────────
("""CREATE TABLE inventory_checks (
    check_id      NUMBER          NOT NULL,
    check_no      VARCHAR2(30)    NOT NULL,
    status        VARCHAR2(20)    DEFAULT 'IN_PROGRESS',
    operator_id   NUMBER          NOT NULL,
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    complete_time TIMESTAMP,
    CONSTRAINT pk_inventory_checks PRIMARY KEY (check_id),
    CONSTRAINT uq_check_no   UNIQUE (check_no),
    CONSTRAINT chk_ic_status CHECK (status IN ('IN_PROGRESS','PENDING_APPROVE','COMPLETED'))
)""", "inventory_checks"),
("CREATE SEQUENCE seq_inventory_checks START WITH 1 INCREMENT BY 1 NOCACHE", "seq_inventory_checks"),

# ── 26. inventory_check_items ───────────────────────────────────────────────
("""CREATE TABLE inventory_check_items (
    item_id         NUMBER          NOT NULL,
    check_id        NUMBER          NOT NULL,
    product_id      NUMBER          NOT NULL,
    system_quantity NUMBER          NOT NULL,
    actual_quantity NUMBER,
    difference      NUMBER,
    CONSTRAINT pk_check_items PRIMARY KEY (item_id),
    CONSTRAINT fk_ci_check   FOREIGN KEY (check_id)   REFERENCES inventory_checks(check_id),
    CONSTRAINT fk_ci_product FOREIGN KEY (product_id) REFERENCES products(product_id)
)""", "inventory_check_items"),
("CREATE SEQUENCE seq_check_items START WITH 1 INCREMENT BY 1 NOCACHE", "seq_check_items"),

# ── 27. damage_records ──────────────────────────────────────────────────────
("""CREATE TABLE damage_records (
    damage_id     NUMBER          NOT NULL,
    product_id    NUMBER          NOT NULL,
    quantity      NUMBER          NOT NULL,
    reason        VARCHAR2(200)   NOT NULL,
    operator_id   NUMBER          NOT NULL,
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_damage_records PRIMARY KEY (damage_id),
    CONSTRAINT fk_dr_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT chk_dr_qty    CHECK (quantity >= 1)
)""", "damage_records"),
("CREATE SEQUENCE seq_damage_records START WITH 1 INCREMENT BY 1 NOCACHE", "seq_damage_records"),

# ── 28. delivery_persons ────────────────────────────────────────────────────
("""CREATE TABLE delivery_persons (
    courier_id    NUMBER          NOT NULL,
    real_name     VARCHAR2(50)    NOT NULL,
    phone         VARCHAR2(20)    NOT NULL,
    password      VARCHAR2(100)   NOT NULL,
    status        VARCHAR2(10)    DEFAULT 'active',
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_delivery_persons PRIMARY KEY (courier_id),
    CONSTRAINT uq_courier_phone    UNIQUE (phone),
    CONSTRAINT chk_courier_status  CHECK (status IN ('active','inactive'))
)""", "delivery_persons"),
("CREATE SEQUENCE seq_delivery_persons START WITH 1 INCREMENT BY 1 NOCACHE", "seq_delivery_persons"),

# ── 29. delivery_tasks ──────────────────────────────────────────────────────
("""CREATE TABLE delivery_tasks (
    task_id       NUMBER          NOT NULL,
    order_id      NUMBER          NOT NULL,
    courier_id    NUMBER,
    status        VARCHAR2(20)    DEFAULT 'ASSIGNED',
    assign_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    pickup_time   TIMESTAMP,
    deliver_time  TIMESTAMP,
    fail_reason   VARCHAR2(200),
    CONSTRAINT pk_delivery_tasks PRIMARY KEY (task_id),
    CONSTRAINT uq_task_order  UNIQUE (order_id),
    CONSTRAINT fk_dt_order    FOREIGN KEY (order_id)   REFERENCES orders(order_id),
    CONSTRAINT fk_dt_courier  FOREIGN KEY (courier_id) REFERENCES delivery_persons(courier_id),
    CONSTRAINT chk_dt_status  CHECK (status IN ('ASSIGNED','PICKED_UP','DELIVERED','FAILED'))
)""", "delivery_tasks"),
("CREATE SEQUENCE seq_delivery_tasks START WITH 1 INCREMENT BY 1 NOCACHE", "seq_delivery_tasks"),

# ── 30. cashier_shifts ──────────────────────────────────────────────────────
("""CREATE TABLE cashier_shifts (
    shift_id      NUMBER          NOT NULL,
    cashier_id    NUMBER          NOT NULL,
    start_time    TIMESTAMP       NOT NULL,
    end_time      TIMESTAMP,
    start_cash    NUMBER(10,2)    DEFAULT 0,
    end_cash      NUMBER(10,2),
    status        VARCHAR2(10)    DEFAULT 'OPEN',
    CONSTRAINT pk_cashier_shifts PRIMARY KEY (shift_id),
    CONSTRAINT fk_cs_cashier FOREIGN KEY (cashier_id) REFERENCES admin_users(admin_id),
    CONSTRAINT chk_cs_status CHECK (status IN ('OPEN','CLOSED'))
)""", "cashier_shifts"),
("CREATE SEQUENCE seq_cashier_shifts START WITH 1 INCREMENT BY 1 NOCACHE", "seq_cashier_shifts"),

# ── 31. cashier_records ─────────────────────────────────────────────────────
("""CREATE TABLE cashier_records (
    record_id       NUMBER          NOT NULL,
    shift_id        NUMBER          NOT NULL,
    user_id         NUMBER,
    total_amount    NUMBER(12,2)    NOT NULL,
    discount_amount NUMBER(12,2)    DEFAULT 0,
    pay_amount      NUMBER(12,2)    NOT NULL,
    pay_method      VARCHAR2(20)    DEFAULT 'CASH',
    cashier_id      NUMBER          NOT NULL,
    create_time     TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_cashier_records PRIMARY KEY (record_id),
    CONSTRAINT fk_cr_shift   FOREIGN KEY (shift_id)   REFERENCES cashier_shifts(shift_id),
    CONSTRAINT fk_cr_user    FOREIGN KEY (user_id)    REFERENCES users(user_id),
    CONSTRAINT fk_cr_cashier FOREIGN KEY (cashier_id) REFERENCES admin_users(admin_id),
    CONSTRAINT chk_cr_pay    CHECK (pay_method IN ('CASH','MOCK_CARD'))
)""", "cashier_records"),
("CREATE SEQUENCE seq_cashier_records START WITH 1 INCREMENT BY 1 NOCACHE", "seq_cashier_records"),

# ── 32. audit_logs ──────────────────────────────────────────────────────────
("""CREATE TABLE audit_logs (
    log_id        NUMBER          NOT NULL,
    operator_id   NUMBER          NOT NULL,
    operator_name VARCHAR2(50)    NOT NULL,
    module        VARCHAR2(50)    NOT NULL,
    action        VARCHAR2(50)    NOT NULL,
    target_id     NUMBER,
    before_data   CLOB,
    after_data    CLOB,
    ip_address    VARCHAR2(50),
    create_time   TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT pk_audit_logs PRIMARY KEY (log_id)
)""", "audit_logs"),
("CREATE SEQUENCE seq_audit_logs START WITH 1 INCREMENT BY 1 NOCACHE", "seq_audit_logs"),

]  # end ddl_list

for sql, label in ddl_list:
    exec_ddl(sql, label)

# ══════════════════════════════════════════════════════════════════════════════
# STEP 3  索引
# ══════════════════════════════════════════════════════════════════════════════
print("\n" + "=" * 60)
print("STEP 3: 创建索引")
print("=" * 60)

indexes = [
    ("CREATE INDEX idx_prod_category  ON products(category_id)",          "idx_prod_category"),
    ("CREATE INDEX idx_prod_brand     ON products(brand_id)",             "idx_prod_brand"),
    ("CREATE INDEX idx_prod_status    ON products(status)",               "idx_prod_status"),
    ("CREATE INDEX idx_prod_name      ON products(product_name)",         "idx_prod_name"),
    ("CREATE INDEX idx_prod_recommend ON products(is_recommend)",         "idx_prod_recommend"),
    ("CREATE INDEX idx_order_user     ON orders(user_id)",                "idx_order_user"),
    ("CREATE INDEX idx_order_status   ON orders(status)",                 "idx_order_status"),
    ("CREATE INDEX idx_order_time     ON orders(create_time)",            "idx_order_time"),
    ("CREATE INDEX idx_order_no       ON orders(order_no)",               "idx_order_no"),
    ("CREATE INDEX idx_cart_user      ON cart(user_id)",                  "idx_cart_user"),
    ("CREATE INDEX idx_il_product     ON inventory_logs(product_id)",     "idx_il_product"),
    ("CREATE INDEX idx_il_time        ON inventory_logs(create_time)",    "idx_il_time"),
    ("CREATE INDEX idx_msg_user_read  ON messages(user_id, is_read)",     "idx_msg_user_read"),
    ("CREATE INDEX idx_rev_product    ON reviews(product_id)",            "idx_rev_product"),
    ("CREATE INDEX idx_pl_user        ON points_logs(user_id)",           "idx_pl_user"),
    ("CREATE INDEX idx_fav_user       ON favorites(user_id)",             "idx_fav_user"),
]
for sql, label in indexes:
    exec_ddl(sql, label)

# ══════════════════════════════════════════════════════════════════════════════
# STEP 4  初始化数据
# ══════════════════════════════════════════════════════════════════════════════
print("\n" + "=" * 60)
print("STEP 4: 插入初始化数据")
print("=" * 60)

data_sqls = [
    # 管理员
    ("INSERT INTO admin_users(admin_id,username,password,real_name,role,status) VALUES(seq_admin_users.NEXTVAL,'admin','admin123','系统管理员','SUPER_ADMIN','active')", "admin:超级管理员"),
    ("INSERT INTO admin_users(admin_id,username,password,real_name,role,status) VALUES(seq_admin_users.NEXTVAL,'manager','manager123','店长','MANAGER','active')", "admin:店长"),
    ("INSERT INTO admin_users(admin_id,username,password,real_name,role,status) VALUES(seq_admin_users.NEXTVAL,'cashier01','cashier123','收银员小王','CASHIER','active')", "admin:收银员"),
    ("INSERT INTO admin_users(admin_id,username,password,real_name,role,status) VALUES(seq_admin_users.NEXTVAL,'warehouse01','warehouse123','仓管小李','WAREHOUSE','active')", "admin:仓库管理员"),
    # 配送员
    ("INSERT INTO delivery_persons(courier_id,real_name,phone,password,status) VALUES(seq_delivery_persons.NEXTVAL,'张配送','13900000001','courier123','active')", "配送员:张配送"),
    ("INSERT INTO delivery_persons(courier_id,real_name,phone,password,status) VALUES(seq_delivery_persons.NEXTVAL,'李配送','13900000002','courier123','active')", "配送员:李配送"),
    # 顾客
    ("INSERT INTO users(user_id,username,password,nickname,real_name,phone,member_level,points,status) VALUES(seq_users.NEXTVAL,'13800138001','user123','小明','张三','13800138001','SILVER',520,'active')", "用户:小明"),
    ("INSERT INTO users(user_id,username,password,nickname,real_name,phone,member_level,points,status) VALUES(seq_users.NEXTVAL,'13800138002','user123','小红','李四','13800138002','NORMAL',0,'active')", "用户:小红"),
    # 一级分类
    ("INSERT INTO categories(category_id,parent_id,category_name,sort_order,status,description) VALUES(seq_categories.NEXTVAL,0,'食品',1,'active','各类食品零食')", "分类:食品"),
    ("INSERT INTO categories(category_id,parent_id,category_name,sort_order,status,description) VALUES(seq_categories.NEXTVAL,0,'饮料',2,'active','各类饮品')", "分类:饮料"),
    ("INSERT INTO categories(category_id,parent_id,category_name,sort_order,status,description) VALUES(seq_categories.NEXTVAL,0,'日用品',3,'active','生活日用品')", "分类:日用品"),
    ("INSERT INTO categories(category_id,parent_id,category_name,sort_order,status,description) VALUES(seq_categories.NEXTVAL,0,'生鲜',4,'active','新鲜蔬果肉蛋')", "分类:生鲜"),
    ("INSERT INTO categories(category_id,parent_id,category_name,sort_order,status,description) VALUES(seq_categories.NEXTVAL,0,'酒水',5,'active','啤酒白酒红酒')", "分类:酒水"),
    # 二级分类（parent_id 取实际值）
    ("INSERT INTO categories(category_id,parent_id,category_name,sort_order,status) VALUES(seq_categories.NEXTVAL,1,'饼干糕点',1,'active')", "分类:饼干糕点"),
    ("INSERT INTO categories(category_id,parent_id,category_name,sort_order,status) VALUES(seq_categories.NEXTVAL,1,'方便速食',2,'active')", "分类:方便速食"),
    ("INSERT INTO categories(category_id,parent_id,category_name,sort_order,status) VALUES(seq_categories.NEXTVAL,2,'碳酸饮料',1,'active')", "分类:碳酸饮料"),
    ("INSERT INTO categories(category_id,parent_id,category_name,sort_order,status) VALUES(seq_categories.NEXTVAL,2,'矿泉水',2,'active')", "分类:矿泉水"),
    ("INSERT INTO categories(category_id,parent_id,category_name,sort_order,status) VALUES(seq_categories.NEXTVAL,3,'洗护用品',1,'active')", "分类:洗护用品"),
    # 品牌
    ("INSERT INTO brands(brand_id,brand_name,status) VALUES(seq_brands.NEXTVAL,'可口可乐','active')", "品牌:可口可乐"),
    ("INSERT INTO brands(brand_id,brand_name,status) VALUES(seq_brands.NEXTVAL,'农夫山泉','active')", "品牌:农夫山泉"),
    ("INSERT INTO brands(brand_id,brand_name,status) VALUES(seq_brands.NEXTVAL,'康师傅','active')", "品牌:康师傅"),
    ("INSERT INTO brands(brand_id,brand_name,status) VALUES(seq_brands.NEXTVAL,'统一','active')", "品牌:统一"),
    ("INSERT INTO brands(brand_id,brand_name,status) VALUES(seq_brands.NEXTVAL,'海飞丝','active')", "品牌:海飞丝"),
    ("INSERT INTO brands(brand_id,brand_name,status) VALUES(seq_brands.NEXTVAL,'高露洁','active')", "品牌:高露洁"),
    # 供应商
    ("INSERT INTO suppliers(supplier_id,supplier_name,contact_name,contact_phone,status) VALUES(seq_suppliers.NEXTVAL,'可口可乐公司','王经理','020-88888881','active')", "供应商:可口可乐"),
    ("INSERT INTO suppliers(supplier_id,supplier_name,contact_name,contact_phone,status) VALUES(seq_suppliers.NEXTVAL,'农夫山泉股份','李经理','0571-88888882','active')", "供应商:农夫山泉"),
    ("INSERT INTO suppliers(supplier_id,supplier_name,contact_name,contact_phone,status) VALUES(seq_suppliers.NEXTVAL,'顶益食品有限公司','张经理','022-88888883','active')", "供应商:顶益食品"),
    # 商品（category_id 直接用数字，Oracle Sequence从1开始）
    ("INSERT INTO products(product_id,product_name,category_id,brand_id,supplier_id,original_price,price,stock,stock_warning,unit,sales_count,avg_rating,is_recommend,status) VALUES(seq_products.NEXTVAL,'可口可乐 330ml',8,1,1,4.00,3.50,500,50,'罐',9999,4.8,1,'active')", "商品:可口可乐330ml"),
    ("INSERT INTO products(product_id,product_name,category_id,brand_id,supplier_id,original_price,price,stock,stock_warning,unit,sales_count,avg_rating,is_recommend,status) VALUES(seq_products.NEXTVAL,'农夫山泉 550ml',9,2,2,2.50,2.00,1000,100,'瓶',8888,4.9,1,'active')", "商品:农夫山泉550ml"),
    ("INSERT INTO products(product_id,product_name,category_id,brand_id,supplier_id,original_price,price,stock,stock_warning,unit,sales_count,avg_rating,is_recommend,status) VALUES(seq_products.NEXTVAL,'康师傅方便面 红烧牛肉',7,3,3,5.50,4.50,300,30,'包',5000,4.7,1,'active')", "商品:康师傅方便面"),
    ("INSERT INTO products(product_id,product_name,category_id,brand_id,supplier_id,original_price,price,stock,stock_warning,unit,sales_count,avg_rating,is_recommend,status) VALUES(seq_products.NEXTVAL,'统一冰红茶 500ml',8,4,2,3.50,3.00,400,40,'瓶',3000,4.6,0,'active')", "商品:统一冰红茶"),
    ("INSERT INTO products(product_id,product_name,category_id,brand_id,supplier_id,original_price,price,stock,stock_warning,unit,sales_count,avg_rating,is_recommend,status) VALUES(seq_products.NEXTVAL,'海飞丝洗发水 400ml',10,5,1,35.00,29.90,150,20,'瓶',1200,4.5,0,'active')", "商品:海飞丝洗发水"),
    ("INSERT INTO products(product_id,product_name,category_id,brand_id,supplier_id,original_price,price,stock,stock_warning,unit,sales_count,avg_rating,is_recommend,status) VALUES(seq_products.NEXTVAL,'高露洁牙膏 120g',10,6,1,15.00,12.00,200,20,'支',2000,4.7,0,'active')", "商品:高露洁牙膏"),
    ("INSERT INTO products(product_id,product_name,category_id,brand_id,supplier_id,original_price,price,stock,stock_warning,unit,sales_count,avg_rating,is_recommend,status) VALUES(seq_products.NEXTVAL,'奥利奥饼干 388g',6,1,1,20.00,16.90,250,25,'盒',4500,4.8,1,'active')", "商品:奥利奥饼干"),
    ("INSERT INTO products(product_id,product_name,category_id,brand_id,supplier_id,original_price,price,stock,stock_warning,unit,sales_count,avg_rating,is_recommend,status) VALUES(seq_products.NEXTVAL,'乐事薯片 104g',6,1,1,8.00,6.50,300,30,'包',6000,4.6,0,'active')", "商品:乐事薯片"),
    # 轮播图
    ("INSERT INTO banners(banner_id,image_url,link_type,sort_order,status) VALUES(seq_banners.NEXTVAL,'/uploads/banner/banner1.jpg','NONE',1,'active')", "轮播图1"),
    ("INSERT INTO banners(banner_id,image_url,link_type,sort_order,status) VALUES(seq_banners.NEXTVAL,'/uploads/banner/banner2.jpg','NONE',2,'active')", "轮播图2"),
    # 优惠券
    ("INSERT INTO coupons(coupon_id,coupon_name,coupon_type,face_value,min_amount,total_count,start_time,end_time,status) VALUES(seq_coupons.NEXTVAL,'新人满50减10','FULL_REDUCE',10.00,50.00,1000,SYSTIMESTAMP,SYSTIMESTAMP+INTERVAL '30' DAY,'active')", "优惠券:新人券"),
    ("INSERT INTO coupons(coupon_id,coupon_name,coupon_type,face_value,min_amount,total_count,start_time,end_time,status) VALUES(seq_coupons.NEXTVAL,'全场九折券','DISCOUNT',0.9,0.00,-1,SYSTIMESTAMP,SYSTIMESTAMP+INTERVAL '7' DAY,'active')", "优惠券:九折券"),
]

for sql, label in data_sqls:
    exec_ddl(sql, label)

con.commit()
print("\n  ✓  COMMIT 成功")

# ══════════════════════════════════════════════════════════════════════════════
# STEP 5  验证
# ══════════════════════════════════════════════════════════════════════════════
print("\n" + "=" * 60)
print("STEP 5: 验证结果")
print("=" * 60)

check_tables = [
    "users","admin_users","categories","brands","suppliers",
    "products","product_images","addresses","cart",
    "coupons","user_coupons","activities","activity_products","banners",
    "orders","order_items","after_sales","reviews","favorites",
    "messages","points_logs","inventory_logs",
    "purchase_orders","purchase_order_items",
    "inventory_checks","inventory_check_items","damage_records",
    "delivery_persons","delivery_tasks",
    "cashier_shifts","cashier_records","audit_logs",
]

ok = 0
fail = 0
print(f"\n  {'表名':<28} {'行数':>6}")
print("  " + "-" * 36)
for t in check_tables:
    try:
        cur.execute(f"SELECT COUNT(*) FROM {t}")
        cnt = cur.fetchone()[0]
        print(f"  ✓  {t:<28} {cnt:>6}")
        ok += 1
    except Exception as e:
        print(f"  ✗  {t:<28}  缺失! {e}")
        fail += 1

print("\n" + "=" * 60)
print(f"  共 {ok+fail} 张表  ✓ 成功 {ok}  ✗ 失败 {fail}")
print("=" * 60)

cur.close()
con.close()
