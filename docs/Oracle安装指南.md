# Oracle Database XE 安装指南

## 下载地址
https://www.oracle.com/database/technologies/xe-downloads.html

选择：Oracle Database 21c Express Edition for Windows x64

## 安装步骤

1. 双击下载的安装文件（OracleXE213_Win64.zip）
2. 解压后运行 setup.exe
3. 按照安装向导操作：
   - 选择安装路径（默认：C:\app\用户名\product\21c\）
   - 设置数据库密码（重要！请记住这个密码）
   - 建议密码：oracle123（或自己设置）
   - 等待安装完成（约10-15分钟）

## 安装后配置

### 1. 验证安装
打开命令提示符（CMD），输入：
```
sqlplus system/你的密码@localhost:1521/XE
```

### 2. 创建项目用户（可选）
```sql
CREATE USER supermarket IDENTIFIED BY super123;
GRANT CONNECT, RESOURCE, DBA TO supermarket;
```

### 3. 修改项目配置
安装完成后，修改项目中的 DBUtil.java：

```java
private static final String URL = "jdbc:oracle:thin:@localhost:1521:XE";
private static final String USER = "system";  // 或 supermarket
private static final String PASSWORD = "你设置的密码";
```

## 执行SQL脚本

1. 打开SQL Developer（Oracle自带）或SQL*Plus
2. 连接到数据库
3. 依次执行项目中的SQL脚本：
   - database/01_tables.sql
   - database/02_orders_inventory.sql
   - database/03_sequences_indexes.sql
   - database/04_init_data.sql

## 常见问题

### 端口被占用
如果1521端口被占用，可以修改端口：
- 打开 Oracle Net Configuration Assistant
- 修改监听器端口

### 服务未启动
打开服务管理器（services.msc），启动：
- OracleServiceXE
- OracleXETNSListener

### 忘记密码
以管理员身份运行CMD：
```
sqlplus / as sysdba
ALTER USER system IDENTIFIED BY 新密码;
```

## 下载链接（需要Oracle账号）
https://www.oracle.com/database/technologies/xe-downloads.html

如果没有账号，需要先注册（免费）。
