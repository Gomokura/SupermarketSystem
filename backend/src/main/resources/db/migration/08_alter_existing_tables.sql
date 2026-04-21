-- ============================================================
-- 超市管理系统 · 修复现有表字段缺失
-- 文件：08_alter_existing_tables.sql
-- 说明：在执行前请确保已执行 01~07 建表脚本
-- ============================================================

-- ============================================================
-- 1. USERS 表补充字段
-- ============================================================
ALTER TABLE USERS ADD nickname VARCHAR2(50);
ALTER TABLE USERS ADD avatar VARCHAR2(255);
ALTER TABLE USERS ADD gender VARCHAR2(4);         -- '男' / '女' / '保密'
ALTER TABLE USERS ADD birthday DATE;
ALTER TABLE USERS ADD email VARCHAR2(100);
ALTER TABLE USERS ADD member_level VARCHAR2(20) DEFAULT 'normal'; -- normal/silver/gold/diamond
ALTER TABLE USERS ADD points NUMBER DEFAULT 0;
ALTER TABLE USERS ADD ban_reason VARCHAR2(200);
-- role 字段扩展说明：普通用户=user，后台角色统一使用ADMINS表管理

COMMENT ON COLUMN USERS.nickname IS '昵称';
COMMENT ON COLUMN USERS.avatar IS '头像路径';
COMMENT ON COLUMN USERS.gender IS '性别';
COMMENT ON COLUMN USERS.birthday IS '生日';
COMMENT ON COLUMN USERS.email IS '邮箱';
COMMENT ON COLUMN USERS.member_level IS '会员等级 normal/silver/gold/diamond';
COMMENT ON COLUMN USERS.points IS '当前积分余额';
COMMENT ON COLUMN USERS.ban_reason IS '封禁原因';

-- ============================================================
-- 2. CATEGORIES 表补充字段（支持二级分类）
-- ============================================================
ALTER TABLE CATEGORIES ADD parent_id NUMBER DEFAULT 0;  -- 0表示一级分类
ALTER TABLE CATEGORIES ADD sort_order NUMBER DEFAULT 0;
ALTER TABLE CATEGORIES ADD icon VARCHAR2(255);

COMMENT ON COLUMN CATEGORIES.parent_id IS '父分类ID，0表示顶级分类';
COMMENT ON COLUMN CATEGORIES.sort_order IS '排序序号，越小越靠前';
COMMENT ON COLUMN CATEGORIES.icon IS '分类图标路径';

-- ============================================================
-- 3. PRODUCTS 表补充字段
-- ============================================================
ALTER TABLE PRODUCTS ADD barcode VARCHAR2(50);
ALTER TABLE PRODUCTS ADD image_url VARCHAR2(255);
ALTER TABLE PRODUCTS ADD brand_id NUMBER;
ALTER TABLE PRODUCTS ADD supplier_id NUMBER;       -- 外键指向SUPPLIERS表
ALTER TABLE PRODUCTS ADD original_price NUMBER(10,2);
ALTER TABLE PRODUCTS ADD cost_price NUMBER(10,2);
ALTER TABLE PRODUCTS ADD stock_warning NUMBER DEFAULT 10;
ALTER TABLE PRODUCTS ADD is_recommend NUMBER(1) DEFAULT 0; -- 1=首页推荐
ALTER TABLE PRODUCTS ADD is_deleted NUMBER(1) DEFAULT 0;   -- 1=已逻辑删除
ALTER TABLE PRODUCTS ADD sales_count NUMBER DEFAULT 0;     -- 累计销量

COMMENT ON COLUMN PRODUCTS.barcode IS '商品条码';
COMMENT ON COLUMN PRODUCTS.image_url IS '主图路径';
COMMENT ON COLUMN PRODUCTS.brand_id IS '品牌ID';
COMMENT ON COLUMN PRODUCTS.supplier_id IS '供应商ID（外键SUPPLIERS）';
COMMENT ON COLUMN PRODUCTS.original_price IS '原价（划线价）';
COMMENT ON COLUMN PRODUCTS.cost_price IS '成本价';
COMMENT ON COLUMN PRODUCTS.stock_warning IS '库存预警阈值';
COMMENT ON COLUMN PRODUCTS.is_recommend IS '是否首页推荐 0/1';
COMMENT ON COLUMN PRODUCTS.is_deleted IS '逻辑删除 0正常/1删除';
COMMENT ON COLUMN PRODUCTS.sales_count IS '累计销量';

-- ============================================================
-- 4. ORDERS 表补充字段
-- ============================================================
ALTER TABLE ORDERS ADD order_no VARCHAR2(32);          -- 业务唯一订单号
ALTER TABLE ORDERS ADD address_id NUMBER;              -- 收货地址ID快照
ALTER TABLE ORDERS ADD address_snapshot VARCHAR2(500); -- 地址文字快照
ALTER TABLE ORDERS ADD freight NUMBER(10,2) DEFAULT 0; -- 运费
ALTER TABLE ORDERS ADD discount_amount NUMBER(10,2) DEFAULT 0; -- 优惠金额
ALTER TABLE ORDERS ADD actual_amount NUMBER(10,2);     -- 实付金额
ALTER TABLE ORDERS ADD points_used NUMBER DEFAULT 0;   -- 使用积分数
ALTER TABLE ORDERS ADD coupon_id NUMBER;               -- 使用的优惠券ID
ALTER TABLE ORDERS ADD remark VARCHAR2(300);           -- 订单备注
ALTER TABLE ORDERS ADD source VARCHAR2(20) DEFAULT 'online'; -- online=线上/cashier=收银台
ALTER TABLE ORDERS ADD delivery_time VARCHAR2(50);     -- 期望配送时间段
ALTER TABLE ORDERS ADD pay_time DATE;                  -- 支付时间

-- 创建唯一索引
CREATE UNIQUE INDEX idx_orders_order_no ON ORDERS(order_no);

COMMENT ON COLUMN ORDERS.order_no IS '业务订单号，格式：年月日+随机8位';
COMMENT ON COLUMN ORDERS.address_snapshot IS '收货地址文字快照（防地址修改影响历史订单）';
COMMENT ON COLUMN ORDERS.freight IS '运费';
COMMENT ON COLUMN ORDERS.discount_amount IS '优惠券/活动减免金额';
COMMENT ON COLUMN ORDERS.actual_amount IS '实付金额=总价-优惠+运费';
COMMENT ON COLUMN ORDERS.points_used IS '积分抵扣数量';
COMMENT ON COLUMN ORDERS.source IS '订单来源 online/cashier';

-- ============================================================
-- 5. ORDER_ITEMS 表补充字段
-- ============================================================
ALTER TABLE ORDER_ITEMS ADD sku_id NUMBER;
ALTER TABLE ORDER_ITEMS ADD spec_name VARCHAR2(100); -- 规格名称快照，如"红色 500ml"

COMMENT ON COLUMN ORDER_ITEMS.sku_id IS 'SKU ID快照';
COMMENT ON COLUMN ORDER_ITEMS.spec_name IS '规格描述快照';

-- ============================================================
-- 6. CART 表补充字段
-- ============================================================
ALTER TABLE CART ADD sku_id NUMBER;
ALTER TABLE CART ADD is_checked NUMBER(1) DEFAULT 1; -- 是否勾选结算 1=是
ALTER TABLE CART ADD add_time DATE DEFAULT SYSDATE;

COMMENT ON COLUMN CART.sku_id IS '选择的SKU ID';
COMMENT ON COLUMN CART.is_checked IS '是否勾选参与结算 1/0';

-- ============================================================
-- 7. DELIVERIES 表补充字段
-- ============================================================
ALTER TABLE DELIVERIES ADD pickup_time DATE;         -- 取件时间
ALTER TABLE DELIVERIES ADD fail_reason VARCHAR2(200); -- 配送失败原因

COMMENT ON COLUMN DELIVERIES.pickup_time IS '配送员取件时间（标记已取件时记录）';
COMMENT ON COLUMN DELIVERIES.fail_reason IS '配送失败原因';

-- ============================================================
-- 8. INVENTORY_LOGS 表补充字段
-- ============================================================
ALTER TABLE INVENTORY_LOGS ADD before_stock NUMBER; -- 变动前库存
ALTER TABLE INVENTORY_LOGS ADD after_stock NUMBER;  -- 变动后库存
ALTER TABLE INVENTORY_LOGS ADD ref_no VARCHAR2(50); -- 关联单号（订单号/采购单号等）

COMMENT ON COLUMN INVENTORY_LOGS.before_stock IS '变动前库存数量';
COMMENT ON COLUMN INVENTORY_LOGS.after_stock IS '变动后库存数量';
COMMENT ON COLUMN INVENTORY_LOGS.ref_no IS '关联单号（订单号/采购单号/盘点单号等）';

-- ============================================================
-- 9. SUPPLIERS 表补充字段
-- ============================================================
ALTER TABLE SUPPLIERS ADD email VARCHAR2(100);
ALTER TABLE SUPPLIERS ADD address VARCHAR2(300);
ALTER TABLE SUPPLIERS ADD payment_period NUMBER DEFAULT 30; -- 结算账期（天）

COMMENT ON COLUMN SUPPLIERS.email IS '供应商邮箱';
COMMENT ON COLUMN SUPPLIERS.address IS '供应商地址';
COMMENT ON COLUMN SUPPLIERS.payment_period IS '结算账期（天）';

-- ============================================================
-- 10. ADDRESS 表补充字段（三级地区）
-- ============================================================
ALTER TABLE ADDRESS ADD province VARCHAR2(50);
ALTER TABLE ADDRESS ADD city VARCHAR2(50);
ALTER TABLE ADDRESS ADD district VARCHAR2(50);

COMMENT ON COLUMN ADDRESS.province IS '省份';
COMMENT ON COLUMN ADDRESS.city IS '城市';
COMMENT ON COLUMN ADDRESS.district IS '区/县';

-- ============================================================
-- 11. PURCHASE_ORDERS 表补充字段
-- ============================================================
ALTER TABLE PURCHASE_ORDERS ADD expected_date DATE;    -- 预计到货日期
ALTER TABLE PURCHASE_ORDERS ADD approver_id NUMBER;    -- 审批人ID
ALTER TABLE PURCHASE_ORDERS ADD approve_time DATE;     -- 审批时间
ALTER TABLE PURCHASE_ORDERS ADD arrive_time DATE;      -- 实际到货时间

COMMENT ON COLUMN PURCHASE_ORDERS.expected_date IS '预计到货日期';
COMMENT ON COLUMN PURCHASE_ORDERS.approver_id IS '审批人管理员ID';

-- ============================================================
-- 12. AUDIT_LOGS 表补充字段
-- ============================================================
ALTER TABLE AUDIT_LOGS ADD before_data CLOB;  -- 操作前数据快照(JSON)
ALTER TABLE AUDIT_LOGS ADD after_data CLOB;   -- 操作后数据快照(JSON)
ALTER TABLE AUDIT_LOGS ADD target_id NUMBER;  -- 操作的记录ID

COMMENT ON COLUMN AUDIT_LOGS.before_data IS '操作前数据快照（JSON格式）';
COMMENT ON COLUMN AUDIT_LOGS.after_data IS '操作后数据快照（JSON格式）';

COMMIT;
