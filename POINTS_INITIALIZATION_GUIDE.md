# 用户积分初始化说明

## 问题修复清单

### ✅ 1. 收藏功能报错修复
**问题**：点击"我的收藏"报错"No static resource favorites"

**原因**：Favorites.vue导入的API名称与API文件导出的名称不匹配
- Vue文件导入：`favoritesAPI`
- API文件导出：`favoriteAPI`

**修复**：在 `frontend/src/api/index.js` 中添加别名支持
```javascript
export const favoritesAPI = {
  getList: () => request.get('/favorites/my'),
  add: (productId) => request.post(`/favorites/${productId}`),
  remove: (productId) => request.delete(`/favorites/${productId}`)
}
```

**测试方法**：
1. 进入用户个人中心
2. 点击"我的收藏"菜单
3. 应显示收藏的商品列表（如果没有收藏则显示空状态）

---

## 🎁 用户积分初始化

### 业务规则
- **积分来源**：用户的已支付订单
- **计算方式**：消费金额 ¥1 = 积分 1
- **取整规则**：按支付金额的整数部分计算
- **示例**：
  - 订单支付 ¥100.50 → 用户获得 100 积分
  - 订单支付 ¥35.99 → 用户获得 35 积分

### 执行步骤

#### 方法1：使用Oracle SQL Developer（推荐）
1. 打开 Oracle SQL Developer
2. 连接到数据库：`system/Oracle123@localhost:1521/XE`
3. 打开文件：`database/initialize_user_points.sql`
4. 点击"执行脚本"或按 Ctrl+Shift+Enter
5. 查看执行结果

#### 方法2：使用SQL*Plus命令行
```bash
cd d:\桌面\SupermarketSystem\database

# Windows PowerShell
sqlplus system/Oracle123@xe < initialize_user_points.sql

# 或直接执行
sqlplus system/Oracle123@xe
# 在sqlplus提示符下输入：
# @initialize_user_points.sql
```

#### 方法3：手动执行SQL语句
逐条复制执行 `initialize_user_points.sql` 中的SQL语句：

```sql
-- 步骤1：创建临时表
CREATE GLOBAL TEMPORARY TABLE temp_user_points AS
SELECT 
  user_id,
  FLOOR(SUM(pay_amount)) as total_points
FROM orders
WHERE status IN ('PAID', 'PENDING_SHIP', 'SHIPPED', 'COMPLETED', 'CLOSED')
GROUP BY user_id;

-- 步骤2：更新用户积分
UPDATE users u
SET u.points = (
  SELECT COALESCE(tmp.total_points, 0)
  FROM temp_user_points tmp
  WHERE tmp.user_id = u.user_id
)
WHERE u.user_id IN (SELECT user_id FROM temp_user_points);

COMMIT;

-- 步骤3：验证（查看结果）
SELECT 
  u.user_id,
  u.username as 账户,
  u.points as 当前积分,
  COUNT(DISTINCT o.order_id) as 订单数,
  SUM(o.pay_amount) as 消费总额
FROM users u
LEFT JOIN orders o ON u.user_id = o.user_id 
  AND o.status IN ('PAID', 'PENDING_SHIP', 'SHIPPED', 'COMPLETED', 'CLOSED')
WHERE u.user_id < 2000
GROUP BY u.user_id, u.username, u.points
ORDER BY u.user_id;

-- 步骤4：清理临时表
DROP TABLE temp_user_points;
```

### 预期结果

执行完成后，应看到类似输出：
```
USER_ID  账户          当前积分  订单数  消费总额
1000     13800138001   345      5      345.50
1001     13800138002   256      3      256.75
1002     13800138003   512      8      512.30
```

---

## 验证步骤

### 1. 前端验证
1. 登录用户账号
2. 进入"我的积分"页面
3. 应显示初始化后的积分值
4. 查看"积分记录"应为空（首次初始化）

### 2. 后端验证
```bash
# 查询用户积分
SELECT user_id, points FROM users WHERE user_id < 2000 ORDER BY user_id;

# 查询积分日志（此时应为空，因为这是初始化）
SELECT * FROM points_logs ORDER BY log_id DESC;
```

### 3. 后续支付验证
1. 用户购买商品、支付订单
2. 订单支付成功后自动调用积分累计逻辑
3. 积分会自动增加，同时在 POINTS_LOGS 表中创建记录
4. 用户可在"我的积分"→"积分记录"查看所有变动

---

## 常见问题

### Q1: 积分初始化后，后续支付是否仍会累计积分？
**A**：是的。初始化只是根据历史订单设置初始值，之后用户支付新订单时，系统会自动累计新的积分。

### Q2: 如何取消已初始化的积分？
**A**：可以执行：
```sql
UPDATE users SET points = 0 WHERE user_id < 2000;
COMMIT;
```

### Q3: 积分可以为负数吗？
**A**：不可以。系统设计中，积分不能低于0。用户使用积分抵扣时，系统会校验积分余额。

### Q4: 未来是否会支持积分有效期？
**A**：目前不支持。可在后续版本中添加 `expire_time` 字段到 POINTS_LOGS 表。

---

## 相关文件

| 文件 | 说明 |
|------|------|
| `database/initialize_user_points.sql` | 积分初始化SQL脚本 |
| `frontend/src/views/user/PointsLog.vue` | 用户积分记录页面 |
| `frontend/src/views/user/Profile.vue` | 用户个人中心（显示当前积分） |
| `backend/src/main/java/com/supermarket/service/PointsService.java` | 积分业务逻辑服务 |

---

## 后续优化建议

1. **评价奖励**：用户提交评价后奖励 +5 积分
2. **首次购买奖励**：新注册用户首次购买奖励 +20 积分
3. **积分到期规则**：积分有效期 12 个月，过期自动清除
4. **积分兑换功能**：允许用户用积分兑换优惠券或商品
5. **会员等级关联**：根据积分自动升级会员等级（BRONZE → SILVER → GOLD → PLATINUM）
