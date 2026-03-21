<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.supermarket.bean.User" %>
<%@ page import="com.supermarket.dao.ProductDAO" %>
<%@ page import="com.supermarket.dao.CategoryDAO" %>
<%@ page import="com.supermarket.bean.Product" %>
<%@ page import="com.supermarket.bean.Category" %>
<%@ page import="java.util.List" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("../login.jsp");
        return;
    }
    ProductDAO productDAO = new ProductDAO();
    CategoryDAO categoryDAO = new CategoryDAO();
    List<Category> categories = categoryDAO.getAllCategories();
    List<Product> products = productDAO.searchProducts("", null, "price DESC");
%>
<!DOCTYPE html>
<html>
<head>
    <title>用户主页</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial; background: #f5f5f5; }
        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 15px 30px;
                  display: flex; justify-content: space-between; align-items: center; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .nav { display: flex; gap: 20px; }
        .nav a { color: white; text-decoration: none; padding: 8px 15px; border-radius: 5px; transition: 0.3s; }
        .nav a:hover { background: rgba(255,255,255,0.2); }
        .container { max-width: 1200px; margin: 30px auto; padding: 0 20px; }
        .categories { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin-bottom: 30px; }
        .category-card { background: white; padding: 20px; border-radius: 8px; text-align: center; cursor: pointer;
                        box-shadow: 0 2px 5px rgba(0,0,0,0.1); transition: 0.3s; }
        .category-card:hover { transform: translateY(-3px); box-shadow: 0 4px 15px rgba(0,0,0,0.2); }
        .section-title { font-size: 24px; margin: 30px 0 20px; color: #333; }
        .products { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
        .product-card { background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 5px rgba(0,0,0,0.1);
                       transition: 0.3s; }
        .product-card:hover { transform: translateY(-5px); box-shadow: 0 5px 15px rgba(0,0,0,0.2); }
        .product-info { padding: 15px; }
        .product-name { font-size: 16px; font-weight: bold; margin-bottom: 10px; }
        .product-price { color: #e74c3c; font-size: 20px; font-weight: bold; }
        .btn { background: #667eea; color: white; border: none; padding: 8px 15px; border-radius: 5px;
               cursor: pointer; transition: 0.3s; }
        .btn:hover { background: #5568d3; }
    </style>
</head>
<body>
    <div class="header">
        <h1>🛒 超市管理系统</h1>
        <div class="nav">
            <a href="index.jsp">首页</a>
            <a href="productBrowse.jsp">商品浏览</a>
            <a href="cart.jsp">购物车</a>
            <a href="myOrders.jsp">我的订单</a>
            <a href="profile.jsp">个人中心</a>
            <span>欢迎，<%= user.getRealName() %></span>
            <a href="../login.jsp">退出</a>
        </div>
    </div>
    <div class="container">
        <h2 class="section-title">商品分类</h2>
        <div class="categories">
            <% for (Category cat : categories) { %>
            <div class="category-card" onclick="location.href='productBrowse.jsp?category=<%= cat.getCategoryId() %>'">
                <h3><%= cat.getCategoryName() %></h3>
                <p><%= cat.getDescription() != null ? cat.getDescription() : "" %></p>
            </div>
            <% } %>
        </div>
        <h2 class="section-title">热销推荐</h2>
        <div class="products">
            <% for (int i = 0; i < Math.min(products.size(), 6); i++) {
                Product p = products.get(i); %>
            <div class="product-card">
                <div class="product-info">
                    <div class="product-name"><%= p.getProductName() %></div>
                    <div class="product-price">¥<%= p.getPrice() %></div>
                    <p>库存：<%= p.getStock() %></p>
                    <form action="../cart" method="post" style="margin-top: 10px;">
                        <input type="hidden" name="action" value="add">
                        <input type="hidden" name="productId" value="<%= p.getProductId() %>">
                        <input type="hidden" name="quantity" value="1">
                        <button type="submit" class="btn">加入购物车</button>
                    </form>
                </div>
            </div>
            <% } %>
        </div>
    </div>
</body>
</html>
