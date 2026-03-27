# 超市管理系统

## 项目简介

基于 **Vue 3 + Spring Boot + Oracle** 的前后端分离超市管理系统，支持六端：顾客端、管理后台、收银端、仓储进货端、数据看板端、配送员端。

## 技术栈

### 前端
- **框架**: Vue 3 + Composition API
- **UI组件**: Element Plus
- **图表**: ECharts
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP客户端**: Axios
- **构建工具**: Vite

### 后端
- **框架**: Spring Boot 3.2
- **ORM**: MyBatis Plus
- **安全**: JWT (JSON Web Token)
- **数据库**: Oracle 19c (XE)
- **构建工具**: Maven

## 功能模块

### 六端总览

| 端 | 面向用户 | 访问路径 | 核心定位 |
|----|---------|---------|----------|
| 顾客端（C端） | 普通消费者 | `http://localhost:3000/` | 商品浏览、购物下单、订单追踪、会员互动 |
| 管理后台（B端） | 管理员/店长/财务/客服 | `http://localhost:3000/admin` | 系统全局管理，商品/订单/用户/营销/报表 |
| 收银端 | 收银员 | `http://localhost:3000/cashier` | 线下快速收银、班次管理、会员识别 |
| 仓储进货端 | 仓库管理员 | `http://localhost:3000/admin`（仓管角色） | 采购入库、库存盘点、报损管理 |
| 数据看板端 | 店长/老板 | `http://localhost:3000/admin`（Dashboard） | 实时经营数据、销售趋势、库存健康度 |
| 配送员端 | 配送员 | `http://localhost:3000/courier` | 查看配送任务、更新配送状态、联系顾客 |

### 顾客端（C端）
- **首页**: Banner轮播、热销商品、推荐商品、促销活动入口
- **商品浏览**: 分类导航、关键词搜索、价格/品牌筛选、排序
- **商品详情**: SKU规格选择、低库存提示、商品评价展示
- **购物车**: 勾选结算、数量调整、失效商品处理
- **结算**: 选择地址、优惠券抵扣、积分抵扣、备注
- **订单**: 状态Tab查看、取消、确认收货、再次购买、去支付
- **售后**: 申请退款/退货、查看售后进度
- **评价**: 打分、文字、标签、匿名提交
- **优惠券中心**: 领券、我的优惠券（未使用/已使用/已过期）
- **积分流水**: 积分变动记录
- **站内消息**: 订单/促销/退款通知，未读角标
- **个人中心**: 修改信息、修改密码、收货地址管理、会员等级/积分展示

### 管理后台（B端）
- **数据看板**: 今日/本月核心指标、销售趋势图、商品排行、分类占比、时段分析
- **商品管理**: 增删改查、上下架、批量操作、SKU管理
- **分类管理**: 树形分类增删改
- **品牌管理**: 品牌CRUD、Logo上传
- **供应商管理**: 供应商档案维护
- **用户管理**: 列表、封禁/解封、手动调整积分
- **管理员账号**: CRUD、角色分配、禁用、重置密码
- **订单管理**: 多条件筛选、详情、分配配送员、强制取消
- **售后管理**: 退款申请审核、同意/拒绝
- **评价管理**: 隐藏/回复评价
- **优惠券管理**: 满减/折扣/品类券CRUD、暂停/恢复
- **秒杀活动**: 创建场次、关联商品、开始/暂停/结束
- **满减促销**: 创建阶梯满减活动
- **轮播图管理**: Banner上传、排序、启用/禁用
- **库存管理**: 入库/出库、库存流水、低库存预警
- **采购管理**: 采购单创建、审批、到货入库
- **库存盘点**: 创建盘点任务、录入实际数量、提交差异报告
- **报损登记**: 填写报损商品和原因，自动扣减库存
- **配送管理**: 配送员账号管理、任务分配与总览
- **财务报表**: 营收统计、支付方式分布
- **审计日志**: 所有增删改操作记录
- **消息通知**: 向用户发送站内信

### 收银端（K端）
- 开班/交班，备用金管理
- 商品名称/条码搜索
- 收银清单管理
- 现金收款（自动计算找零）/ 模拟支付
- 历史班次记录

### 配送员端（P端）
- 配送员独立登录
- 待配送任务列表
- 标记已取件 / 已送达 / 配送失败
- 历史配送记录、修改密码

## 系统特点

1. **六端覆盖**: 顾客端/管理后台/收银端/配送员端前端 + 完整后端
2. **前后端分离**: Vue 3 + Spring Boot，REST API通信
3. **JWT身份认证**: 三类角色独立Token（用户/管理员/配送员）
4. **全局异常处理**: 统一错误码与响应格式
5. **事务处理**: 订单创建含库存扣减、积分发放、优惠券核销
6. **完整营销体系**: 优惠券（满减/折扣/品类）、秒杀、积分
7. **库存全流程**: 采购入库、销售出库、盘点、报损全链路追踪
8. **数据看板**: ECharts 销售趋势、商品排行、分类占比等图表
9. **多角色权限**: 超级管理员/店长/商品专员/财务/客服/仓管/收银员/配送员

## 后端 API 总览（21 个 Controller，全部已实现）

| Controller | 路径前缀 | 说明 |
|-----------|---------|------|
| AuthController | `/auth` | 用户/管理员/配送员登录注册 |
| ProductController | `/products` | 商品CRUD、SKU、分类 |
| CartController | `/cart` | 购物车增删改查 |
| OrderController | `/orders` | C端订单、B端管理、收银台下单 |
| AddressController | `/addresses` | 收货地址管理 |
| AfterSaleController | `/after-sales` | 售后申请与审核 |
| ReviewController | `/reviews` | 评价提交与管理 |
| CouponController | `/coupons` | 领取、使用、管理、批量发券 |
| BannerController | `/banners` | 轮播图管理 |
| BrandController | `/brands` | 品牌管理 |
| SeckillController | `/seckill` | 秒杀活动C端展示与B端管理 |
| MessageController | `/messages` | 站内消息 |
| PointsController | `/points` | 积分余额与流水 |
| FavoriteController | `/favorites` | 商品收藏 |
| PromotionController | `/promotions` | 促销活动管理 |
| SupplierController | `/suppliers` | 供应商管理 |
| PurchaseController | `/purchase` | 采购单管理 |
| WarehouseController | `/warehouse` | 库存总览、报损登记、库存流水 |
| CashierController | `/cashier` | 开班/交班 |
| CourierController | `/courier` | 配送员任务管理 |
| StocktakeController | `/stocktake` | 库存盘点 |
| AdminController | `/admin` | 用户/订单/库存入出/统计/配送员/审计日志 |

## 项目结构

```
SupermarketSystem/
├── backend/
│   ├── pom.xml
│   ├── API文档.md
│   └── src/main/java/com/supermarket/
│       ├── common/
│       ├── config/
│       ├── controller/                         # 21个 Controller
│       ├── entity/                             # 30+ 个实体类
│       ├── mapper/                             # 30+ 个 Mapper
│       └── service/                            # 20+ 个 Service
│
├── frontend/
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── api/index.js
│       ├── router/index.js
│       ├── stores/user.js
│       ├── utils/request.js
│       └── views/
│           ├── Login.vue
│           ├── Layout.vue
│           ├── user/                           # 顾客端
│           │   ├── Home.vue                    # 已完成（组长A）
│           │   ├── Products.vue                # 已完成（组长A）
│           │   ├── ProductDetail.vue           # 已完成（组长A）
│           │   ├── Seckill.vue                 # 已完成（组长A）
│           │   ├── Cart.vue                    # 待完善（成员B）
│           │   ├── Checkout.vue                # 已完成（组长A）
│           │   ├── Orders.vue                  # 待完善（成员B）
│           │   ├── OrderDetail.vue             # 待完善（成员B）
│           │   ├── AfterSale.vue               # 待开发（成员B）
│           │   ├── Review.vue                  # 待开发（成员B）
│           │   ├── Favorites.vue               # 待开发（成员B）
│           │   ├── Address.vue                 # 待完善（成员C）
│           │   ├── Profile.vue                 # 待完善（成员C）
│           │   ├── Coupons.vue                 # 待开发（成员C）
│           │   ├── PointsLog.vue               # 待开发（成员C）
│           │   └── Messages.vue                # 待开发（成员C）
│           ├── admin/                          # 管理后台
│           │   ├── Layout.vue                  # 待完善（成员D）
│           │   ├── Dashboard.vue               # 待完善（成员E）
│           │   ├── Products.vue                # 已有
│           │   ├── Categories.vue              # 已有
│           │   ├── Users.vue                   # 已有
│           │   ├── Orders.vue                  # 待完善（成员D）
│           │   ├── Inventory.vue               # 已有
│           │   ├── Deliveries.vue              # 已有
│           │   ├── Promotions.vue              # 待完善（成员D）
│           │   ├── Finance.vue                 # 已有
│           │   ├── Admins.vue                  # 待开发（成员D）
│           │   ├── Brands.vue                  # 待开发（成员D）
│           │   ├── Banners.vue                 # 待开发（成员D）
│           │   ├── CouponsManage.vue           # 待开发（成员D）
│           │   ├── PurchaseOrders.vue          # 待开发（成员D）仓储进货端
│           │   ├── AfterSales.vue              # 待开发（成员B）
│           │   ├── Reviews.vue                 # 待开发（成员B）
│           │   ├── Suppliers.vue               # 待开发（成员C）仓储进货端
│           │   ├── AuditLog.vue                # 待开发（成员C）
│           │   ├── Seckill.vue                 # 待开发（成员E）
│           │   ├── Stocktake.vue               # 待开发（成员E）仓储进货端
│           │   ├── DamageRecords.vue           # 待开发（成员E）仓储进货端
│           │   └── Couriers.vue                # 待开发（成员E）
│           ├── cashier/                        # 收银端（待开发，成员E）
│           │   ├── Layout.vue
│           │   └── Cashier.vue
│           └── courier/                        # 配送员端（待开发，成员E）
│               ├── Layout.vue
│               └── Tasks.vue
│
├── database/
│   ├── all_tables.sql                  # v3.0 完整版（唯一需执行的脚本）
│   └── run_sql.py                      # Python执行脚本（解决编码问题）
│
├── README.md
├── 前端分工文档.md                      # 前端5人开发分工详细说明（六端全覆盖）
```

## 安装部署

### 1. 数据库配置

执行 `database/all_tables.sql`（v3.0 完整版，包含建表、序列、索引、初始数据，会自动清理旧表）：

```bash
sqlplus system/123456@localhost:1521/XE @database/all_tables.sql
```

> `database/` 目录下的 `01_tables.sql` ~ `08_additions.sql` 是旧版分散脚本，已被 `all_tables.sql` 整合替代，**无需执行**。

### 2. 后端配置

修改 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@//localhost:1521/XE
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

### 5. 访问地址

| 端 | 地址 | 说明 |
|----|------|------|
| 顾客端 | http://localhost:3000/ | 普通消费者 |
| 管理后台 | http://localhost:3000/admin | 管理员/店长/财务/客服 |
| 收银端 | http://localhost:3000/cashier | 收银员 |
| 仓储进货端 | http://localhost:3000/admin | 仓管角色登录后台使用库存/采购/盘点模块 |
| 数据看板端 | http://localhost:3000/admin | 店长/老板查看 Dashboard |
| 配送员端 | http://localhost:3000/courier | 配送员 |

**默认账户：**
- 管理员：`admin` / `admin123`
- 普通用户：`user01` / `user123`
- 配送员：账号由管理员后台创建，初始密码 `123456`

## 开发进度

- [x] 后端全部 API 接口（21个 Controller，160+ 个接口，编译通过）
- [x] 数据库表结构（30+ 张表）
- [x] 前端基础架构（路由、API封装、Pinia、拦截器）
- [x] 顾客端核心页面（首页、商品列表/详情、购物车、结算、订单、地址、个人中心、秒杀页）
- [x] 管理后台基础页面（Dashboard、商品、分类、用户、订单、库存、配送、促销、财务）
- [ ] 顾客端待完善：售后、评价、优惠券、积分、消息、收藏（成员B/C）
- [ ] 管理后台待开发：管理员/品牌/轮播图/优惠券/售后/评价/秒杀/盘点/报损/配送员/采购/供应商/审计日志（成员B/C/D/E）
- [ ] 收银端、配送员端、数据看板（成员E）

## 前端开发分工

详见 [`前端分工文档.md`](./前端分工文档.md)，按5人分工，包含每人负责的具体页面、调用接口、实现要点。

## 开发者

- 技术架构：Vue 3 + Spring Boot 3.2 + Oracle XE
- 课程项目：企业实训
