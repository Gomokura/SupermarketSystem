<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.supermarket.bean.User" %>
<%@ page import="com.supermarket.bean.Order" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null || !"admin".equals(user.getRole())) {
        response.sendRedirect("../login.jsp");
        return;
    }
    List<Order> orders = (List<Order>) request.getAttribute("orders");
    String keyword = request.getAttribute("keyword") != null ? (String) request.getAttribute("keyword") : "";
    String status  = request.getAttribute("status")  != null ? (String) request.getAttribute("status")  : "";
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
%>
<!DOCTYPE html>
<html>
<head>
    <title>订单管理</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial; background: #f5f5f5; }
        .header { background: #2c3e50; color: white; padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; }
        .header a { color: white; text-decoration: none; font-size: 14px; }
        .container { padding: 30px; }
        .search-box { background: white; padding: 20px; border-radius: 8px; margin-bottom: 20px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
        .search-box input, .search-box select { padding: 8px; margin-right: 10px; border: 1px solid #ddd; border-radius: 4px; }
        .search-box button { padding: 8px 20px; background: #3498db; color: white; border: none; border-radius: 4px; cursor: pointer; }
        table { width: 100%; background: white; border-collapse: collapse; box-shadow: 0 2px 5px rgba(0,0,0,0.1); border-radius: 8px; overflow: hidden; }
        th, td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background: #34495e; color: white; }
        tr:hover { background: #f9f9f9; }
        .badge { padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: bold; }
        .badge-pending   { background: #fff3cd; color: #856404; }
        .badge-completed { background: #d4edda; color: #155724; }
        .badge-cancelled { background: #f8d7da; color: #721c24; }
        .btn { padding: 4px 10px; border: none; border-radius: 3px; cursor: pointer; color: white; font-size: 12px; }
        .btn-detail   { background: #3498db; }
        .btn-complete { background: #27ae60; }
        .btn-cancel   { background: #e74c3c; }
        select.status-select { padding: 3px 6px; border-radius: 3px; border: 1px solid #ddd; font-size: 12px; }
    </style>
</head>
<body>
    <div class="header">
        <h1>订单管理</h1>
        <a href="index.jsp">← 返回首页</a>
    </div>
    <div class="container">
        <div class="search-box">
            <form action="../adminOrder" method="get">
                <input type="text" name="keyword" placeholder="订单号/用户名" value="<%= keyword %>">
                <select name="status">
                    <option value="">全部状态</option>
                    <option value="pending"   <%= "pending".equals(status)   ? "selected" : "" %>>待处理</option>
                    <option value="completed" <%= "completed".equals(status) ? "selected" : "" %>>已完成</option>
                    <option value="cancelled" <%= "cancelled".equals(status) ? "selected" : "" %>>已取消</option>
                </select>
                <button type="submit">搜索</button>
            </form>
        </div>

        <table>
            <tr>
                <th>订单号</th>
                <th>用户</th>
                <th>总金额</th>
                <th>状态</th>
                <th>支付方式</th>
                <th>下单时间</th>
                <th>操作</th>
            </tr>
            <% if (orders != null && !orders.isEmpty()) {
                for (Order o : orders) { %>
            <tr>
                <td><%= o.getOrderId() %></td>
                <td><%= o.getUsername() %></td>
                <td>¥<%= String.format("%.2f", o.getTotalAmount()) %></td>
                <td>
                    <span class="badge badge-<%= o.getOrderStatus() %>">
                        <% if ("pending".equals(o.getOrderStatus()))        out.print("待处理");
                           else if ("completed".equals(o.getOrderStatus())) out.print("已完成");
                           else if ("cancelled".equals(o.getOrderStatus())) out.print("已取消");
                           else out.print(o.getOrderStatus()); %>
                    </span>
                </td>
                <td><%= o.getPaymentMethod() %></td>
                <td><%= o.getOrderTime() != null ? sdf.format(o.getOrderTime()) : "-" %></td>
                <td>
                    <button class="btn btn-detail" onclick="location.href='../adminOrder?action=detail&orderId=<%= o.getOrderId() %>'">详情</button>
                    <% if ("pending".equals(o.getOrderStatus())) { %>
                    <form style="display:inline" method="post" action="../adminOrder">
                        <input type="hidden" name="action"    value="updateStatus">
                        <input type="hidden" name="orderId"   value="<%= o.getOrderId() %>">
                        <input type="hidden" name="newStatus" value="completed">
                        <button type="submit" class="btn btn-complete">完成</button>
                    </form>
                    <form style="display:inline" method="post" action="../adminOrder">
                        <input type="hidden" name="action"    value="updateStatus">
                        <input type="hidden" name="orderId"   value="<%= o.getOrderId() %>">
                        <input type="hidden" name="newStatus" value="cancelled">
                        <button type="submit" class="btn btn-cancel" onclick="return confirm('确认取消该订单？')">取消</button>
                    </form>
                    <% } %>
                </td>
            </tr>
            <% } } else { %>
            <tr><td colspan="7" style="text-align:center;color:#999;padding:30px;">暂无订单数据</td></tr>
            <% } %>
        </table>
    </div>
</body>
</html>
