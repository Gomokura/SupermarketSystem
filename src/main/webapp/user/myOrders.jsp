<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.supermarket.bean.User" %>
<%@ page import="com.supermarket.dao.OrderDAO" %>
<%@ page import="com.supermarket.bean.Order" %>
<%@ page import="com.supermarket.bean.OrderItem" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("../login.jsp");
        return;
    }
    OrderDAO orderDAO = new OrderDAO();
    List<Order> orders = orderDAO.getOrdersByUser(user.getUserId());
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
%>
<!DOCTYPE html>
<html>
<head>
    <title>我的订单</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial; background: #f5f5f5; }
        .header { background: #3498db; color: white; padding: 15px 30px; }
        .container { max-width: 1000px; margin: 30px auto; padding: 0 20px; }
        .order-card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); margin-bottom: 20px; }
        .order-header { display: flex; justify-content: space-between; padding-bottom: 15px; border-bottom: 1px solid #ecf0f1; }
        .order-info { margin-top: 15px; }
        .order-item { padding: 10px 0; border-bottom: 1px solid #f5f5f5; }
        .status-completed { color: #27ae60; font-weight: bold; }
        .status-pending { color: #f39c12; font-weight: bold; }
        .status-cancelled { color: #e74c3c; font-weight: bold; }
        .nav { display: flex; gap: 20px; margin-top: 10px; }
        .nav a { color: white; text-decoration: none; }
        .btn { background: #667eea; color: white; border: none; padding: 8px 15px; border-radius: 5px; cursor: pointer; }
        .btn:hover { background: #5568d3; }
    </style>
</head>
<body>
    <div class="header">
        <h1>我的订单</h1>
        <div class="nav">
            <a href="index.jsp">首页</a>
            <a href="cart.jsp">购物车</a>
            <a href="profile.jsp">个人中心</a>
        </div>
    </div>
    <div class="container">
        <% if (orders.isEmpty()) { %>
            <div class="order-card" style="text-align: center; color: #999;">暂无订单</div>
        <% } else {
            for (Order order : orders) {
                List<OrderItem> items = orderDAO.getOrderItems(order.getOrderId());
                String statusClass = "status-pending";
                String statusText = "待处理";
                if ("completed".equals(order.getOrderStatus())) {
                    statusClass = "status-completed";
                    statusText = "已完成";
                } else if ("cancelled".equals(order.getOrderStatus())) {
                    statusClass = "status-cancelled";
                    statusText = "已取消";
                } else if ("shipping".equals(order.getOrderStatus())) {
                    statusClass = "status-pending";
                    statusText = "配送中";
                }
        %>
        <div class="order-card">
            <div class="order-header">
                <div>
                    <strong>订单号：</strong><%= order.getOrderId() %><br>
                    <strong>下单时间：</strong><%= sdf.format(order.getOrderTime()) %><br>
                    <strong>支付方式：</strong><%= order.getPaymentMethod() %>
                </div>
                <div>
                    <div class="<%= statusClass %>"><%= statusText %></div>
                    <% if ("pending".equals(order.getOrderStatus())) { %>
                    <form action="../orderStatus" method="post" style="margin-top: 10px;">
                        <input type="hidden" name="orderId" value="<%= order.getOrderId() %>">
                        <input type="hidden" name="status" value="shipping">
                        <button type="submit" class="btn" style="font-size: 12px; padding: 5px 10px;">发货</button>
                    </form>
                    <% } else if ("shipping".equals(order.getOrderStatus())) { %>
                    <form action="../orderStatus" method="post" style="margin-top: 10px;">
                        <input type="hidden" name="orderId" value="<%= order.getOrderId() %>">
                        <input type="hidden" name="status" value="completed">
                        <button type="submit" class="btn" style="font-size: 12px; padding: 5px 10px;">确认收货</button>
                    </form>
                    <% } %>
                </div>
            </div>
            <div class="order-info">
                <% for (OrderItem item : items) { %>
                <div class="order-item"><%= item.getProductName() %> x <%= item.getQuantity() %> = ¥<%= item.getSubtotal() %></div>
                <% } %>
                <div style="text-align: right; margin-top: 15px; font-size: 18px;">
                    <strong>总计：¥<%= order.getTotalAmount() %></strong>
                </div>
            </div>
        </div>
        <% } } %>
    </div>
</body>
</html>
