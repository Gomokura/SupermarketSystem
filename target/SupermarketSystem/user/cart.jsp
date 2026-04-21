<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.supermarket.bean.User" %>
<%@ page import="com.supermarket.dao.CartDAO" %>
<%@ page import="com.supermarket.bean.Cart" %>
<%@ page import="java.util.List" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("../login.jsp");
        return;
    }
    CartDAO cartDAO = new CartDAO();
    List<Cart> cartList = cartDAO.getCartByUser(user.getUserId());
    double total = 0;
    for (Cart c : cartList) {
        total += c.getSubtotal();
    }
%>
<!DOCTYPE html>
<html>
<head>
    <title>购物车</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial; background: #f5f5f5; }
        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 15px 30px;
                  display: flex; justify-content: space-between; align-items: center; }
        .nav { display: flex; gap: 20px; }
        .nav a { color: white; text-decoration: none; padding: 8px 15px; border-radius: 5px; }
        .nav a:hover { background: rgba(255,255,255,0.2); }
        .container { max-width: 1000px; margin: 30px auto; padding: 0 20px; }
        .cart-card { background: white; border-radius: 8px; padding: 20px; margin-bottom: 20px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 15px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background: #f8f9fa; font-weight: bold; }
        .total { font-size: 24px; color: #e74c3c; font-weight: bold; text-align: right; margin-top: 20px; }
        .btn { background: #667eea; color: white; border: none; padding: 8px 15px; border-radius: 5px; cursor: pointer; }
        .btn:hover { background: #5568d3; }
        .btn-danger { background: #e74c3c; }
        .btn-danger:hover { background: #c0392b; }
    </style>
</head>
<body>
    <div class="header">
        <h1>🛒 购物车</h1>
        <div class="nav">
            <a href="index.jsp">首页</a>
            <a href="productBrowse.jsp">商品浏览</a>
            <a href="cart.jsp">购物车</a>
            <a href="myOrders.jsp">我的订单</a>
            <a href="../login.jsp">退出</a>
        </div>
    </div>
    <div class="container">
        <div class="cart-card">
            <h2>我的购物车</h2>
            <% if (cartList.isEmpty()) { %>
                <p style="text-align: center; padding: 40px; color: #999;">购物车是空的</p>
            <% } else { %>
                <table>
                    <tr>
                        <th>商品名称</th>
                        <th>单价</th>
                        <th>数量</th>
                        <th>小计</th>
                        <th>操作</th>
                    </tr>
                    <% for (Cart c : cartList) { %>
                    <tr>
                        <td><%= c.getProductName() %></td>
                        <td>¥<%= c.getPrice() %></td>
                        <td>
                            <form action="../cart" method="post" style="display: inline;">
                                <input type="hidden" name="action" value="update">
                                <input type="hidden" name="cartId" value="<%= c.getCartId() %>">
                                <button type="submit" name="quantity" value="<%= c.getQuantity() - 1 %>" class="btn" <%= c.getQuantity() <= 1 ? "disabled" : "" %>>-</button>
                                <%= c.getQuantity() %>
                                <button type="submit" name="quantity" value="<%= c.getQuantity() + 1 %>" class="btn">+</button>
                            </form>
                        </td>
                        <td>¥<%= c.getSubtotal() %></td>
                        <td>
                            <form action="../cart" method="post" style="display: inline;">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="cartId" value="<%= c.getCartId() %>">
                                <button type="submit" class="btn btn-danger">删除</button>
                            </form>
                        </td>
                    </tr>
                    <% } %>
                </table>
                <div class="total">总计：¥<%= total %></div>
                <div style="text-align: right; margin-top: 20px;">
                    <a href="checkout.jsp" class="btn" style="text-decoration: none; display: inline-block;">去结算</a>
                </div>
            <% } %>
        </div>
    </div>
</body>
</html>
