# 超市管理系统

## 项目简介

基于 **Vue 3 + Spring Boot + Oracle** 的前后端分离超市管理系统

## 技术栈。。。。。


### 前端
- **框架**: Vue 3 + Composition API
- **UI组件**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP客户端**: Axios
- **构建工具**: Vite

### 后端
- **框架**: Spring Boot 3.2
- **ORM**: MyBatis Plus
- **安全**: JWT (JSON Web Token)
- **数据库**: Oracle 19c
- **构建工具**: Maven

## 功能模块

### 管理员功能
- **Dashboard**: 数据统计概览
- **商品管理**: 商品增删改查、上下架、SKU管理
- **分类管理**: 商品分类管理
- **品牌管理**: 商品品牌维护
- **用户管理**: 用户账号管理、权限控制
- **订单管理**: 查看所有订单、状态跟踪
- **库存管理**: 入库/出库、盘点、报损记录
- **采购管理**: 供应商管理、采购单处理
- **配送管理**: 配送员分配、配送任务跟踪
- **促销管理**: 促销活动、秒杀活动管理
- **优惠券管理**: 优惠券发放与核销
- **轮播图管理**: 首页Banner配置
- **售后管理**: 售后申请审核处理
- **财务报表**: 营收统计、支付记录
- **收银班次**: 收银员班次管理
- **审计日志**: 操作日志查询
- **消息通知**: 系统消息管理

### 普通用户功能
- **商品浏览**: 分类查看、商品搜索、Banner展示
- **购物车**: 添加商品、修改数量、删除、清空
- **下单结算**: 选择地址、选择支付方式、使用优惠券
- **我的订单**: 查看订单、取消订单、查看详情
- **售后服务**: 申请售后、跟踪售后进度
- **商品评价**: 购买后评价商品
- **收货地址**: 地址管理（增删改查）
- **优惠券**: 领取和使用优惠券
- **积分中心**: 查看积分记录
- **消息通知**: 接收系统消息
- **个人中心**: 个人信息管理

## 系统特点

1. **前后端分离架构**: 前端Vue + 后端Spring Boot，通过REST API通信
2. **JWT身份认证**: 安全的无状态认证机制，含全局拦截器
3. **响应式设计**: 支持多种设备访问
4. **Element Plus UI**: 美观的现代化界面
5. **事务处理**: 订单创建包含库存扣减、日志记录
6. **CORS跨域**: 支持前后端分离部署
7. **全局异常处理**: 统一的错误码与响应格式
8. **优惠券/促销体系**: 支持满减、秒杀等多种营销活动
9. **售后服务**: 完整的退换货售后流程
10. **积分体系**: 消费积分记录与管理
11. **多角色权限**: 管理员、普通用户、配送员三类角色

## 项目结构

```
项目根目录/
├── backend/                                    # Spring Boot 后端
│   ├── pom.xml                                 # Maven配置
│   ├── API文档.md                              # 接口文档
│   └── src/main/java/com/supermarket/
│       ├── common/                             # 通用类
│       │   ├── Result.java                     # 统一响应封装
│       │   ├── PageRequest.java                # 分页请求
│       │   ├── BusinessException.java          # 业务异常
│       │   └── GlobalExceptionHandler.java     # 全局异常处理
│       ├── config/                             # 配置类
│       │   ├── CorsConfig.java                 # 跨域配置
│       │   ├── JwtConfig.java                  # JWT配置
│       │   ├── MybatisPlusConfig.java          # MyBatis Plus配置
│       │   └── WebConfig.java                  # Web配置
│       ├── controller/                         # REST控制器
│       │   ├── AuthController.java             # 认证接口
│       │   ├── ProductController.java          # 商品接口
│       │   ├── CartController.java             # 购物车接口
│       │   ├── OrderController.java            # 订单接口
│       │   ├── AddressController.java          # 地址接口
│       │   ├── AdminController.java            # 管理端接口
│       │   ├── AfterSaleController.java        # 售后接口
│       │   ├── BannerController.java           # 轮播图接口
│       │   ├── CouponController.java           # 优惠券接口
│       │   ├── CourierController.java          # 配送员接口
│       │   └── ReviewController.java           # 评价接口
│       ├── dto/                                # 数据传输对象
│       │   ├── LoginRequest.java
│       │   ├── RegisterRequest.java
│       │   ├── CartRequest.java
│       │   └── CreateOrderRequest.java
│       ├── entity/                             # 实体类
│       │   ├── User.java / Admin.java          # 用户/管理员
│       │   ├── Product.java / ProductSku.java  # 商品/SKU
│       │   ├── Category.java / Brand.java      # 分类/品牌
│       │   ├── Cart.java                       # 购物车
│       │   ├── Order.java / OrderItem.java     # 订单/订单项
│       │   ├── Address.java                    # 收货地址
│       │   ├── Delivery.java / DeliveryTask.java # 配送
│       │   ├── Courier.java                    # 配送员
│       │   ├── Coupon.java / UserCoupon.java   # 优惠券
│       │   ├── Promotion.java                  # 促销活动
│       │   ├── Banner.java                     # 轮播图
│       │   ├── Review.java                     # 商品评价
│       │   ├── AfterSale.java                  # 售后申请
│       │   ├── Payment.java                    # 支付记录
│       │   ├── InventoryLog.java               # 库存日志
│       │   ├── PurchaseOrder.java / PurchaseOrderItem.java  # 采购单
│       │   ├── Supplier.java                   # 供应商
│       │   ├── StocktakeTask.java / StocktakeItem.java      # 盘点
│       │   ├── DamageRecord.java               # 报损记录
│       │   ├── SeckillActivity.java / SeckillProduct.java  # 秒杀
│       │   ├── CashierShift.java               # 收银班次
│       │   ├── AuditLog.java                   # 审计日志
│       │   ├── Message.java                    # 消息通知
│       │   └── PointsLog.java                  # 积分日志
│       ├── interceptor/                        # JWT拦截器
│       │   └── JwtInterceptor.java
│       ├── mapper/                             # MyBatis Mapper接口
│       └── service/                            # 业务服务层
│
├── frontend/                                   # Vue 3 前端
│   ├── index.html
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── App.vue
│       ├── main.js
│       ├── api/                                # API接口封装
│       │   └── index.js
│       ├── router/                             # 路由配置
│       │   └── index.js
│       ├── stores/                             # Pinia状态管理
│       │   └── user.js
│       ├── styles/                             # 公共样式
│       │   └── common.scss
│       ├── utils/                              # 工具函数
│       │   └── request.js                      # Axios封装
│       └── views/
│           ├── Login.vue                       # 登录注册页
│           ├── Layout.vue                      # 用户端布局
│           ├── user/                           # 用户页面
│           │   ├── Home.vue                    # 首页
│           │   ├── Products.vue                # 商品列表
│           │   ├── Cart.vue                    # 购物车
│           │   ├── Checkout.vue                # 结算页
│           │   ├── Orders.vue                  # 订单列表
│           │   ├── OrderDetail.vue             # 订单详情
│           │   ├── Address.vue                 # 地址管理
│           │   └── Profile.vue                 # 个人中心
│           └── admin/                          # 管理员页面
│               ├── Layout.vue                  # 管理端布局
│               ├── Dashboard.vue               # 数据概览
│               ├── Products.vue                # 商品管理
│               ├── Categories.vue              # 分类管理
│               ├── Users.vue                   # 用户管理
│               ├── Orders.vue                  # 订单管理
│               ├── Inventory.vue               # 库存管理
│               ├── Deliveries.vue              # 配送管理
│               ├── Promotions.vue              # 促销管理
│               └── Finance.vue                 # 财务报表
│
└── database/                                   # 数据库脚本
    ├── 01_tables.sql                           # 创建表结构
    ├── 02_orders_inventory.sql                 # 订单和库存表
    ├── 03_sequences_indexes.sql                # 序列和索引
    ├── 04_init_data.sql                        # 初始化数据
    ├── 05_cart.sql                             # 购物车表
    ├── 06_more_data.sql                        # 补充数据
    ├── 07_promotion_delivery.sql               # 促销和配送表
    └── 08_additions.sql                        # 附加扩展
```

## 安装部署

### 1. 数据库配置

执行 database 目录下的 SQL 脚本：

```bash
01_tables.sql              # 创建表结构
02_orders_inventory.sql    # 创建订单和库存表
03_sequences_indexes.sql   # 创建序列和索引
04_init_data.sql           # 初始化数据
05_cart.sql                # 购物车表
06_more_data.sql           # 补充测试数据
07_promotion_delivery.sql  # 促销和配送表
08_additions.sql           # 附加扩展脚本
```

### 2. 后端配置

修改 `backend/src/main/resources/application.yml` 中的数据库连接：

```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521:XE
    username: system
    password: 123456
```

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
# 后端运行在 http://localhost:8080
```

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
# 前端运行在 http://localhost:3000
```

### 5. 访问系统

打开浏览器访问：http://localhost:3000

**默认账户：**
- 管理员：admin / admin123
- 普通用户：user01 / user123

## API 接口

### 认证接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 用户登录 |
| POST | /api/auth/register | 用户注册 |
| GET | /api/auth/userinfo | 获取用户信息 |

### 商品接口
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/products/list | 商品列表 |
| GET | /api/products/{id} | 商品详情 |
| GET | /api/products/categories | 分类列表 |

### 购物车接口
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/cart/list | 购物车列表 |
| POST | /api/cart/add | 添加商品 |
| PUT | /api/cart/update | 修改数量 |
| DELETE | /api/cart/{id} | 删除商品 |
| DELETE | /api/cart/clear | 清空购物车 |

### 订单接口
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/orders/list | 订单列表 |
| GET | /api/orders/{id} | 订单详情 |
| POST | /api/orders/create | 创建订单 |
| PUT | /api/orders/cancel/{id} | 取消订单 |

### 管理端接口
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/admin/users | 用户列表 |
| GET | /api/admin/statistics | 统计数据 |
| POST | /api/admin/inventory/warehousing | 入库 |
| POST | /api/admin/inventory/outbound | 出库 |

## 开发者

- 开发时间：2026年
- 技术架构：Vue 3 + Spring Boot + Oracle
- 课程项目：企业实训
