<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.supermarket.bean.User" %>
<%@ page import="com.supermarket.dao.ProductDAO, com.supermarket.dao.UserDAO, com.supermarket.dao.OrderDAO" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null || !"admin".equals(user.getRole())) {
        response.sendRedirect("../login.jsp");
        return;
    }
    int totalProducts = new ProductDAO().countAllProducts();
    int totalUsers    = new UserDAO().countAllUsers();
    int totalOrders   = new OrderDAO().countAllOrders();
%>
<!DOCTYPE html>
<html>
<head>
    <title>管理员主页</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial; }
        .header { background: #2c3e50; color: white; padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; }
        .header h1 { font-size: 24px; }
        .user-info { font-size: 14px; }
        .container { display: flex; height: calc(100vh - 60px); }
        .sidebar { width: 200px; background: #34495e; color: white; padding: 20px 0; }
        .sidebar a { display: block; padding: 15px 20px; color: white; text-decoration: none; transition: 0.3s; }
        .sidebar a:hover { background: #2c3e50; }
        .content { flex: 1; padding: 30px; background: #ecf0f1; }
        .card { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .stats { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; margin-bottom: 30px; }
        .stat-box { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 25px; border-radius: 8px; text-align: center; }
        .stat-box h3 { font-size: 36px; margin-bottom: 10px; }
        .stat-box p { font-size: 14px; opacity: 0.9; }
    </style>
</head>
<body>
    <div class="header">
        <h1>超市管理系统 - 管理员</h1>
        <div class="user-info">
            欢迎，<%= user.getRealName() %> | <a href="../login.jsp" style="color: white;">退出</a>
        </div>
    </div>
    <div class="container">
        <div class="sidebar">
            <a href="index.jsp">首页</a>
            <a href="../product?action=search">商品管理</a>
            <a href="../adminInventory">库存管理</a>
            <a href="../adminPurchase">采购管理</a>
            <a href="../adminPrice">价格管理</a>
            <a href="../user?action=search">用户管理</a>
            <a href="../adminUserProfile">用户画像</a>
            <a href="../adminOrder?keyword=&status=">订单管理</a>
            <a href="../adminDelivery">配送调度</a>
            <a href="../adminPromotion">促销管理</a>
            <a href="../adminActivity">活动运营</a>
            <a href="../adminFinance">财务报表</a>
            <a href="../adminStatistics">统计分析</a>
            <a href="../adminAlert">异常预警</a>
            <a href="../adminDataIO">数据导入导出</a>
            <a href="../adminAudit">操作日志</a>
        </div>
        <div class="content">
            <div class="stats">
                <div class="stat-box">
                    <h3><%= totalProducts %></h3>
                    <p>商品总数</p>
                </div>
                <div class="stat-box" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
                    <h3><%= totalUsers %></h3>
                    <p>用户总数</p>
                </div>
                <div class="stat-box" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
                    <h3><%= totalOrders %></h3>
                    <p>订单总数</p>
                </div>
            </div>
            <div class="card">
                <h2>系统功能</h2>
                <p style="margin-top: 15px; line-height: 1.8;">
                    • 商品管理：添加、修改、删除商品信息，支持分类和库存管理<br>
                    • 用户管理：管理系统用户，分配权限<br>
                    • 订单管理：查看和处理订单<br>
                    • 统计分析：销售数据统计和报表导出
                </p>
            </div>
        </div>
    </div>
</body>
</html>
