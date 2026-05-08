# 更新日志 — 2026年5月8日

## 数据库密码修复

- **admin_users 表** — 将占位符 `$2a$10$PLACEHOLDER...` 更新为真实密码的 MD5 哈希
  - `admin` / `manager`：密码 `admin123`（MD5: `0192023a7bbd73250516f069df18b500`）
  - `cashier01` / `warehouse01` / `product01` / `service01`：密码 `123456`（MD5: `e10adc3949ba59abbe56e057f20f883e`）

---

## 后端 API 修复

### 库存日志主键缺失（7处）

- **`PurchaseService.java`** — `receive()` 方法插入 `inventory_log` 漏设 `logId` → 补充 `logId = inventoryLogMapper.getNextId()`
- **`AdminService.java`** — 3处：`warehousing()` / `outbound()` / `receivePurchaseOrder()` 补全 `logId`
- **`AdminService.java`** — `assignCourier()` 插入订单状态日志漏设 `logId` → 补充
- **`ProductService.java`** — `updateStock()` / `updateSkuStock()` 各补1处 `logId`

### 状态值大小写不一致（7处）

- **`AfterSaleService.java`**
  - `applyAfterSale()`：`"pending"` → `"PENDING"`；`"after_sale"` → `"AFTER_SALE"`
  - `applyAfterSale()`：`"rejected"` → 数据库约束正确值 `"REJECTED"`
  - `handleAfterSale()`：`"pending"` → `"PENDING"`；`"approve"` → `"APPROVE"`；`"reject"` → `"REJECT"`
  - `handleAfterSale()`：`"approved"` → `"APPROVED"`；`"rejected"` → `"REJECTED"`
  - `completeRefund()`：`"approved"` → `"APPROVED"`；`"completed"` → `"COMPLETED"`；`"refunded"` → `"REFUNDED"`
- **`StocktakeService.java`**
  - `create()`：`"pending"` → `"PENDING"`（任务立即进入待盘点）；`"counting"` → `"IN_PROGRESS"`
  - `inputActual()`：`"counting"` → `"IN_PROGRESS"`（校验状态）
  - `submit()`：`"counting"` → `"IN_PROGRESS"`（校验）；`"done"` → `"COMPLETED"`
- **`WarehouseService.java`** — `reportDamage()`：`logType = "damage"` → 符合数据库 CHECK 约束的 `"DAMAGE"`
- **`AdminService.outbound()`** — `logType = "damage"` → 语义正确的 `"MANUAL"`

### Oracle 主键自增配置修复（4处实体 + 4处调用）

- **`PointsLog.java`** — `IdType.AUTO` → `IdType.INPUT`（Oracle 无 AUTO 机制）
- **`AuditLog.java`** — `IdType.AUTO` → `IdType.INPUT`
- **`AuditLogMapper.java`** — 新增 `getNextId()` 方法（`SELECT SEQ_AUDIT_LOGS.NEXTVAL FROM DUAL`）
- **`PointsService.adminAdjust()`** — 插入 `PointsLog` 漏设 `logId` → 补充
- **`AuditAspect.java`** — 审计日志插入前补充 `logId = auditLogMapper.getNextId()`
- **`ProductSku.java`** — `IdType.AUTO` → `IdType.INPUT`
- **`SeckillService.adminUpsertSeckillProducts()`** — 新增秒杀商品时漏设 `id` → 加 `seckillProductMapper.nextProductRowId()`

### 实体字段映射错误修复（6处）

- **`PurchaseOrder.java`** — `expectedDate` 缺少 `@TableField("EXPECTED_ARRIVE_TIME")` 注解，MyBatis 插入时使用错误列名
- **`Category.java`** — 缺少 `status` / `createTime` 数据库字段；`icon` 使用了错误的 `@TableField("icon_url")`（Oracle 区分大小写）
- **`Brand.java`** — `sortOrder` 被误标为 `@TableField(exist=false)`，导致插入品牌时排序字段被忽略；补充缺失的 `createTime` 字段
- **`Supplier.java`** — `contact`/`phone`/`paymentPeriod` 与 Lombok 生成的 getter 名不匹配，统一改为 `contactName`/`contactPhone`/`paymentDays`
- **`PointsLog.java`** — 实体缺少 `remark` 字段（数据库有 `remark` 列但实体没有），补充
- **`AdminService.java`** — `getSupplierList()` 中 `Supplier::getContact` → `Supplier::getContactName`（配合 Supplier 字段重命名）

### 业务逻辑错误修复（3处）

- **`AuthService.grantNewUserCoupons()`** — 原代码查 `coupon_type = 'new_user'`，但 demo 数据中无此类型，导致新人注册从未成功发放优惠券。改为按名称关键字（`新人`/`新用户`/`新会员`）过滤，对应 demo 中的"新人满50减10"
- **`AuthService.createCourier()`** — 配送员密码未加密直接存入数据库，改为 `passwordEncoder.encode()`
- **`PointsService.adminAdjust()`** — 原逻辑 `if (remark != null) log.setRefId(null)` 在备注非空时反而清空了关联ID（逻辑写反），已修正
- **`AdminService.createPromotion()`** — 创建活动时未设置主键 `activityId`，Oracle 会报错，补上了 `promotionMapper.getNextId()`

---

## 修改文件清单

| 文件路径 | 修改类型 |
|---------|---------|
| `backend/src/main/java/.../service/AfterSaleService.java` | 状态值大小写修正 |
| `backend/src/main/java/.../service/StocktakeService.java` | 状态值大小写修正 |
| `backend/src/main/java/.../service/WarehouseService.java` | logType 大小写 + 补 balanceAfter |
| `backend/src/main/java/.../service/AdminService.java` | logId 补全、字段映射修正、密码加密、活动主键 |
| `backend/src/main/java/.../service/ProductService.java` | logId 补全 |
| `backend/src/main/java/.../service/PointsService.java` | logId 补全、remark 逻辑修正 |
| `backend/src/main/java/.../service/AuthService.java` | 新人券查询逻辑修正、配送员密码加密 |
| `backend/src/main/java/.../service/SeckillService.java` | 秒杀商品主键补全 |
| `backend/src/main/java/.../service/PurchaseService.java` | logId 补全 |
| `backend/src/main/java/.../entity/PurchaseOrder.java` | expectedDate 字段映射 |
| `backend/src/main/java/.../entity/Category.java` | status/createTime/icon 映射修复 |
| `backend/src/main/java/.../entity/Brand.java` | sortOrder/createTime 修复 |
| `backend/src/main/java/.../entity/Supplier.java` | 字段名统一修正 |
| `backend/src/main/java/.../entity/PointsLog.java` | IdType + remark 补全 |
| `backend/src/main/java/.../entity/AuditLog.java` | IdType 修正 |
| `backend/src/main/java/.../entity/ProductSku.java` | IdType 修正 |
| `backend/src/main/java/.../mapper/AuditLogMapper.java` | getNextId 方法补全 |
| `backend/src/main/java/.../aop/AuditAspect.java` | 审计日志主键补全 |
