<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.supermarket.bean.User" %>
<%@ page import="com.supermarket.bean.OrderItem" %>
<%@ page import="java.util.List" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null || !"admin".equals(user.getRole())) {
        response.sendRedirect("../login.jsp");
        return;
    }
    List<OrderItem> items = (List<OrderItem>) request.getAttribute("items");
    int orderId = (int) request.getAttribute("orderId");
%>
<!DOCTYPE html>
<html>
<head>
    <title>订单详情 #<%= orderId %></title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial; background: #f5f5f5; }
        .header { background: #2c3e50; color: white; padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; }
        .header a { color: white; text-decoration: none; }
        .container { padding: 30px; max-width: 900px; }
        .card { background: white; padding: 25px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
        table { width: 100%; border-collapse: collapse; margin-top: 15px; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background: #34495e; color: white; }
        .total { text-align: right; margin-top: 15px; font-size: 18px; font-weight: bold; color: #e74c3c; }
    </style>
</head>
<body>
    <div class="header">
        <h1>订单详情 #<%= orderId %></h1>
        <a href="../adminOrder?keyword=&status=">← 返回订单列表</a>
    </div>
    <div class="container">
        <div class="card">
            <h2>商品明细</h2>
            <table>
                <tr>
                    <th>商品名称</th>
                    <th>单价</th>
                    <th>数量</th>
                    <th>小计</th>
                </tr>
                <% double total = 0;
                   if (items != null) for (OrderItem item : items) {
                       double sub = item.getPrice() * item.getQuantity();
                       total += sub; %>
                <tr>
                    <td><%= item.getProductName() %></td>
                    <td>¥<%= String.format("%.2f", item.getPrice()) %></td>
                    <td><%= item.getQuantity() %></td>
                    <td>¥<%= String.format("%.2f", sub) %></td>
                </tr>
                <% } %>
            </table>
            <div class="total">合计：¥<%= String.format("%.2f", total) %></div>
        </div>
    </div>
</body>
</html>
