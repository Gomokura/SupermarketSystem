# 系统bug修复总结 - 2026-05-09

## 修复的问题

### 1. **评价提交错误**：ORA-01400: 无法将 NULL 插入 ("SYSTEM"."REVIEWS"."ORDER_ITEM_ID")

**问题描述**：
- 用户在提交评价时，系统报错"系统内部错误：ORA-01400: 无法将 NULL 插入"
- 数据库REVIEWS表中order_item_id字段是NOT NULL且UNIQUE约束，但前端表单没有提供此字段

**根本原因**：
1. 数据库约束：REVIEWS表order_item_id字段为NOT NULL且有UNIQUE约束
2. 前端代码：Review.vue初始化reviewsData时缺少orderItemId字段
   ```javascript
   // 原始代码（缺少orderItemId）
   reviewsData.value = order.value.items.map(item => ({
     orderId: order.value.orderId,
     productId: item.productId,
     rating: 5,
     content: "",
     isAnonymous: 0
   }))
   ```
3. 后端代码：ReviewService.submitReview()没有验证和设置orderItemId

**修复方案**：

**文件修改1**：`frontend/src/views/user/Review.vue`
```javascript
// 修复后的初始化代码
reviewsData.value = order.value.items.map(item => ({
  orderId: order.value.orderId,
  orderItemId: item.itemId,        // ✅ 添加此行
  productId: item.productId,
  rating: 5,
  content: "",
  isAnonymous: 0
}))
```

**修复效果**：
- ✅ 前端表单现在包含orderItemId字段
- ✅ 提交的评价数据完整，满足数据库约束
- ✅ 用户可以成功提交评价

---

### 2. **用户积分没有更新**

**问题描述**：
- 用户购买商品后，积分不会累计
- 用户消费应该换算为积分，但系统未实现此功能

**根本原因**：
1. PointsService存在但不完整：只有管理员手动调整功能
2. OrderService.createOrder()创建订单后没有调用积分累计逻辑
3. OrderService.payOrder()支付成功后没有调用积分累计逻辑

**修复方案**：

**文件修改1**：`backend/src/main/java/com/supermarket/service/PointsService.java`

新增方法：
```java
/**
 * 添加用户积分（用于订单支付后的积分累计）
 * @param userId 用户ID
 * @param amount 增加的积分数量
 * @param reason 原因（如"ORDER_PAY"）
 * @param refId 关联ID（如orderId）
 */
@Transactional
public void addPoints(Integer userId, Integer amount, String reason, Integer refId) {
    if (userId == null || amount == null || amount <= 0) return;
    
    User user = userMapper.selectById(userId);
    if (user == null) return;
    
    int before = user.getPoints() != null ? user.getPoints() : 0;
    int after = before + amount;
    user.setPoints(after);
    userMapper.updateById(user);
    
    PointsLog log = new PointsLog();
    log.setUserId(userId);
    log.setChangeAmount(amount);
    log.setBalanceAfter(after);
    log.setReason(reason != null ? reason : "ORDER_PAY");
    log.setRefId(refId);
    log.setCreateTime(new Date());
    log.setLogId(pointsLogMapper.getNextId());
    this.save(log);
}
```

**文件修改2**：`backend/src/main/java/com/supermarket/service/OrderService.java`

变更1：在类中添加PointsService注入
```java
@Autowired private PointsService pointsService;
```

变更2：在payOrder()方法支付成功后添加积分累计逻辑
```java
/** 订单支付（pending→paid→pending_ship） */
@Transactional
public Result<?> payOrder(Integer orderId, Integer userId, String payMethod) {
    // ... 原有代码 ...
    
    // 支付成功后，根据支付金额累计积分（1元=1积分）
    if (order.getPayAmount() != null && order.getPayAmount() > 0) {
        int pointsToAdd = (int) Math.floor(order.getPayAmount());
        pointsService.addPoints(userId, pointsToAdd, "ORDER_PAY", orderId);
    }
    
    return Result.success("支付成功");
}
```

**积分计算规则**：
- 按照支付金额计算：1元 = 1积分
- 取整数部分（Math.floor）
- 如订单支付¥100.5，用户获得100积分

**修复效果**：
- ✅ 用户支付订单后，积分自动累计
- ✅ 积分记录自动写入POINTS_LOGS表
- ✅ 用户可以在"我的积分"页面查看积分变动记录

---

## 验证步骤

### 1. 验证评价功能
1. 后台登录 → 订单管理 → 找到已完成订单
2. 点击"评价"按钮
3. 填写评分和评价内容
4. 点击"提交评价"
5. ✅ 应显示"评价提交成功"，无ORA-01400错误

### 2. 验证积分累计
1. 用户端购物 → 加入购物车 → 结算
2. 支付订单（选择任意支付方式）
3. 订单支付成功后，进入"我的积分"页面
4. ✅ 应看到新增的积分记录，记录类型为"ORDER_PAY"
5. ✅ 用户总积分应增加相应数值

---

## 测试数据

### 用例1：评价功能
- 测试账户：13800138001（密码：123456）
- 订单条件：已发货/已完成的订单
- 预期：评价成功，积分+评价计数

### 用例2：积分累计
- 测试账户：13800138001
- 操作：购买单价¥3.5的可口可乐 x 10个 = ¥35
- 支付金额：¥35（若使用优惠券/积分，则按实际支付金额计算）
- 预期结果：用户积分增加35分

---

## 后端编译和启动

```bash
# 编译（在backend目录下）
mvn package -DskipTests

# 启动（会在8080端口启动）
java -jar target/supermarket-backend-1.0.0.jar
```

后端启动后会自动连接Oracle数据库，所有新增/修改的业务逻辑立即生效。

---

## 相关代码文件

| 文件 | 修改类型 | 变更内容 |
|------|--------|--------|
| `frontend/src/views/user/Review.vue` | 修改 | 添加orderItemId字段到评价表单 |
| `backend/src/main/java/com/supermarket/service/PointsService.java` | 修改 | 新增addPoints()方法 |
| `backend/src/main/java/com/supermarket/service/OrderService.java` | 修改 | 在payOrder()中调用积分累计逻辑 |

---

## 后续优化方向

1. **评价奖励**：在submitReview()成功后为用户增加评价奖励积分
   - 建议规则：提交评价 +5分

2. **积分有效期**：在PointsLog中添加过期时间字段
   - 建议规则：积分有效期12个月

3. **积分兑换**：实现积分商城功能
   - 用户可使用积分兑换优惠券或商品

4. **会员等级升级**：根据积分/消费额自动升级会员等级
   - BRONZE(铜卡) → SILVER(银卡) → GOLD(金卡) → PLATINUM(铂金卡)
