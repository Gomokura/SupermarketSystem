## 超市管理系统

项目文档已统一整理到 `docs/`，请从这里开始阅读：

- `docs/README.md`（项目总览 / 六端说明 / 目录导航）
- `docs/运行指南.md`（本地运行与常见问题）
- `docs/API文档.md`（接口说明）
- `docs/数据库设计.md`（表结构与关键字段）
- `docs/测试文档.md`（测试用例）
- `docs/部署运维.md`（部署与运维）

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
