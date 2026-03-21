-- 超市管理系统数据库设计
-- 1. 用户表
CREATE TABLE users (
    user_id NUMBER PRIMARY KEY,
    username VARCHAR2(50) UNIQUE NOT NULL,
    password VARCHAR2(100) NOT NULL,
    real_name VARCHAR2(50),
    role VARCHAR2(20) CHECK (role IN ('admin', 'user')),
    phone VARCHAR2(20),
    status VARCHAR2(10) DEFAULT 'active',
    create_time DATE DEFAULT SYSDATE
);

-- 2. 商品分类表
CREATE TABLE categories (
    category_id NUMBER PRIMARY KEY,
    category_name VARCHAR2(50) NOT NULL,
    description VARCHAR2(200)
);

-- 3. 商品表
CREATE TABLE products (
    product_id NUMBER PRIMARY KEY,
    product_name VARCHAR2(100) NOT NULL,
    category_id NUMBER,
    price NUMBER(10,2) NOT NULL,
    stock NUMBER DEFAULT 0,
    unit VARCHAR2(20),
    supplier VARCHAR2(100),
    status VARCHAR2(10) DEFAULT 'active',
    create_time DATE DEFAULT SYSDATE,
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
);
