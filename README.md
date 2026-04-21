# 超市管理系统

## 项目简介
基于JavaServlet + JSP + JavaBean + Oracle数据库的超市管理系统

## 技术栈
- 后端：JavaServlet、JSP、JavaBean
- 数据库：Oracle Database
- 服务器：Tomcat 8.5+
- 构建工具：Maven

## 功能模块

### 管理员功能
- 用户管理：添加、修改、查询用户，分配权限
- 商品管理：商品增删改查、分类管理、库存管理
- 订单管理：查看订单、订单统计
- 数据统计：销售统计、数据导出

### 普通用户功能
- 商品浏览：查看商品信息、搜索商品
- 订单查询：查看个人订单记录
- 个人信息：修改个人资料

## 系统特点
1. 支持条件组合查询和模糊查询
2. 查询结果支持多字段排序
3. 权限控制：管理员和普通用户分离
4. 界面美观、操作便捷

## 安装部署

### 1. 数据库配置
执行database目录下的SQL脚本：
```
01_tables.sql          -- 创建表结构
02_orders_inventory.sql -- 创建订单和库存表
03_sequences_indexes.sql -- 创建序列和索引
04_init_data.sql       -- 初始化数据
```

### 2. 修改数据库连接
编辑 `src/main/java/com/supermarket/util/DBUtil.java`
修改数据库连接信息：
- URL
- 用户名
- 密码

### 3. 编译部署
```bash
mvn clean package
```
将生成的war包部署到Tomcat的webapps目录

### 4. 访问系统
http://localhost:8080/SupermarketSystem

默认账户：
- 管理员：admin / admin123
- 普通用户：user01 / user123

## 项目结构
```
SupermarketSystem/
├── database/              # 数据库脚本
├── docs/                  # 项目文档
├── src/
│   └── main/
│       ├── java/
│       │   └── com/supermarket/
│       │       ├── bean/      # JavaBean实体类
│       │       ├── dao/       # 数据访问层
│       │       ├── servlet/   # Servlet控制层
│       │       ├── filter/    # 过滤器
│       │       └── util/      # 工具类
│       └── webapp/
│           ├── admin/         # 管理员页面
│           ├── user/          # 用户页面
│           ├── WEB-INF/       # 配置文件
│           └── login.jsp      # 登录页面
└── pom.xml                # Maven配置
```

## 开发者
- 开发时间：2026年
- 课程项目：企业实训
