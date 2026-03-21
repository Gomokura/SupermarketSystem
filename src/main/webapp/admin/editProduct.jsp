<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.supermarket.bean.Product" %>
<%@ page import="com.supermarket.bean.Category" %>
<%@ page import="com.supermarket.dao.ProductDAO" %>
<%@ page import="com.supermarket.dao.CategoryDAO" %>
<%@ page import="java.util.List" %>
<%
    int productId = Integer.parseInt(request.getParameter("id"));
    ProductDAO dao = new ProductDAO();
    CategoryDAO categoryDAO = new CategoryDAO();
    List<Product> list = dao.searchProducts(null, null, null);
    List<Category> categories = categoryDAO.getAllCategories();
    Product product = null;
    for (Product p : list) {
        if (p.getProductId() == productId) {
            product = p;
            break;
        }
    }
%>
<!DOCTYPE html>
<html>
<head>
    <title>编辑商品</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial; background: #f5f5f5; }
        .header { background: #2c3e50; color: white; padding: 15px 30px; }
        .container { max-width: 600px; margin: 30px auto; }
        .form-box { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .form-group { margin-bottom: 20px; }
        label { display: block; margin-bottom: 5px; font-weight: bold; color: #333; }
        input, select { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; }
        .btn { padding: 12px 30px; border: none; border-radius: 4px; cursor: pointer; margin-right: 10px; }
        .btn-submit { background: #3498db; color: white; }
        .btn-cancel { background: #95a5a6; color: white; }
    </style>
</head>
<body>
    <div class="header">
        <h1>编辑商品</h1>
    </div>
    <div class="container">
        <div class="form-box">
            <form action="../product" method="post">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="productId" value="<%= product.getProductId() %>">

                <div class="form-group">
                    <label>商品名称</label>
                    <input type="text" name="productName" value="<%= product.getProductName() %>" required>
                </div>

                <div class="form-group">
                    <label>商品分类</label>
                    <select name="categoryId" required>
                        <% for (Category cat : categories) { %>
                        <option value="<%= cat.getCategoryId() %>" <%= product.getCategoryId()==cat.getCategoryId()?"selected":"" %>><%= cat.getCategoryName() %></option>
                        <% } %>
                    </select>
                </div>

                <div class="form-group">
                    <label>价格</label>
                    <input type="number" name="price" step="0.01" value="<%= product.getPrice() %>" required>
                </div>

                <div class="form-group">
                    <label>库存</label>
                    <input type="number" name="stock" value="<%= product.getStock() %>" required>
                </div>

                <div class="form-group">
                    <label>单位</label>
                    <input type="text" name="unit" value="<%= product.getUnit() %>">
                </div>

                <div class="form-group">
                    <label>供应商</label>
                    <input type="text" name="supplier" value="<%= product.getSupplier() %>">
                </div>

                <div class="form-group">
                    <label>状态</label>
                    <select name="status">
                        <option value="active" <%= "active".equals(product.getStatus())?"selected":"" %>>启用</option>
                        <option value="inactive" <%= "inactive".equals(product.getStatus())?"selected":"" %>>禁用</option>
                    </select>
                </div>

                <div>
                    <button type="submit" class="btn btn-submit">保存</button>
                    <button type="button" class="btn btn-cancel" onclick="history.back()">取消</button>
                </div>
            </form>
        </div>
    </div>
</body>
</html>
