# 超市管理系统 · 测试指南

> **测试环境：** `http://localhost:8080`
>
> **目的：** 帮助测试人员系统化地验证系统各项功能，确保上线质量

---

## 一、测试前准备

### 1.1 账号准备

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | admin123 | 可访问所有管理功能 |
| 普通用户 | user01 | 123456 | 顾客端功能 |
| 配送员 | 13900000001 | 123456 | 配送端功能 |

### 1.2 测试工具

- **Apifox / Postman** - 推荐，用于测试带认证的接口
- **浏览器** - 可直接测试公开接口
- **数据库客户端** - 用于验证数据是否正确写入

### 1.3 测试数据

> 测试前确保数据库已初始化，包含以下测试数据：
> - 商品 ID: 1000, 1001, 1002
> - 分类 ID: 8 (饮料), 9 (食品)
> - 条码: 6901234500001

---

## 二、功能模块测试

### 2.1 认证模块

#### 登录测试

| 序号 | 测试项 | 测试步骤 | 预期结果 | 测试数据 |
|------|--------|----------|----------|----------|
| 1 | 管理员登录成功 | 输入账号密码，点击登录 | 登录成功，获取token | admin / admin123 |
| 2 | 普通用户登录成功 | 输入账号密码，点击登录 | 登录成功，获取token | user01 / 123456 |
| 3 | 配送员登录成功 | 输入账号密码，点击登录 | 登录成功，获取token | 13900000001 / 123456 |
| 4 | 用户名错误 | 输入错误用户名 | 提示用户名或密码错误 | admin123 / admin123 |
| 5 | 密码错误 | 输入错误密码 | 提示用户名或密码错误 | admin / 123456 |
| 6 | 用户名为空 | 不输入用户名 | 提示用户名不能为空 | (空) / admin123 |
| 7 | 密码为空 | 不输入密码 | 提示密码不能为空 | admin / (空) |

---

### 2.2 商品模块（公开接口）

#### 商品浏览测试

| 序号 | 测试项 | 测试步骤 | 预期结果 | 验证点 |
|------|--------|----------|----------|----------|
| 1 | 商品列表查询 | GET /products/list?pageNum=1&pageSize=10 | 返回分页商品列表 | total、pages字段 |
| 2 | 商品详情查看 | GET /products/1000 | 返回商品完整信息 | 名称、价格、库存 |
| 3 | 分类筛选 | GET /products/list?categoryId=8 | 只返回饮料类商品 | category_id=8 |
| 4 | 关键词搜索 | GET /products/list?keyword=可乐 | 返回包含"可乐"的商品 | 模糊匹配 |
| 5 | 价格升序 | GET /products/list?sortBy=price&sortOrder=asc | 按价格从低到高 | 第一条价格最低 |
| 6 | 价格降序 | GET /products/list?sortBy=price&sortOrder=desc | 按价格从高到低 | 第一条价格最高 |
| 7 | 条码查询 | GET /products/barcode/6901234500001 | 返回对应商品 | 条码匹配 |
| 8 | 推荐商品 | GET /products/recommended?limit=5 | 返回5条推荐商品 | 数量≤5 |
| 9 | 分类树结构 | GET /products/categories/tree | 返回树形分类 | 有层级关系 |
| 10 | 分类列表 | GET /products/categories/list | 返回平铺分类列表 | 所有分类 |

#### 边界测试

| 序号 | 测试项 | 测试步骤 | 预期结果 |
|------|--------|----------|----------|
| 1 | 空关键词搜索 | keyword=空 | 返回所有商品 |
| 2 | 不存在的商品ID | /products/999999 | 返回空或错误 |
| 3 | 不存在的条码 | /barcode/0000000000000 | 返回空或错误 |
| 4 | 负数分页 | pageNum=-1 | 参数校验错误 |
| 5 | 超大分页 | pageSize=1000 | 限制返回数量 |

---

### 2.3 购物车模块（需登录）

> **前置条件：** 先登录获取token，在Header中添加 `Authorization: Bearer <token>`

#### 基础功能测试

| 序号 | 测试项 | 测试步骤 | 预期结果 |
|------|--------|----------|----------|
| 1 | 添加商品到购物车 | POST /cart/add，body: {"productId":1001,"quantity":2} | 添加成功，返回购物车信息 |
| 2 | 查看购物车 | GET /cart/list | 显示已添加的商品 |
| 3 | 修改商品数量 | PUT /cart/update，body: {"cartId":1,"quantity":5} | 数量更新为5 |
| 4 | 删除单个商品 | DELETE /cart/1 | 商品从购物车移除 |
| 5 | 清空购物车 | DELETE /cart/clear | 购物车为空 |

#### 异常测试

| 序号 | 测试项 | 测试步骤 | 预期结果 |
|------|--------|----------|----------|
| 1 | 添加不存在的商品 | productId=999999 | 提示商品不存在 |
| 2 | 添加数量为0 | quantity=0 | 参数校验失败 |
| 3 | 添加负数数量 | quantity=-1 | 参数校验失败 |
| 4 | 数量超过库存 | quantity=99999 | 提示库存不足 |
| 5 | 未登录访问 | 不带token请求 | 401 Unauthorized |

---

### 2.4 订单模块（需登录）

#### 订单流程测试

| 序号 | 测试项 | 测试步骤 | 预期结果 |
|------|--------|----------|----------|
| 1 | 提交订单 | POST /orders/create，填写地址和商品 | 生成订单，返回orderId |
| 2 | 订单支付 | POST /orders/{orderId}/pay，body: {"payMethod":"alipay"} | 订单状态变为paid |
| 3 | 查看我的订单 | GET /orders/list | 显示用户所有订单 |
| 4 | 按状态筛选 | GET /orders/list?status=paid | 只显示已支付订单 |
| 5 | 查看订单详情 | GET /orders/{orderId} | 显示订单完整信息 |
| 6 | 取消订单 | PUT /orders/{orderId}/cancel | 订单状态变为cancelled |
| 7 | 确认收货 | PUT /orders/{orderId}/confirm | 订单状态变为completed |

#### 订单状态流转测试

```
测试目标：验证订单状态按正确顺序流转

步骤1: 提交订单 → 状态: pending (待支付)
步骤2: 支付订单 → 状态: paid (已支付)
步骤3: 管理员发货 → 状态: shipped (已发货)
步骤4: 确认收货 → 状态: completed (已完成)

验证点：每个步骤后检查订单状态是否正确更新
```

---

### 2.5 收货地址模块（需登录）

| 序号 | 测试项 | 测试步骤 | 预期结果 |
|------|--------|----------|----------|
| 1 | 新增收货地址 | POST /addresses，填写完整信息 | 添加成功 |
| 2 | 设为默认地址 | isDefault=1 | 该地址被标记为默认 |
| 3 | 修改地址 | PUT /addresses，修改部分信息 | 信息已更新 |
| 4 | 删除地址 | DELETE /addresses/{addressId} | 地址已删除 |
| 5 | 查看地址列表 | GET /addresses/list | 显示所有收货地址 |
| 6 | 无默认地址 | 所有地址isDefault=0 | 下单时提示选择地址 |

---

### 2.6 优惠券模块（需登录）

#### 用户侧测试

| 序号 | 测试项 | 测试步骤 | 预期结果 |
|------|--------|----------|----------|
| 1 | 领取优惠券 | POST /coupons/claim/{couponId} | 领取成功 |
| 2 | 查看我的优惠券 | GET /coupons/my | 显示已领取的优惠券 |
| 3 | 查看可用优惠券 | GET /coupons/available?orderAmount=99.9 | 显示满足条件的券 |
| 4 | 重复领取 | 再次领取同一优惠券 | 提示已领取 |

#### 管理侧测试（需管理员token）

| 序号 | 测试项 | 测试步骤 | 预期结果 |
|------|--------|----------|----------|
| 1 | 创建优惠券 | POST /coupons/admin，填写信息 | 创建成功 |
| 2 | 修改优惠券 | PUT /coupons/admin/{id} | 信息已更新 |
| 3 | 暂停优惠券 | PUT /coupons/admin/{id}/status?status=paused | 状态变为paused |
| 4 | 删除优惠券 | DELETE /coupons/admin/{id} | 优惠券已删除 |

---

### 2.7 评价模块（需登录）

#### 用户评价测试

| 序号 | 测试项 | 测试步骤 | 预期结果 |
|------|--------|----------|----------|
| 1 | 提交评价 | POST /reviews，填写评价内容 | 评价提交成功 |
| 2 | 查看商品评价 | GET /reviews/product/1001 | 显示该商品所有评价 |
| 3 | 带图评价 | images参数传递图片 | 图片显示在评价中 |
| 4 | 匿名评价 | isAnonymous=1 | 评价显示为匿名 |
| 5 | 评价星级 | rating=1~5 | 对应星级显示 |
| 6 | 重复评价 | 同一订单同一商品评价两次 | 提示已评价 |

#### 管理侧评价管理

| 序号 | 测试项 | 测试步骤 | 预期结果 |
|------|--------|----------|----------|
| 1 | 查看所有评价 | GET /reviews/admin/list | 显示所有评价 |
| 2 | 按星级筛选 | GET /reviews/admin/list?rating=5 | 只显示5星评价 |
| 3 | 回复评价 | PUT /reviews/admin/{id}/reply | 回复成功 |
| 4 | 隐藏评价 | PUT /reviews/admin/{id}/hidden | 评价被隐藏 |
| 5 | 删除评价 | DELETE /reviews/admin/{id} | 评价已删除 |

---

### 2.8 售后模块（需登录）

#### 售后申请测试

| 序号 | 测试项 | 测试步骤 | 预期结果 |
|------|--------|----------|----------|
| 1 | 仅退款申请 | POST /after-sales，asType=refund_only | 申请提交成功 |
| 2 | 退货退款申请 | asType=return_refund | 申请提交成功 |
| 3 | 查看我的售后 | GET /after-sales/my | 显示售后记录 |
| 4 | 查看售后详情 | GET /after-sales/{id} | 显示详细信息 |

#### 管理侧售后处理

| 序号 | 测试项 | 测试步骤 | 预期结果 |
|------|--------|----------|----------|
| 1 | 查看售后列表 | GET /after-sales/admin/list | 显示所有售后申请 |
| 2 | 审批通过 | PUT /after-sales/admin/{id}/handle，action=approve | 状态变为approved |
| 3 | 审批拒绝 | action=reject | 状态变为rejected |
| 4 | 完成退款 | PUT /after-sales/admin/{id}/refund | 退款成功 |

---

### 2.9 管理员功能测试

> **前置：** 使用管理员token `Authorization: Bearer <admin_token>`

#### 统计概览

| 序号 | 测试项 | 测试步骤 | 预期结果 | 验证点 |
|------|--------|----------|----------|--------|
| 1 | 获取统计数据 | GET /admin/statistics | 返回统计信息 | 用户数、商品数、订单数、今日营收 |

#### 商品管理测试

| 序号 | 测试项 | 测试步骤 | 预期结果 |
|------|--------|----------|----------|
| 1 | 后台商品列表 | GET /products/admin/list | 显示所有商品(含禁用) |
| 2 | 按状态筛选 | status=off_shelf | 只显示下架商品 |
| 3 | 新增商品 | POST /products | 商品创建成功 |
| 4 | 修改商品 | PUT /products/{id} | 信息已更新 |
| 5 | 删除商品 | DELETE /products/{id} | 逻辑删除成功 |
| 6 | 上架商品 | PUT /products/{id}/status?status=active | 状态变为active |
| 7 | 下架商品 | status=off_shelf | 状态变为off_shelf |
| 8 | 批量上下架 | PUT /products/batch/status | 批量操作成功 |
| 9 | 低库存预警 | GET /products/low-stock | 显示库存不足商品 |

#### 分类管理测试

| 序号 | 测试项 | 测试步骤 | 预期结果 |
|------|--------|----------|----------|
| 1 | 新增分类 | POST /products/categories | 分类创建成功 |
| 2 | 修改分类名 | PUT /products/categories/{id} | 分类名已更新 |
| 3 | 删除分类 | DELETE /products/categories/{id} | 分类已删除 |

#### 订单管理测试

| 序号 | 测试项 | 测试步骤 | 预期结果 |
|------|--------|----------|----------|
| 1 | 订单列表 | GET /orders/admin/list | 显示所有订单 |
| 2 | 按订单号搜索 | orderNo=ORD | 返回匹配订单 |
| 3 | 按用户筛选 | userId=1 | 显示该用户订单 |
| 4 | 商家发货 | PUT /orders/{id}/ship | 状态变为shipped |
| 5 | 管理员取消订单 | PUT /orders/{id}/admin-cancel | 订单取消 |

#### 用户管理测试

| 序号 | 测试项 | 测试步骤 | 预期结果 |
|------|--------|----------|----------|
| 1 | 用户列表 | GET /admin/users | 显示所有用户 |
| 2 | 关键词搜索 | keyword=user | 返回匹配用户 |
| 3 | 封禁用户 | PUT /users/{id}/status?status=banned | 用户被封禁 |
| 4 | 解封用户 | status=active | 用户恢复正常 |

#### 库存管理测试

| 序号 | 测试项 | 测试步骤 | 预期结果 |
|------|--------|----------|----------|
| 1 | 商品入库 | POST /admin/inventory/warehousing | 库存增加 |
| 2 | 商品出库 | POST /admin/inventory/outbound | 库存减少 |
| 3 | 查看库存流水 | GET /admin/inventory/logs | 显示进出记录 |
| 4 | 按商品查流水 | productId=1001 | 该商品所有流水 |
| 5 | 按类型查流水 | type=warehousing | 只看出库记录 |

#### 财务报表

| 序号 | 测试项 | 测试步骤 | 预期结果 | 验证点 |
|------|--------|----------|----------|--------|
| 1 | 财务报表 | GET /admin/finance | 返回财务数据 | 总收入、支出、利润 |
| 2 | 指定日期范围 | startDate=2026-03-01&endDate=2026-03-25 | 该期间数据 | 数据在指定范围内 |

---

### 2.10 配送员功能测试

> **前置：** 使用配送员token

| 序号 | 测试项 | 测试步骤 | 预期结果 |
|------|--------|----------|----------|
| 1 | 查看配送任务 | GET /courier/tasks | 显示分配给我的任务 |
| 2 | 按状态筛选 | status=pending | 只显示待取件任务 |
| 3 | 取件操作 | PUT /tasks/{id}/pickup | 状态变为配送中 |
| 4 | 完成配送 | PUT /tasks/{id}/complete | 任务完成 |
| 5 | 配送失败 | PUT /tasks/{id}/fail，填写失败原因 | 记录失败原因 |
| 6 | 更新在线状态 | PUT /courier/status?status=online | 状态已更新 |

---

## 三、端到端流程测试

### 3.1 完整购物流程

```
目的：验证从浏览商品到完成评价的完整用户旅程

【步骤1】用户登录
  POST /auth/login
  Body: {"username":"user01","password":"123456"}
  → 保存返回的token

【步骤2】浏览商品
  GET /products/list?pageNum=1&pageSize=10
  → 选择商品ID 1001

【步骤3】添加购物车
  POST /cart/add
  Header: Authorization: Bearer <token>
  Body: {"productId":1001,"quantity":2}

【步骤4】确认购物车
  GET /cart/list
  → 验证商品已添加

【步骤5】添加收货地址
  POST /addresses
  Body: {"receiverName":"张三","phone":"13800138000",
         "province":"广东省","city":"深圳市",
         "district":"南山区","detail":"科技园路1号","isDefault":1}

【步骤6】提交订单
  POST /orders/create
  Body: {"addressId":1,"paymentMethod":"alipay",
         "cartItems":[{"productId":1001,"quantity":2}]}
  → 记录返回的orderId

【步骤7】支付订单
  POST /orders/{orderId}/pay
  Body: {"payMethod":"alipay"}

【步骤8】管理员发货（换管理员账号）
  PUT /orders/{orderId}/ship

【步骤9】确认收货
  PUT /orders/{orderId}/confirm

【步骤10】提交评价
  POST /reviews
  Body: {"orderId":1,"productId":1001,"rating":5,
         "content":"商品很好，满意！"}
```

### 3.2 售后处理流程

```
目的：验证用户申请售后到管理员完成退款的完整流程

【步骤1】用户提交仅退款申请
  POST /after-sales
  Body: {"orderId":1,"asType":"refund_only",
         "reason":"商品损坏","refundAmount":29.9}

【步骤2】管理员查看售后列表
  GET /after-sales/admin/list
  → 找到对应售后申请，记录afterSaleId

【步骤3】管理员审批通过
  PUT /after-sales/admin/{afterSaleId}/handle
  Body: {"action":"approve","remark":"核实后同意退款"}

【步骤4】管理员完成退款
  PUT /after-sales/admin/{afterSaleId}/refund

【步骤5】验证结果
  GET /after-sales/{afterSaleId}
  → status=refunded
```

### 3.3 收银台快速下单流程

```
目的：测试线下收银功能

【步骤1】管理员登录
  POST /auth/admin/login

【步骤2】收银下单
  POST /orders/cashier
  Body: {"payMethod":"cash","receivedAmount":50.0,
         "cartItems":[
           {"productId":1001,"quantity":1},
           {"productId":1002,"quantity":2}
         ]}
  → 返回订单号和找零金额
```

---

## 四、边界值与异常测试

### 4.1 边界值测试

| 测试项 | 测试数据 | 预期结果 |
|--------|----------|----------|
| 订单数量边界 | 数量=1 | 成功 |
| 订单数量边界 | 数量=0 | 失败，提示数量必须大于0 |
| 订单数量边界 | 数量=-1 | 失败，参数校验不通过 |
| 订单数量边界 | 数量=99999 | 成功（库存足够情况下） |
| 价格边界 | price=0 | 失败，价格必须大于0 |
| 价格边界 | price=0.01 | 成功 |
| 价格边界 | price=999999.99 | 成功 |
| 评分边界 | rating=0 | 失败，评分范围1-5 |
| 评分边界 | rating=1 | 成功 |
| 评分边界 | rating=5 | 成功 |
| 评分边界 | rating=6 | 失败，超出范围 |
| 分页边界 | pageNum=1 | 成功 |
| 分页边界 | pageNum=0 | 失败，页码必须>=1 |
| 分页边界 | pageNum=999999 | 返回空数据 |

### 4.2 异常场景测试

| 场景 | 测试方法 | 预期结果 |
|------|----------|----------|
| token过期 | 使用过期token请求 | 401 Unauthorized |
| 无token访问受保护接口 | 不带Authorization头 | 401 Unauthorized |
| 普通用户访问管理员接口 | user token访问/admin/* | 403 Forbidden |
| 访问不存在的接口 | GET /not-exist | 404 Not Found |
| 商品库存不足时下单 | 库存=0时加入购物车 | 提示库存不足 |
| 重复提交订单 | 快速点击提交两次 | 只有一条订单 |
| 取消已发货订单 | 订单状态shipped时取消 | 提示无法取消 |

---

## 五、测试结果记录表

### 5.1 模块测试结果

| 模块 | 用例数 | 通过 | 失败 | 通过率 | 测试人 | 测试日期 |
|------|--------|------|------|--------|--------|----------|
| 认证模块 | | | | | | |
| 商品模块 | | | | | | |
| 购物车模块 | | | | | | |
| 订单模块 | | | | | | |
| 收货地址 | | | | | | |
| 优惠券 | | | | | | |
| 评价模块 | | | | | | |
| 售后模块 | | | | | | |
| 管理员功能 | | | | | | |
| 配送功能 | | | | | | |
| **总计** | | | | | | |

### 5.2 Bug记录表

| 序号 | Bug编号 | 模块 | 描述 | 严重程度 | 状态 | 发现日期 | 修复日期 |
|------|---------|------|------|----------|------|----------|----------|
| 1 | | | | | | | |
| 2 | | | | | | | |
| 3 | | | | | | | |

> **严重程度：** P1-致命 / P2-严重 / P3-一般 / P4-轻微

---

## 六、测试检查清单

### 上线前必检项

- [ ] 所有公开接口返回正常
- [ ] 用户登录/登出功能正常
- [ ] 购物车增删改查正常
- [ ] 订单创建、支付、取消流程正常
- [ ] 管理员所有CRUD功能正常
- [ ] 库存管理功能正常
- [ ] 售后流程完整可用
- [ ] 配送员任务功能正常
- [ ] 数据库数据正确写入
- [ ] 边界值和异常处理正常
- [ ] 不同角色权限控制正确
- [ ] 页面无JS错误
- [ ] 响应时间在可接受范围内

---

## 七、常见问题排查

| 现象 | 原因 | 解决方案 |
|------|------|----------|
| 401 Unauthorized | 未带token或token错误 | 重新登录获取token，检查Header格式 |
| 403 Forbidden | 权限不足 | 确认使用正确的账号角色 |
| 404 Not Found | 接口地址错误 | 核对接口文档 |
| 500 内部错误 | 后端代码异常 | 查看后端控制台日志 |
| ORA-00904 | 数据库字段不存在 | 重新执行初始化SQL |
| 数据没更新 | 缓存问题 | 清除浏览器缓存或重启服务 |
| 页面加载慢 | 数据库查询慢 | 检查是否有全表扫描 |

---

**文档版本：** v1.0
**创建日期：** 2026-03-25
**适用系统：** 超市管理系统
