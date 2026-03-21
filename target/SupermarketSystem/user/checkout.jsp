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
    if (cartList.isEmpty()) {
        response.sendRedirect("cart.jsp");
        return;
    }
    double total = 0;
    for (Cart c : cartList) {
        total += c.getSubtotal();
    }
%>
<!DOCTYPE html>
<html>
<head>
    <title>结算</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial; background: #f5f5f5; }
        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 15px 30px; }
        .container { max-width: 800px; margin: 30px auto; padding: 0 20px; }
        .card { background: white; border-radius: 8px; padding: 20px; margin-bottom: 20px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }
        .total { font-size: 24px; color: #e74c3c; font-weight: bold; text-align: right; margin: 20px 0; }
        .form-group { margin: 15px 0; }
        label { display: block; margin-bottom: 5px; font-weight: bold; }
        select { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; }
        .btn { background: #667eea; color: white; border: none; padding: 12px 30px; border-radius: 5px; cursor: pointer; font-size: 16px; }
        .btn:hover { background: #5568d3; }
    </style>
</head>
<body>
    <div class="header">
        <h1>🛒 订单结算</h1>
    </div>
    <div class="container">
        <div class="card">
            <h2>商品清单</h2>
            <table>
                <tr><th>商品名称</th><th>单价</th><th>数量</th><th>小计</th></tr>
                <% for (Cart c : cartList) { %>
                <tr>
                    <td><%= c.getProductName() %></td>
                    <td>¥<%= c.getPrice() %></td>
                    <td><%= c.getQuantity() %></td>
                    <td>¥<%= c.getSubtotal() %></td>
                </tr>
                <% } %>
            </table>
        </div>
        <div class="card">
            <form action="../order" method="post">
                <div class="form-group">
                    <label>支付方式</label>
                    <select name="paymentMethod" required>
                        <option value="cash">现金</option>
                        <option value="wechat">微信支付</option>
                        <option value="alipay">支付宝</option>
                        <option value="card">银行卡</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>优惠选择</label>
                    <select name="discount">
                        <option value="none">无优惠</option>
                        <option value="discount9">9折优惠</option>
                        <option value="discount100">满100减20</option>
                    </select>
                </div>
                <div class="total">应付金额：¥<%= total %></div>
                <button type="submit" class="btn">提交订单</button>
            </form>
        </div>
    </div>
</body>
</html>
