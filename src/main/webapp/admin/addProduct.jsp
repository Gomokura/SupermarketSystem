<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.supermarket.bean.Category" %>
<%@ page import="com.supermarket.dao.CategoryDAO" %>
<%@ page import="java.util.List" %>
<%
    CategoryDAO categoryDAO = new CategoryDAO();
    List<Category> categories = categoryDAO.getAllCategories();
%>
<!DOCTYPE html>
<html>
<head>
    <title>添加商品</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial; background: #f5f5f5; }
        .header { background: #2c3e50; color: white; padding: 15px 30px; }
        .container { max-width: 600px; margin: 30px auto; }
        .form-box { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .form-group { margin-bottom: 20px; }
        label { display: block; margin-bottom: 5px; font-weight: bold; color: #333; }
        input, select, textarea { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; }
        .btn { padding: 12px 30px; border: none; border-radius: 4px; cursor: pointer; margin-right: 10px; }
        .btn-submit { background: #27ae60; color: white; }
        .btn-cancel { background: #95a5a6; color: white; }
    </style>
</head>
<body>
    <div class="header">
        <h1>添加商品</h1>
    </div>
    <div class="container">
        <div class="form-box">
            <form action="../product" method="post">
                <input type="hidden" name="action" value="add">

                <div class="form-group">
                    <label>商品名称 *</label>
                    <input type="text" name="productName" required>
                </div>

                <div class="form-group">
                    <label>商品分类 *</label>
                    <select name="categoryId" required>
                        <option value="">请选择</option>
                        <% for (Category cat : categories) { %>
                        <option value="<%= cat.getCategoryId() %>"><%= cat.getCategoryName() %></option>
                        <% } %>
                    </select>
                </div>

                <div class="form-group">
                    <label>价格 *</label>
                    <input type="number" name="price" step="0.01" required>
                </div>

                <div class="form-group">
                    <label>库存 *</label>
                    <input type="number" name="stock" required>
                </div>

                <div class="form-group">
                    <label>单位</label>
                    <input type="text" name="unit" placeholder="如：瓶、包、个">
                </div>

                <div class="form-group">
                    <label>供应商</label>
                    <input type="text" name="supplier">
                </div>

                <div>
                    <button type="submit" class="btn btn-submit">提交</button>
                    <button type="button" class="btn btn-cancel" onclick="history.back()">取消</button>
                </div>
            </form>
        </div>
    </div>
</body>
</html>
