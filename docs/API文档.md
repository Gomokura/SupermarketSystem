# 超市管理系统 · 后端 API 文档

> **Base URL：** `http://localhost:8080`
> **认证方式：** Bearer Token（JWT）
> 需要认证的接口请在 Header 中携带：`Authorization: Bearer <token>`

---

## 目录

1. [认证模块 `/auth`](#1-认证模块)
2. [商品模块 `/products`](#2-商品模块)
3. [购物车模块 `/cart`](#3-购物车模块)
4. [订单模块 `/orders`](#4-订单模块)
5. [地址模块 `/addresses`](#5-地址模块)
6. [优惠券模块 `/coupons`](#6-优惠券模块)
7. [评价模块 `/reviews`](#7-评价模块)
8. [售后模块 `/after-sales`](#8-售后模块)
9. [轮播图模块 `/banners`](#9-轮播图模块)
10. [配送员模块 `/courier`](#10-配送员模块)
11. [积分模块 `/points`](#11-积分模块)
12. [收藏模块 `/favorites`](#12-收藏模块)
13. [促销模块 `/promotions`](#13-促销模块)
14. [秒杀模块 `/seckill`](#14-秒杀模块)
15. [消息模块 `/messages`](#15-消息模块)
16. [管理后台模块 `/admin`](#16-管理后台模块)

---

## 通用说明

### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 200=成功，401=未登录，403=无权限，404=资源不存在，500=服务器错误 |
| message | string | 提示信息 |
| data | any | 返回数据（失败时为 null） |

### 订单状态说明

| 状态值 | 含义 |
|--------|------|
| `pending` | 待支付 |
| `paid` | 已支付（待发货） |
| `shipped` | 已发货 |
| `completed` | 已完成 |
| `cancelled` | 已取消 |
| `after_sale` | 售后处理中 |
| `refunded` | 已退款 |

---

## 1. 认证模块

### 1.1 用户登录

**POST** `/auth/login`
无需认证

**Request Body:**
```json
{
  "username": "13800138001",
  "password": "user123"
}
```

**Response:**
```json
{
  "code": 200,
  "data": {
    "token": "eyJ...",
    "userId": 1,
    "username": "13800138001",
    "nickname": "昵称",
    "avatarUrl": "/uploads/avatar.jpg",
    "memberLevel": "NORMAL",
    "points": 100
  }
}
```

---

### 1.2 用户注册

**POST** `/auth/register`
无需认证

**Request Body:**
```json
{
  "username": "newuser",
  "password": "123456",
  "phone": "13800138000",
  "nickname": "新用户",
  "realName": "张三",
  "email": "user@example.com"
}
```

---

### 1.3 获取当前用户信息

**GET** `/auth/userinfo`
需要认证（用户）

---

### 1.4 更新用户信息

**PUT** `/auth/userinfo`
需要认证（用户）

**Request Body:**
```json
{
  "nickname": "新昵称",
  "avatarUrl": "/uploads/new.jpg",
  "gender": "M",
  "email": "new@example.com"
}
```

> gender 取值：`M`=男，`F`=女，`U`=保密

---

### 1.5 修改密码

**PUT** `/auth/password`
需要认证（用户）

**Request Body:**
```json
{
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

---

### 1.6 管理员登录

**POST** `/auth/admin/login`
无需认证

**Request Body:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**
```json
{
  "code": 200,
  "data": {
    "token": "eyJ...",
    "adminId": 1,
    "username": "admin",
    "realName": "超级管理员",
    "role": "super_admin"
  }
}
```

> role 取值：`super_admin` / `store_manager` / `product_staff` / `finance` / `customer_service` / `warehouse` / `cashier`

---

### 1.7 获取管理员信息

**GET** `/auth/admin/info`
需要认证（管理员）

---

### 1.8 配送员登录

**POST** `/auth/courier/login`
无需认证

**Request Body:**
```json
{
  "username": "13900000001",
  "password": "courier123"
}
```

---

### 1.9 配送员修改密码

**PUT** `/auth/courier/password`
需要认证（配送员）

**Request Body:**
```json
{
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

---

## 2. 商品模块

### 2.1 商品列表（公开）

**GET** `/products/list`
无需认证

**Query Params:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页条数，默认10 |
| categoryId | int | 否 | 分类ID |
| keyword | string | 否 | 关键词（商品名/条码） |
| brandId | int | 否 | 品牌ID |
| sortBy | string | 否 | 排序字段：`price` / `sales` / `rating` |
| sortOrder | string | 否 | 排序方向：`asc` / `desc` |
| minPrice | double | 否 | 最低价格 |
| maxPrice | double | 否 | 最高价格 |

---

### 2.2 商品详情（公开）

**GET** `/products/{productId}`
无需认证

---

### 2.3 条码查询（公开）

**GET** `/products/barcode/{barcode}`
无需认证

---

### 2.4 推荐商品（公开）

**GET** `/products/recommended`
无需认证

**Query Params:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| limit | int | 否 | 返回数量，默认10 |

---

### 2.5 分类树（公开）

**GET** `/products/categories/tree`
无需认证

---

### 2.6 分类列表（公开）

**GET** `/products/categories/list`
无需认证

---

### 2.7 后台商品列表

**GET** `/products/admin/list`
需要认证（管理员）

**Query Params:**

| 参数 | 类型 | 说明 |
|------|------|------|
| pageNum | int | 页码 |
| pageSize | int | 每页条数 |
| categoryId | int | 分类筛选 |
| keyword | string | 关键词 |
| status | string | 状态：`active` / `off_shelf` |

---

### 2.8 新增商品

**POST** `/products`
需要认证（管理员）

**Request Body:**
```json
{
  "productName": "商品名称",
  "categoryId": 1,
  "brandId": 1,
  "supplierId": 1,
  "price": 9.9,
  "originalPrice": 12.0,
  "stock": 100,
  "stockWarning": 10,
  "unit": "瓶",
  "barcode": "6901234567890",
  "coverImage": "/uploads/product.jpg",
  "description": "商品描述",
  "isRecommend": 0
}
```

---

### 2.9 修改商品

**PUT** `/products/{productId}`
需要认证（管理员）

---

### 2.10 删除商品（逻辑删除）

**DELETE** `/products/{productId}`
需要认证（管理员）

---

### 2.11 上下架商品

**PUT** `/products/{productId}/status`
需要认证（管理员）

**Query Params:**

| 参数 | 类型 | 说明 |
|------|------|------|
| status | string | `active`=上架，`off_shelf`=下架 |

---

### 2.12 批量上下架

**PUT** `/products/batch/status`
需要认证（管理员）

**Request Body:**
```json
{
  "productIds": [1, 2, 3],
  "status": "active"
}
```

---

### 2.13 低库存预警列表

**GET** `/products/low-stock`
需要认证（管理员）

---

### 2.14 分类管理

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/products/categories` | 新增分类 |
| PUT | `/products/categories/{categoryId}` | 修改分类 |
| DELETE | `/products/categories/{categoryId}` | 删除分类 |

---

### 2.15 品牌管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/brands/admin/list` | 品牌列表（管理员） |
| POST | `/brands` | 新增品牌 |
| PUT | `/brands/{brandId}` | 修改品牌 |
| DELETE | `/brands/{brandId}` | 删除品牌 |

---

### 2.16 SKU 管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/products/{productId}/skus` | 获取商品 SKU 列表 |
| POST | `/products/{productId}/skus` | 新增/更新 SKU |
| DELETE | `/products/skus/{skuId}` | 删除 SKU |

---

## 3. 购物车模块

所有接口需要认证（用户）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/cart/list` | 获取购物车列表 |
| POST | `/cart/add` | 添加商品到购物车 |
| PUT | `/cart/update` | 修改购物车商品数量 |
| DELETE | `/cart/{cartId}` | 删除单个购物车商品 |
| DELETE | `/cart/clear` | 清空购物车 |

**添加购物车 Request Body:**
```json
{
  "productId": 1001,
  "quantity": 2,
  "skuId": null,
  "specName": null
}
```

**修改数量 Request Body:**
```json
{
  "cartId": 1,
  "quantity": 3
}
```

---

## 4. 订单模块

### 4.1 用户订单列表

**GET** `/orders/list`
需要认证（用户）

**Query Params:**

| 参数 | 类型 | 说明 |
|------|------|------|
| pageNum | int | 页码 |
| pageSize | int | 每页条数 |
| status | string | 状态筛选（见订单状态说明） |

---

### 4.2 订单详情

**GET** `/orders/{orderId}`
需要认证（用户）

---

### 4.3 提交订单

**POST** `/orders/create`
需要认证（用户）

**Request Body:**
```json
{
  "addressId": 1,
  "paymentMethod": "alipay",
  "couponId": null,
  "pointsUsed": 0,
  "remark": "尽快发货",
  "cartItems": [
    {
      "productId": 1001,
      "quantity": 2,
      "skuId": null,
      "specName": null
    }
  ]
}
```

> paymentMethod 取值：`alipay` / `wechat` / `card` / `cash` / `pending`（货到付款）

---

### 4.4 订单支付

**POST** `/orders/{orderId}/pay`
需要认证（用户）

**Request Body:**
```json
{
  "payMethod": "alipay"
}
```

---

### 4.5 取消订单

**PUT** `/orders/{orderId}/cancel`
需要认证（用户）

---

### 4.6 确认收货

**PUT** `/orders/{orderId}/confirm`
需要认证（用户）

---

### 4.7 后台订单列表

**GET** `/orders/admin/list`
需要认证（管理员）

**Query Params:**

| 参数 | 类型 | 说明 |
|------|------|------|
| pageNum | int | 页码 |
| pageSize | int | 每页条数 |
| status | string | 状态筛选 |
| orderNo | string | 订单号模糊查询 |
| userId | int | 按用户筛选 |

---

### 4.8 发货

**PUT** `/orders/{orderId}/ship`
需要认证（管理员）

---

### 4.9 管理员取消订单

**PUT** `/orders/{orderId}/admin-cancel`
需要认证（管理员）

**Request Body:**
```json
{
  "reason": "取消原因"
}
```

---

### 4.10 收银台快速下单

**POST** `/orders/cashier`
需要认证（管理员）

**Request Body:**
```json
{
  "payMethod": "cash",
  "receivedAmount": 50.0,
  "cartItems": [
    { "productId": 1001, "quantity": 1 }
  ]
}
```

---

## 5. 地址模块

所有接口需要认证（用户）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/addresses/list` | 获取用户地址列表 |
| POST | `/addresses` | 新增收货地址 |
| PUT | `/addresses` | 修改收货地址 |
| DELETE | `/addresses/{addressId}` | 删除收货地址 |

**新增/修改地址 Request Body:**
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

---

## 6. 优惠券模块

### 6.1 后台接口（管理员）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/coupons/admin/list` | 优惠券列表（分页+状态筛选） |
| POST | `/coupons/admin` | 创建优惠券 |
| PUT | `/coupons/admin/{couponId}` | 修改优惠券 |
| PUT | `/coupons/admin/{couponId}/status` | 启用/暂停优惠券 |
| DELETE | `/coupons/admin/{couponId}` | 删除优惠券 |

**创建优惠券 Request Body:**
```json
{
  "couponName": "新人专享券",
  "couponType": "full_reduction",
  "faceValue": 10.0,
  "minAmount": 50.0,
  "totalCount": 1000,
  "startTime": "2026-01-01 00:00:00",
  "endTime": "2026-12-31 23:59:59"
}
```

> couponType 取值：`full_reduction`=满减，`discount`=折扣，`category`=品类券
> faceValue：满减时=减免金额；折扣时=折扣率（如 0.85=八五折）

---

### 6.2 用户端接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/coupons/claim/{couponId}` | 领取优惠券 |
| GET | `/coupons/my` | 我的优惠券列表 |
| GET | `/coupons/available?orderAmount=99.9` | 查询下单可用优惠券 |

---

## 7. 评价模块

### 7.1 商品评价列表（公开）

**GET** `/reviews/product/{productId}`
无需认证

**Query Params:**

| 参数 | 类型 | 说明 |
|------|------|------|
| pageNum | int | 页码 |
| pageSize | int | 每页条数 |
| rating | int | 按星级筛选（1-5） |

---

### 7.2 提交评价

**POST** `/reviews`
需要认证（用户）

**Request Body:**
```json
{
  "orderId": 1,
  "productId": 1001,
  "rating": 5,
  "content": "商品质量很好",
  "images": "/uploads/r1.jpg,/uploads/r2.jpg",
  "isAnonymous": 0
}
```

---

### 7.3 后台评价管理（管理员）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/reviews/admin/list` | 所有评价列表 |
| PUT | `/reviews/admin/{reviewId}/reply` | 回复评价 |
| PUT | `/reviews/admin/{reviewId}/hidden` | 显示/隐藏评价 |
| DELETE | `/reviews/admin/{reviewId}` | 删除评价 |

**回复评价 Request Body:**
```json
{
  "reply": "感谢您的好评！"
}
```

---

## 8. 售后模块

### 8.1 用户端

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/after-sales` | 提交售后申请 |
| GET | `/after-sales/my` | 我的售后列表 |
| GET | `/after-sales/{afterSaleId}` | 售后申请详情 |

**提交售后 Request Body:**
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

### 8.2 后台管理（管理员）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/after-sales/admin/list` | 所有售后列表（分页+状态筛选） |
| PUT | `/after-sales/admin/{afterSaleId}/handle` | 审批售后（同意/拒绝） |
| PUT | `/after-sales/admin/{afterSaleId}/refund` | 完成退款 |

**审批售后 Request Body:**
```json
{
  "action": "approve",
  "remark": "审批通过"
}
```

> action 取值：`approve`=同意，`reject`=拒绝

---

## 9. 轮播图模块

### 9.1 获取有效轮播图（公开）

**GET** `/banners/list`
无需认证

---

### 9.2 后台管理（管理员）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/banners/admin/list` | 所有轮播图列表 |
| POST | `/banners/admin` | 新增轮播图 |
| PUT | `/banners/admin/{bannerId}` | 修改轮播图 |
| DELETE | `/banners/admin/{bannerId}` | 删除轮播图 |
| PUT | `/banners/admin/{bannerId}/toggle` | 启用/禁用轮播图 |

**新增轮播图 Request Body:**
```json
{
  "imageUrl": "/uploads/banner/banner1.jpg",
  "linkType": "product",
  "linkTarget": "1001",
  "sortOrder": 1
}
```

> linkType 取值：`product`=商品，`category`=分类，`activity`=活动，`none`=无跳转

---

## 10. 配送员模块

所有接口需要认证（配送员 token）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/courier/tasks` | 查看我的配送任务 |
| PUT | `/courier/tasks/{taskId}/pickup` | 取件（开始配送） |
| PUT | `/courier/tasks/{taskId}/complete` | 完成配送 |
| PUT | `/courier/tasks/{taskId}/fail` | 标记配送失败 |
| PUT | `/courier/status` | 更新在线状态 |

**查看任务 Query Params:**

| 参数 | 类型 | 说明 |
|------|------|------|
| status | string | 状态筛选：`pending` / `picking` / `done` / `failed` |

**标记失败 Request Body:**
```json
{
  "failReason": "无人接收"
}
```

**更新在线状态 Query Params:**

| 参数 | 类型 | 说明 |
|------|------|------|
| status | string | `online`=上线，`offline`=下线 |

---

## 11. 积分模块

所有接口需要认证（用户）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/points` | 获取当前积分余额 |
| GET | `/points/logs` | 获取积分变动流水（分页） |

---

## 12. 收藏模块

所有接口需要认证（用户）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/favorites` | 我的收藏列表 |
| POST | `/favorites` | 添加收藏 |
| DELETE | `/favorites/{productId}` | 取消收藏 |

**添加收藏 Request Body:**
```json
{
  "productId": 1001
}
```

---

## 13. 促销模块

### 13.1 后台管理（管理员）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/promotions/admin/list` | 促销活动列表 |
| POST | `/promotions` | 创建促销活动 |
| PUT | `/promotions/{promotionId}` | 修改促销活动 |
| DELETE | `/promotions/{promotionId}` | 删除促销活动 |

### 13.2 用户端（公开）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/promotions/active` | 获取当前有效促销活动列表 |

---

## 14. 秒杀模块

### 14.1 后台管理（管理员）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/seckill/admin/list` | 秒杀活动列表 |
| POST | `/seckill/admin` | 创建秒杀活动 |
| PUT | `/seckill/admin/{activityId}` | 修改秒杀活动 |
| PUT | `/seckill/admin/{activityId}/status` | 开始/暂停/结束秒杀 |
| DELETE | `/seckill/admin/{activityId}` | 删除秒杀活动 |

### 14.2 用户端（公开）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/seckill/active` | 获取当前秒杀场次 |
| GET | `/seckill/products/{activityId}` | 获取秒杀商品列表 |
| POST | `/seckill/orders/{activityId}` | 抢购秒杀商品 |

---

## 15. 消息模块

### 15.1 用户端

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/messages` | 我的消息列表（分页） |
| PUT | `/messages/{messageId}/read` | 标记消息已读 |
| GET | `/messages/unread-count` | 获取未读消息数量 |

### 15.2 后台管理（管理员）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/messages/send` | 向指定用户发送站内信 |
| GET | `/messages/admin/sent` | 我发送的消息记录 |

**发送消息 Request Body:**
```json
{
  "userId": 1,
  "title": "系统通知",
  "content": "您的订单已发货"
}
```

---

## 16. 管理后台模块

所有接口需要认证（管理员）

### 16.1 统计概览

**GET** `/admin/statistics`

**Response:**
```json
{
  "code": 200,
  "data": {
    "userCount": 100,
    "productCount": 50,
    "orderCount": 200,
    "todayOrder": 15,
    "todayRevenue": 3200.5
  }
}
```

---

### 16.2 用户管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/users` | 用户列表（分页+关键词搜索） |
| GET | `/admin/users/{userId}` | 用户详情（含订单数） |
| PUT | `/admin/users/{userId}/status` | 封禁/解封用户 |

**用户列表 Query Params:**

| 参数 | 类型 | 说明 |
|------|------|------|
| pageNum | int | 页码 |
| pageSize | int | 每页条数 |
| keyword | string | 用户名/手机号/昵称 |

**更新用户状态 Query Params：**

| 参数 | 类型 | 说明 |
|------|------|------|
| status | string | `active`=正常，`banned`=封禁 |
| banReason | string | 封禁原因（status=banned时填写） |

---

### 16.3 管理员账号管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/admins` | 管理员列表 |
| POST | `/admin/admins` | 新增管理员 |
| PUT | `/admin/admins/{adminId}` | 修改管理员信息 |
| PUT | `/admin/admins/{adminId}/password` | 重置管理员密码 |
| DELETE | `/admin/admins/{adminId}` | 删除管理员 |

---

### 16.4 库存管理

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/admin/inventory/warehousing` | 入库 |
| POST | `/admin/inventory/outbound` | 出库 |
| GET | `/admin/inventory/logs` | 库存流水（分页+商品/类型筛选） |

**入库/出库 Request Body:**
```json
{
  "productId": 1001,
  "quantity": 100,
  "remark": "补货入库"
}
```

---

### 16.5 配送管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/deliveries` | 配送任务列表 |
| PUT | `/admin/deliveries/{deliveryId}/assign` | 分配配送员 |
| PUT | `/admin/deliveries/{deliveryId}/status` | 更新任务状态 |

---

### 16.6 配送员账号管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/couriers` | 配送员列表 |
| POST | `/admin/couriers` | 新增配送员 |
| PUT | `/admin/couriers/{courierId}` | 修改配送员信息 |
| PUT | `/admin/couriers/{courierId}/status` | 启用/禁用配送员 |
| DELETE | `/admin/couriers/{courierId}` | 删除配送员 |

---

### 16.7 供应商管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/suppliers` | 供应商列表 |
| POST | `/suppliers` | 新增供应商 |
| PUT | `/suppliers/{supplierId}` | 修改供应商 |
| DELETE | `/suppliers/{supplierId}` | 删除供应商 |

---

### 16.8 采购管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/purchase/orders` | 采购单列表 |
| POST | `/purchase/orders` | 创建采购单 |
| PUT | `/purchase/orders/{poId}/approve` | 审批采购单 |
| PUT | `/purchase/orders/{poId}/receive` | 确认收货（同步入库） |

**创建采购单 Request Body:**
```json
{
  "order": {
    "supplierId": 1,
    "expectedDate": "2026-04-01",
    "remark": "紧急采购"
  },
  "items": [
    { "productId": 1001, "quantity": 200, "unitPrice": 2.5 }
  ]
}
```

---

### 16.9 库存盘点

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/stocktake/tasks` | 盘点任务列表 |
| POST | `/stocktake/tasks` | 创建盘点任务 |
| PUT | `/stocktake/tasks/{taskId}/submit` | 提交盘点结果 |
| GET | `/stocktake/tasks/{taskId}/report` | 查看盘点差异报告 |

---

### 16.10 报损登记

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/warehouse/damage` | 登记报损 |
| GET | `/warehouse/damage` | 报损记录列表 |

**报损登记 Request Body:**
```json
{
  "productId": 1001,
  "quantity": 5,
  "reason": "商品过期"
}
```

---

### 16.11 财务报表

**GET** `/admin/finance`

**Query Params:**

| 参数 | 类型 | 说明 |
|------|------|------|
| startDate | string | 开始日期（yyyy-MM-dd） |
| endDate | string | 结束日期（yyyy-MM-dd） |

**Response:**
```json
{
  "data": {
    "totalRevenue": 100000.0,
    "payMethodStats": [
      { "payMethod": "alipay", "amount": 60000.0 },
      { "payMethod": "wechat", "amount": 40000.0 }
    ]
  }
}
```

---

### 16.12 审计日志

**GET** `/admin/audit-logs`

**Query Params:**

| 参数 | 类型 | 说明 |
|------|------|------|
| pageNum | int | 页码 |
| pageSize | int | 每页条数 |
| module | string | 模块筛选 |

---

### 16.13 消息通知

**POST** `/messages/send`
向指定用户发送站内信，详见 15.2。

---

## 附录：免认证接口汇总

| 路径 | 说明 |
|------|------|
| POST `/auth/login` | 用户登录 |
| POST `/auth/register` | 用户注册 |
| POST `/auth/admin/login` | 管理员登录 |
| POST `/auth/courier/login` | 配送员登录 |
| GET `/products/list` | 商品列表 |
| GET `/products/{productId}` | 商品详情 |
| GET `/products/barcode/{barcode}` | 条码查询 |
| GET `/products/recommended` | 推荐商品 |
| GET `/products/categories/tree` | 分类树 |
| GET `/products/categories/list` | 分类列表 |
| GET `/reviews/product/{productId}` | 商品评价 |
| GET `/banners/list` | 轮播图 |
| GET `/promotions/active` | 有效促销活动 |
| GET `/seckill/active` | 当前秒杀场次 |
