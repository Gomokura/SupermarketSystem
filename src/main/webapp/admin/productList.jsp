<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.supermarket.bean.User" %>
<%@ page import="com.supermarket.bean.Product" %>
<%@ page import="com.supermarket.bean.Category" %>
<%@ page import="com.supermarket.dao.CategoryDAO" %>
<%@ page import="java.util.List" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null || !"admin".equals(user.getRole())) {
        response.sendRedirect("../login.jsp");
        return;
    }
    List<Product> products = (List<Product>) request.getAttribute("products");
    CategoryDAO categoryDAO = new CategoryDAO();
    List<Category> categories = categoryDAO.getAllCategories();
%>
<!DOCTYPE html>
<html>
<head>
    <title>商品管理</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial; }
        .header { background: #2c3e50; color: white; padding: 15px 30px; }
        .container { padding: 30px; }
        .search-box { background: white; padding: 20px; border-radius: 8px; margin-bottom: 20px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
        .search-box input, .search-box select { padding: 8px; margin-right: 10px; border: 1px solid #ddd; border-radius: 4px; }
        .search-box button { padding: 8px 20px; background: #3498db; color: white; border: none; border-radius: 4px; cursor: pointer; }
        table { width: 100%; background: white; border-collapse: collapse; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background: #34495e; color: white; }
        tr:hover { background: #f5f5f5; }
        .btn { padding: 5px 10px; margin: 0 2px; border: none; border-radius: 3px; cursor: pointer; color: white; }
        .btn-edit { background: #3498db; }
        .btn-delete { background: #e74c3c; }
        .btn-add { background: #27ae60; padding: 10px 20px; margin-bottom: 20px; }
    </style>
</head>
<body>
    <div class="header">
        <h1>商品管理</h1>
    </div>
    <div class="container">
        <button class="btn btn-add" onclick="location.href='/SupermarketSystem/admin/addProduct.jsp'">添加商品</button>

        <div class="search-box">
            <form action="../product" method="get">
                <input type="hidden" name="action" value="search">
                <input type="text" name="keyword" placeholder="商品名称/供应商">
                <select name="categoryId">
                    <option value="">全部分类</option>
                    <% for (Category cat : categories) { %>
                    <option value="<%= cat.getCategoryId() %>"><%= cat.getCategoryName() %></option>
                    <% } %>
                </select>
                <select name="orderBy">
                    <option value="">默认排序</option>
                    <option value="price ASC">价格升序</option>
                    <option value="price DESC">价格降序</option>
                    <option value="stock ASC">库存升序</option>
                    <option value="stock DESC">库存降序</option>
                </select>
                <button type="submit">搜索</button>
            </form>
        </div>

        <table>
            <tr>
                <th>ID</th>
                <th>商品名称</th>
                <th>分类</th>
                <th>价格</th>
                <th>库存</th>
                <th>单位</th>
                <th>供应商</th>
                <th>状态</th>
                <th>操作</th>
            </tr>
            <% if (products != null) {
                for (Product p : products) { %>
            <tr>
                <td><%= p.getProductId() %></td>
                <td><%= p.getProductName() %></td>
                <td><%= p.getCategoryName() %></td>
                <td>¥<%= p.getPrice() %></td>
                <td><%= p.getStock() %></td>
                <td><%= p.getUnit() %></td>
                <td><%= p.getSupplier() %></td>
                <td><%= p.getStatus() %></td>
                <td>
                    <button class="btn btn-edit" onclick="location.href='/SupermarketSystem/admin/editProduct.jsp?id=<%= p.getProductId() %>'">编辑</button>
                    <button class="btn btn-delete" onclick="if(confirm('确认删除?')) location.href='../product?action=delete&productId=<%= p.getProductId() %>'">删除</button>
                </td>
            </tr>
            <% } } %>
        </table>
    </div>
</body>
</html>
