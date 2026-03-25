# 超市管理系统 · 接口测试文档

> **后端地址：** `http://localhost:8080`
> **认证方式：** Bearer Token（JWT）
> **测试工具推荐：** Apifox、Postman 或浏览器（无需认证接口可直接在地址栏访问）
> **需要认证的接口** 请在请求 Header 中添加：`Authorization: Bearer <token>`

---

## 快速开始

### 第一步：获取 Token

先登录拿 token，后续接口认证都要用到。

**管理员登录**（种子数据：`admin` / `admin123`）
```
POST http://localhost:8080/auth/admin/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```
→ 复制返回的 `token` 字段

**普通用户登录**（种子数据：`user01` / `123456`）
```
POST http://localhost:8080/auth/login
{
  "username": "user01",
  "password": "123456"
}
```

> **Apifox 设置方法：** 新建环境变量 `{{token}}`，在「认证」Tab 里添加全局 Header：`Authorization: Bearer {{token}}`，这样每个接口自动带上 token，无需手动填。

---

## 一、公开接口（无需登录，直接访问）

直接在浏览器地址栏或 Apifox 无 Header 状态下测试。

### 1.1 商品模块

| 用例 | 方法 | URL |
|------|------|-----|
| 商品列表（分页） | GET | `http://localhost:8080/products/list?pageNum=1&pageSize=10` |
| 商品列表（分类筛选） | GET | `http://localhost:8080/products/list?categoryId=8` |
| 商品列表（关键词搜索） | GET | `http://localhost:8080/products/list?keyword=可乐` |
| 商品列表（价格排序） | GET | `http://localhost:8080/products/list?sortBy=price&sortOrder=asc` |
| 商品详情 | GET | `http://localhost:8080/products/1000` |
| 条码查询 | GET | `http://localhost:8080/products/barcode/6901234500001` |
| 推荐商品 | GET | `http://localhost:8080/products/recommended?limit=5` |
| 分类树 | GET | `http://localhost:8080/products/categories/tree` |
| 分类列表 | GET | `http://localhost:8080/products/categories/list` |
| 商品评价列表 | GET | `http://localhost:8080/reviews/product/1001?pageNum=1&pageSize=5` |

### 1.2 轮播图模块

| 用例 | 方法 | URL |
|------|------|-----|
| 获取有效轮播图 | GET | `http://localhost:8080/banners/list` |

---

## 二、认证后接口（需先获取 token）

以下所有接口在请求 Header 中携带：`Authorization: Bearer <上面复制的token>`

---

### 2.1 管理员功能

#### 统计概览
```
GET /admin/statistics
```
**预期响应：** 用户数、商品数、订单数、今日订单、今日营收

#### 商品管理
| 用例 | 方法 | URL | Body（JSON） |
|------|------|-----|-------------|
| 后台商品列表 | GET | `/products/admin/list?pageNum=1&pageSize=10&status=active` | - |
| 新增商品 | POST | `/products` | 见下方示例 |
| 修改商品 | PUT | `/products/{productId}` | 见下方示例 |
| 删除商品（逻辑删除） | DELETE | `/products/{productId}` | - |
| 上下架 | PUT | `/products/{productId}/status?status=off_shelf` | - |
| 批量上下架 | PUT | `/products/batch/status` | `{"productIds":[1000,1001],"status":"active"}` |
| 低库存预警 | GET | `/products/low-stock` | - |
| 新增分类 | POST | `/products/categories` | `{"categoryName":"零食","parentId":null}` |
| 修改分类 | PUT | `/products/categories/{categoryId}` | `{"categoryName":"零食2"}` |
| 删除分类 | DELETE | `/products/categories/{categoryId}` | - |
| 获取商品 SKU | GET | `/products/{productId}/skus` | - |
| 新增/更新 SKU | POST | `/products/{productId}/skus` | `{"skus":[{"specName":"红色/L","price":19.9,"stock":50}]}` |
| 删除 SKU | DELETE | `/products/skus/{skuId}` | - |

**新增商品 Body 示例：**
```json
{
  "productName": "农夫山泉 550ml",
  "categoryId": 9,
  "brandId": 2,
  "supplierId": 2,
  "price": 2.0,
  "originalPrice": 2.5,
  "costPrice": 1.0,
  "stock": 1000,
  "stockWarning": 100,
  "unit": "瓶",
  "barcode": "6901234500999",
  "coverImage": "/uploads/product.jpg",
  "description": "天然矿泉水",
  "isRecommend": 1
}
```

#### 订单管理
| 用例 | 方法 | URL |
|------|------|-----|
| 后台订单列表 | GET | `/orders/admin/list?pageNum=1&pageSize=10` |
| 后台订单列表（状态筛选） | GET | `/orders/admin/list?status=paid` |
| 订单号模糊搜索 | GET | `/orders/admin/list?orderNo=ORD` |
| 按用户筛选 | GET | `/orders/admin/list?userId=1` |
| 订单详情 | GET | `/orders/{orderId}` |
| 商家发货 | PUT | `/orders/{orderId}/ship` |
| 管理员取消订单 | PUT | `/orders/{orderId}/admin-cancel` |

#### 用户管理
| 用例 | 方法 | URL |
|------|------|-----|
| 用户列表 | GET | `/admin/users?pageNum=1&pageSize=10` |
| 关键词搜索用户 | GET | `/admin/users?keyword=user` |
| 用户详情 | GET | `/admin/users/{userId}` |
| 封禁用户 | PUT | `/admin/users/{userId}/status?status=banned&banReason=违规` |
| 解封用户 | PUT | `/admin/users/{userId}/status?status=active` |

#### 优惠券管理
| 用例 | 方法 | URL | Body（JSON） |
|------|------|-----|------------|
| 优惠券列表 | GET | `/coupons/admin/list?pageNum=1&pageSize=10` | - |
| 创建优惠券 | POST | `/coupons/admin` | 见下方示例 |
| 修改优惠券 | PUT | `/coupons/admin/{couponId}` | `{"faceValue":20.0,"minAmount":100.0}` |
| 启用/暂停 | PUT | `/coupons/admin/{couponId}/status?status=paused` | - |
| 删除优惠券 | DELETE | `/coupons/admin/{couponId}` | - |

**创建优惠券 Body 示例：**
```json
{
  "couponName": "新人专享满50减10",
  "couponType": "full_reduction",
  "faceValue": 10.0,
  "minAmount": 50.0,
  "totalCount": 1000,
  "startTime": "2026-03-01 00:00:00",
  "endTime": "2026-12-31 23:59:59"
}
```

#### 评价管理
| 用例 | 方法 | URL |
|------|------|-----|
| 所有评价列表 | GET | `/reviews/admin/list?pageNum=1&pageSize=10` |
| 按星级筛选 | GET | `/reviews/admin/list?rating=5` |
| 回复评价 | PUT | `/reviews/admin/{reviewId}/reply` |
| 显示/隐藏评价 | PUT | `/reviews/admin/{reviewId}/hidden` |
| 删除评价 | DELETE | `/reviews/admin/{reviewId}` |

**回复评价 Body：** `{"reply":"感谢您的好评！"}`

#### 售后管理
| 用例 | 方法 | URL |
|------|------|-----|
| 售后列表 | GET | `/after-sales/admin/list?pageNum=1&pageSize=10` |
| 售后详情 | GET | `/after-sales/{afterSaleId}` |
| 审批售后 | PUT | `/after-sales/admin/{afterSaleId}/handle` |
| 完成退款 | PUT | `/after-sales/admin/{afterSaleId}/refund` |

**审批售后 Body：** `{"action":"approve","remark":"审批通过"}`
> action 取值：`approve`=同意，`reject`=拒绝

#### 轮播图管理
| 用例 | 方法 | URL | Body（JSON） |
|------|------|-----|------------|
| 所有轮播图 | GET | `/banners/admin/list` | - |
| 新增轮播图 | POST | `/banners/admin` | 见下方示例 |
| 修改轮播图 | PUT | `/banners/admin/{bannerId}` | `{"imageUrl":"/uploads/new.jpg","sortOrder":2}` |
| 删除轮播图 | DELETE | `/banners/admin/{bannerId}` | - |
| 启用/禁用 | PUT | `/banners/admin/{bannerId}/toggle` | - |

**新增轮播图 Body：** `{"imageUrl":"/uploads/banner/banner1.jpg","linkType":"product","linkTarget":"1001","sortOrder":1}`

#### 库存管理
| 用例 | 方法 | URL | Body（JSON） |
|------|------|-----|------------|
| 入库 | POST | `/admin/inventory/warehousing` | `{"productId":1001,"quantity":200,"remark":"补货入库"}` |
| 出库 | POST | `/admin/inventory/outbound` | `{"productId":1001,"quantity":50,"remark":"报损出库"}` |
| 库存流水 | GET | `/admin/inventory/logs?pageNum=1&pageSize=20` | - |
| 按商品查流水 | GET | `/admin/inventory/logs?productId=1001` | - |
| 按类型查流水 | GET | `/admin/inventory/logs?type=warehousing` | - |

#### 供应商管理
| 用例 | 方法 | URL | Body（JSON） |
|------|------|-----|------------|
| 供应商列表 | GET | `/admin/suppliers` | - |
| 新增供应商 | POST | `/admin/suppliers` | `{"supplierName":"可口可乐","contact":"张经理","phone":"13800000001"}` |
| 修改供应商 | PUT | `/admin/suppliers/{supplierId}` | `{"contact":"李经理"}` |
| 删除供应商 | DELETE | `/admin/suppliers/{supplierId}` | - |

#### 配送管理
| 用例 | 方法 | URL |
|------|------|-----|
| 配送任务列表 | GET | `/admin/deliveries` |
| 分配配送员 | PUT | `/admin/deliveries/{deliveryId}/assign?courierId=1` |
| 更新任务状态 | PUT | `/admin/deliveries/{deliveryId}/status?status=shipped` |
| 配送员列表 | GET | `/admin/couriers` |
| 启用/禁用配送员 | PUT | `/admin/couriers/{courierId}/status?status=offline` |

#### 促销活动管理
| 用例 | 方法 | URL | Body（JSON） |
|------|------|-----|------------|
| 促销活动列表 | GET | `/admin/promotions` | - |
| 创建促销活动 | POST | `/admin/promotions` | `{"name":"满100减20","startTime":"2026-04-01","endTime":"2026-04-30"}` |
| 修改促销活动 | PUT | `/admin/promotions/{promotionId}` | `{"name":"满100减25"}` |
| 删除促销活动 | DELETE | `/admin/promotions/{promotionId}` | - |

#### 采购管理
| 用例 | 方法 | URL | Body（JSON） |
|------|------|-----|------------|
| 采购单列表 | GET | `/admin/purchase-orders?pageNum=1&pageSize=10` | - |
| 创建采购单 | POST | `/admin/purchase-orders` | 见下方示例 |
| 审批采购单 | PUT | `/admin/purchase-orders/{poId}/approve` | `{"action":"approve"}` |
| 确认收货入库 | PUT | `/admin/purchase-orders/{poId}/receive` | - |

**创建采购单 Body：**
```json
{
  "order": {
    "supplierId": 1,
    "expectedDate": "2026-04-01",
    "remark": "紧急补货"
  },
  "items": [
    { "productId": 1001, "quantity": 200, "unitPrice": 2.5 }
  ]
}
```

#### 财务报表
| 用例 | 方法 | URL |
|------|------|-----|
| 财务报表 | GET | `/admin/finance` |
| 指定日期范围 | GET | `/admin/finance?startDate=2026-03-01&endDate=2026-03-25` |

#### 审计日志
| 用例 | 方法 | URL |
|------|------|-----|
| 审计日志列表 | GET | `/admin/audit-logs?pageNum=1&pageSize=20` |
| 按模块筛选 | GET | `/admin/audit-logs?module=ORDER` |

---

### 2.2 普通用户功能

#### 收货地址
| 用例 | 方法 | URL | Body（JSON） |
|------|------|-----|------------|
| 地址列表 | GET | `/addresses/list` | - |
| 新增收址 | POST | `/addresses` | 见下方示例 |
| 修改地址 | PUT | `/addresses` | `{"addressId":1,"receiverName":"张三"}` |
| 删除地址 | DELETE | `/addresses/{addressId}` | - |

**新增地址 Body：**
```json
{
  "receiverName": "张三",
  "phone": "13800138000",
  "province": "广东省",
  "city": "深圳市",
  "district": "南山区",
  "detail": "科技园路1号",
  "isDefault": 1
}
```

#### 购物车
| 用例 | 方法 | URL | Body（JSON） |
|------|------|-----|------------|
| 购物车列表 | GET | `/cart/list` | - |
| 添加商品 | POST | `/cart/add` | `{"productId":1001,"quantity":2}` |
| 修改数量 | PUT | `/cart/update` | `{"cartId":1,"quantity":3}` |
| 删除单个商品 | DELETE | `/cart/{cartId}` | - |
| 清空购物车 | DELETE | `/cart/clear` | - |

#### 订单
| 用例 | 方法 | URL | Body（JSON） |
|------|------|-----|------------|
| 我的订单列表 | GET | `/orders/list?pageNum=1&pageSize=10` | - |
| 按状态筛选 | GET | `/orders/list?status=paid` | - |
| 订单详情 | GET | `/orders/{orderId}` | - |
| 提交订单 | POST | `/orders/create` | 见下方示例 |
| 订单支付 | POST | `/orders/{orderId}/pay` | `{"payMethod":"alipay"}` |
| 取消订单 | PUT | `/orders/{orderId}/cancel` | - |
| 确认收货 | PUT | `/orders/{orderId}/confirm` | - |

**提交订单 Body：**
```json
{
  "addressId": 1,
  "paymentMethod": "alipay",
  "couponId": null,
  "pointsUsed": 0,
  "remark": "尽快发货",
  "cartItems": [
    { "productId": 1001, "quantity": 2 }
  ]
}
```

> paymentMethod 取值：`alipay` / `wechat` / `card` / `cash` / `pending`（货到付款）

#### 优惠券
| 用例 | 方法 | URL |
|------|------|-----|
| 领取优惠券 | POST | `/coupons/claim/{couponId}` |
| 我的优惠券 | GET | `/coupons/my` |
| 下单可用优惠券 | GET | `/coupons/available?orderAmount=99.9` |

#### 评价
| 用例 | 方法 | URL | Body（JSON） |
|------|------|-----|------------|
| 提交评价 | POST | `/reviews` | 见下方示例 |

**提交评价 Body：**
```json
{
  "orderId": 1,
  "productId": 1001,
  "rating": 5,
  "content": "商品质量很好，发货快！",
  "images": "/uploads/r1.jpg",
  "isAnonymous": 0
}
```

#### 售后
| 用例 | 方法 | URL | Body（JSON） |
|------|------|-----|------------|
| 提交售后申请 | POST | `/after-sales` | 见下方示例 |
| 我的售后列表 | GET | `/after-sales/my?pageNum=1&pageSize=10` | - |
| 售后详情 | GET | `/after-sales/{afterSaleId}` | - |

**提交售后 Body：**
```json
{
  "orderId": 1,
  "asType": "refund_only",
  "reason": "商品损坏",
  "refundAmount": 29.9
}
```

> asType 取值：`refund_only`=仅退款，`return_refund`=退货退款

---

### 2.3 配送员功能

> 配送员登录：`POST /auth/courier/login`，Body：`{"username":"13900000001","password":"123456"}`

| 用例 | 方法 | URL |
|------|------|-----|
| 我的配送任务 | GET | `/courier/tasks` |
| 按状态筛选任务 | GET | `/courier/tasks?status=pending` |
| 取件（开始配送） | PUT | `/courier/tasks/{taskId}/pickup` |
| 完成配送 | PUT | `/courier/tasks/{taskId}/complete` |
| 标记配送失败 | PUT | `/courier/tasks/{taskId}/fail` |
| 更新在线状态 | PUT | `/courier/status?status=online` |

**标记失败 Body：** `{"failReason":"无人接收"}`

---

## 三、端到端流程测试

### 流程 1：用户下单完整流程

```
① 登录（普通用户）  POST /auth/login
  → 获取 user token

② 添加购物车        POST /cart/add
  Body: {"productId":1001,"quantity":2}

③ 确认购物车        GET /cart/list

④ 新增收货地址      POST /addresses
  Body: {"receiverName":"张三","phone":"13800138000","province":"广东省",
         "city":"深圳市","district":"南山区","detail":"科技园路1号","isDefault":1}

⑤ 提交订单          POST /orders/create
  Body: {"addressId":1,"paymentMethod":"alipay","cartItems":[{"productId":1001,"quantity":2}]}
  → 获得 orderId

⑥ 订单支付          POST /orders/{orderId}/pay
  Body: {"payMethod":"alipay"}

⑦ 确认收货          PUT /orders/{orderId}/confirm

⑧ 提交评价          POST /reviews
  Body: {"orderId":1,"productId":1001,"rating":5,"content":"很满意！"}
```

### 流程 2：管理员处理订单流程

```
① 管理员登录        POST /auth/admin/login
  → 获取 admin token

② 查看待发货订单     GET /orders/admin/list?status=paid

③ 商家发货           PUT /orders/{orderId}/ship

④ 查看已完成订单     GET /orders/admin/list?status=completed
```

### 流程 3：售后处理流程

```
① 用户提交售后      POST /after-sales
  Body: {"orderId":1,"asType":"refund_only","reason":"商品损坏","refundAmount":29.9}

② 管理员查看售后    GET /after-sales/admin/list

③ 管理员审批同意    PUT /after-sales/admin/{afterSaleId}/handle
  Body: {"action":"approve","remark":"同意退款"}

④ 管理员完成退款    PUT /after-sales/admin/{afterSaleId}/refund
```

### 流程 4：收银台快速下单

```
① 管理员登录        POST /auth/admin/login

② 收银下单          POST /orders/cashier
  Body: {"payMethod":"cash","receivedAmount":50.0,
         "cartItems":[{"productId":1001,"quantity":1},{"productId":1002,"quantity":2}]}
```

---

## 四、浏览器快速测试（无需工具）

以下地址直接在浏览器地址栏粘贴访问，验证接口是否正常：

```
http://localhost:8080/products/list
http://localhost:8080/products/1000
http://localhost:8080/products/barcode/6901234500001
http://localhost:8080/products/recommended?limit=5
http://localhost:8080/products/categories/tree
http://localhost:8080/banners/list
http://localhost:8080/reviews/product/1001
```

---

## 五、常见错误排查

| 错误现象 | 可能原因 | 解决方法 |
|---------|---------|---------|
| 401 Unauthorized | 未带 token 或 token 过期 | 重新登录获取 token，Header 填 `Authorization: Bearer <token>` |
| 403 Forbidden | 权限不足（如普通用户访问管理员接口） | 确认登录的是 admin 账号 |
| 404 Not Found | 接口路径写错 | 对照本文档检查 URL 拼写 |
| 500 内部错误 | 后端代码异常 | 查看后端控制台日志输出 |
| ORA-00904 | 数据库表缺列 | 数据库初始化 SQL 未执行完整，重新执行 `all_tables_ascii.sql` |
| ORA-12514 | Oracle Listener 连接失败 | 检查 Oracle 服务是否启动，`lsnrctl status` 确认 |
| 连接超时 | Oracle 服务未启动 | Windows 服务中启动 Oracle 相关服务 |

---

## 六、订单状态说明

| 状态值 | 含义 | 触发操作 |
|--------|------|---------|
| `pending` | 待支付 | 用户提交订单后 |
| `paid` | 已支付（待发货） | 用户支付成功后 |
| `shipped` | 已发货 | 管理员发货后 |
| `completed` | 已完成 | 用户确认收货后 |
| `cancelled` | 已取消 | 用户或管理员取消后 |
| `after_sale` | 售后处理中 | 用户提交售后申请后 |
| `refunded` | 已退款 | 管理员完成退款后 |
