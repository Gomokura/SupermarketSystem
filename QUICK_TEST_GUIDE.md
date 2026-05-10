# 快速测试指南 - 系统修复验证

## 前置条件
✅ 后端已启动在 http://localhost:8080  
✅ 前端已启动在 http://localhost:5173  
✅ Oracle数据库已连接

---

## 测试 1：收藏功能修复 ⭐
**问题原文**：点击"我的收藏"报错"系统内部错误：No static resource favorites"

### 测试步骤
1. 打开前端应用：http://localhost:5173
2. 登录账号（用户账号）
3. 进入个人中心 → 我的收藏
4. **预期结果**：页面正常显示，不报错

### 验证成功标志
- ✅ 页面加载成功
- ✅ 显示已收藏商品列表或"暂无收藏"提示
- ✅ 浏览器控制台无 404 错误

---

## 测试 2：评价提交修复 ⭐
**问题原文**：提交评价报错"ORA-01400: 无法将 NULL 插入 ORDER_ITEM_ID"

### 测试步骤
1. 登录用户（已有已完成订单的账号，如 13800138001）
2. 进入"我的订单" → "已完成"
3. 选择一个订单，点击"评价"按钮
4. 填写评价内容：
   - 选择星级（如 5 星）
   - 输入评价文字
   - 选择是否匿名（如否）
5. 点击"提交评价"
6. **预期结果**：评价提交成功，显示"评价成功"提示

### 验证成功标志
- ✅ 无 ORA-01400 错误提示
- ✅ 系统显示"评价成功"消息
- ✅ 订单评价按钮变灰或消失

### 数据库验证
```sql
-- 在 SQL Developer 执行
SELECT * FROM REVIEWS 
WHERE ORDER_ID = [刚刚评价的订单号]
ORDER BY CREATE_TIME DESC;

-- 检查 ORDER_ITEM_ID 是否有值（不为NULL）
```

---

## 测试 3：积分自动累计 ⭐⭐
**问题原文**：用户支付订单后，积分不会自动累计

### 准备工作
```sql
-- 检查测试用户当前积分
SELECT USER_ID, USERNAME, POINTS FROM USERS 
WHERE USER_ID = 13800138001;

-- 记录初始积分值
```

### 测试步骤
1. 登录用户（如 13800138001）
2. 进入商城，选购商品，创建订单（例如总金额 ¥100.50）
3. 进入"我的订单" → "待支付"
4. 点击"支付"
5. 完成支付流程
6. **预期结果**：积分自动增加 100 点（¥1 = 1积分）

### 验证成功标志
- ✅ 订单支付成功
- ✅ 进入"我的积分"页面，看到新的积分增加记录
- ✅ 积分流水显示"订单支付"来源

### 数据库验证
```sql
-- 支付订单后检查用户积分
SELECT USER_ID, POINTS FROM USERS WHERE USER_ID = 13800138001;

-- 查看积分流水记录
SELECT * FROM POINTS_LOGS 
WHERE USER_ID = 13800138001 
  AND REASON = 'ORDER_PAY'
ORDER BY CREATE_TIME DESC
LIMIT 1;

-- 检查 CHANGE_AMOUNT 应该等于订单支付金额的整数部分
```

---

## 测试 4：用户积分初始化 ⭐⭐
**需求**：把每个顾客的积分都按照订单消费历史调整

### 方法 A：通过 API 初始化（推荐）
```bash
# 使用 curl 或 Postman
POST http://localhost:8080/points/admin/initialize
Authorization: Bearer [admin_token]
X-User-Id: [admin_id]

# 预期响应
{
  "code": 200,
  "data": {
    "totalUsers": 24,        # 有订单的用户数
    "updatedCount": 24       # 成功更新的用户数
  },
  "message": "success"
}
```

### 方法 B：通过 SQL 脚本初始化
```bash
# 1. 在 SQL Developer 中打开脚本
# 文件：d:\桌面\SupermarketSystem\database\initialize_user_points.sql

# 2. 执行脚本（复制粘贴各部分分别执行）

# 3. 验证结果
SELECT USER_ID, USERNAME, POINTS, 
       (SELECT COUNT(*) FROM ORDERS WHERE ORDERS.USER_ID = USERS.USER_ID) as order_count
FROM USERS 
WHERE POINTS IS NOT NULL AND POINTS > 0
ORDER BY POINTS DESC
LIMIT 20;
```

### 验证成功标志
- ✅ 已支付订单的用户都被初始化积分
- ✅ 积分 = 该用户所有已支付订单金额之和（按整数计算）
- ✅ 使用 "我的积分" 菜单查看，显示正确的积分值

### 示例验证
```
用户 A：有 3 个已支付订单
  - 订单1：¥100.50
  - 订单2：¥200.75
  - 订单3：¥50.00
  预期积分 = FLOOR(100.50 + 200.75 + 50.00) = 351

初始化后：SELECT POINTS FROM USERS WHERE USER_ID = A;
结果应为：351
```

---

## 快速检查清单

### 🔍 前端检查
```javascript
// 打开浏览器控制台，执行：

// 检查 1：API 别名是否正确
import { favoritesAPI } from '@/api'
console.log(favoritesAPI)  // 应显示对象，包含 getList 方法

// 检查 2：评价数据格式
const reviewData = {
  orderId: 123,
  orderItemId: 456,      // 应包含此字段
  productId: 789,
  rating: 5,
  content: "很好"
}
```

### 🔍 后端检查
```bash
# 查看后端日志，应看到：
# 1. 积分添加日志
# 2. SQL 执行日志
# 3. PointsService 方法调用

# 检查端口
netstat -ano | findstr :8080  # Windows
lsof -i :8080                 # macOS/Linux
```

### 🔍 数据库检查
```sql
-- 检查关键表状态
SELECT COUNT(*) FROM USERS;           -- 用户总数
SELECT COUNT(*) FROM REVIEWS;         -- 评价总数
SELECT COUNT(*) FROM POINTS_LOGS;     -- 积分日志总数
SELECT SUM(POINTS) FROM USERS;        -- 用户总积分

-- 检查约束和索引
DESC REVIEWS;  -- 查看 ORDER_ITEM_ID 是否 NOT NULL
DESC USERS;    -- 查看 POINTS 字段类型和是否可空
```

---

## 常见问题和排查

### ❌ 收藏页面仍然报错
**症状**：Still seeing "No static resource"  
**排查**：
```bash
# 1. 检查前端编译
cd frontend
npm run build
# 查看是否有报错

# 2. 检查 api/index.js 是否有 favoritesAPI
grep -n "export const favoritesAPI" src/api/index.js

# 3. 清除浏览器缓存
# 按 Ctrl+Shift+Delete 清除缓存后重新登录
```

### ❌ 评价提交仍报错 ORA-01400
**症状**：Still seeing "ORA-01400"  
**排查**：
```bash
# 1. 检查前端是否有 orderItemId
grep -n "orderItemId" src/views/user/Review.vue

# 2. 查看后端日志中评价提交的请求数据
# 搜索："ReviewController" 或 "submitReview"

# 3. 数据库检查
SELECT * FROM REVIEWS WHERE ORDER_ITEM_ID IS NULL;
# 如果有结果，说明旧数据未清理
```

### ❌ 积分不累计
**症状**：支付后积分没有增加  
**排查**：
```bash
# 1. 检查 OrderService 是否注入 PointsService
grep -n "@Autowired.*PointsService" backend/src/main/.../OrderService.java

# 2. 查看后端日志
# 搜索："Adding points" 或 "PointsService"

# 3. 数据库检查
SELECT * FROM POINTS_LOGS 
WHERE REASON = 'ORDER_PAY'
ORDER BY CREATE_TIME DESC
LIMIT 1;
# 应该有最新记录

# 4. 检查用户积分值
SELECT USER_ID, POINTS FROM USERS WHERE USER_ID = ?;
```

### ❌ 初始化 API 报错
**症状**：POST /points/admin/initialize 返回错误  
**排查**：
```bash
# 1. 检查管理员权限
# 确保是以管理员账号登录
# 查看 token 中是否包含 adminId

# 2. 查看后端日志
# 搜索："initializePoints" 或 "PointsController"

# 3. 检查 SQL 查询
SELECT COUNT(*) FROM ORDERS 
WHERE STATUS IN ('PAID', 'PENDING_SHIP', 'SHIPPED', 'COMPLETED', 'CLOSED');
```

---

## 测试结果提交

完成测试后，请记录：

```markdown
## 测试结果 - [日期]

### 收藏功能
- [ ] ✅ 通过 / [ ] ❌ 失败 / [ ] ⏭️ 跳过

### 评价提交
- [ ] ✅ 通过 / [ ] ❌ 失败 / [ ] ⏭️ 跳过

### 积分自动累计
- [ ] ✅ 通过 / [ ] ❌ 失败 / [ ] ⏭️ 跳过

### 用户积分初始化
- [ ] ✅ 通过 / [ ] ❌ 失败 / [ ] ⏭️ 跳过

### 备注
[任何错误信息、日志内容或需要改进的地方]
```

---

## 相关命令速查

```bash
# 重启后端
cd backend
mvn clean package -DskipTests
java -jar target/supermarket-backend-1.0.0.jar

# 启动前端开发服务器
cd frontend
npm run dev  # http://localhost:5173

# 连接 Oracle 数据库
sqlplus system/Oracle123@xe

# 查看后端日志（如果保存到文件）
tail -f backend.log | grep -E "(ERROR|积分|POINTS)"

# Maven 构建（仅编译，不打包）
mvn clean compile

# Maven 快速打包（仅打包，不测试）
mvn package -DskipTests -q
```

---

**文档版本**：v1.0  
**最后更新**：2026年5月9日  
**修复版本**：1.0.0  
