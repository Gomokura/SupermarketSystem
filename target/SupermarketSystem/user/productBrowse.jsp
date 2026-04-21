<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.supermarket.bean.User" %>
<%@ page import="com.supermarket.bean.Product" %>
<%@ page import="com.supermarket.bean.Category" %>
<%@ page import="com.supermarket.dao.ProductDAO" %>
<%@ page import="com.supermarket.dao.CategoryDAO" %>
<%@ page import="java.util.List" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("../login.jsp");
        return;
    }
    ProductDAO dao = new ProductDAO();
    CategoryDAO categoryDAO = new CategoryDAO();
    String keyword = request.getParameter("keyword");
    String categoryId = request.getParameter("categoryId");
    Integer catId = null;
    if (categoryId != null && !categoryId.isEmpty()) {
        catId = Integer.parseInt(categoryId);
    }
    List<Product> products = dao.searchProducts(keyword, catId, "product_name ASC");
    List<Category> categories = categoryDAO.getAllCategories();
%>
<!DOCTYPE html>
<html>
<head>
    <title>商品浏览</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial; background: #f5f5f5; }
        .header { background: #3498db; color: white; padding: 15px 30px; display: flex; justify-content: space-between; }
        .container { max-width: 1200px; margin: 30px auto; padding: 0 20px; }
        .search-box { background: white; padding: 20px; border-radius: 8px; margin-bottom: 20px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
        .search-box input, .search-box select { padding: 10px; margin-right: 10px; border: 1px solid #ddd; border-radius: 4px; }
        .search-box button { padding: 10px 20px; background: #3498db; color: white; border: none; border-radius: 4px; cursor: pointer; }
        .products { display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 20px; }
        .product-card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); transition: 0.3s; }
        .product-card:hover { transform: translateY(-5px); box-shadow: 0 5px 15px rgba(0,0,0,0.2); }
        .product-name { font-size: 18px; font-weight: bold; margin-bottom: 10px; color: #2c3e50; }
        .product-price { font-size: 24px; color: #e74c3c; font-weight: bold; margin: 10px 0; }
        .product-info { color: #7f8c8d; font-size: 14px; margin: 5px 0; }
        .stock-low { color: #e74c3c; }
        .stock-ok { color: #27ae60; }
    </style>
</head>
<body>
    <div class="header">
        <h1>商品浏览</h1>
        <div>
            <a href="index.jsp" style="color: white; margin-right: 20px;">返回主页</a>
            <span><%= user.getRealName() %></span>
        </div>
    </div>
    <div class="container">
        <div class="search-box">
            <form method="get">
                <input type="text" name="keyword" placeholder="搜索商品名称" value="<%= keyword==null?"":keyword %>">
                <select name="categoryId">
                    <option value="">全部分类</option>
                    <% for (Category cat : categories) { %>
                    <option value="<%= cat.getCategoryId() %>" <%= String.valueOf(cat.getCategoryId()).equals(categoryId)?"selected":"" %>><%= cat.getCategoryName() %></option>
                    <% } %>
                </select>
                <button type="submit">搜索</button>
            </form>
        </div>

        <div class="products">
            <% if (products != null && !products.isEmpty()) {
                for (Product p : products) { %>
            <div class="product-card">
                <div class="product-name"><%= p.getProductName() %></div>
                <div class="product-price">¥<%= p.getPrice() %></div>
                <div class="product-info">分类：<%= p.getCategoryName() %></div>
                <div class="product-info">单位：<%= p.getUnit() %></div>
                <div class="product-info <%= p.getStock()<10?"stock-low":"stock-ok" %>">
                    库存：<%= p.getStock() %> <%= p.getStock()<10?"(库存紧张)":"" %>
                </div>
                <div class="product-info">供应商：<%= p.getSupplier() %></div>
                <form action="../cart" method="post" style="margin-top: 10px;">
                    <input type="hidden" name="action" value="add">
                    <input type="hidden" name="productId" value="<%= p.getProductId() %>">
                    <input type="hidden" name="quantity" value="1">
                    <button type="submit" style="width: 100%; padding: 10px; background: #3498db; color: white; border: none; border-radius: 4px; cursor: pointer;">加入购物车</button>
                </form>
            </div>
            <% } } else { %>
            <p style="grid-column: 1/-1; text-align: center; color: #7f8c8d;">暂无商品</p>
            <% } %>
        </div>
    </div>
</body>
</html>
