<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.supermarket.bean.User, com.supermarket.bean.Address, com.supermarket.dao.AddressDAO, java.util.*" %>
<%
    User u = (User) session.getAttribute("user");
    if (u == null || !"user".equals(u.getRole())) { response.sendRedirect(request.getContextPath()+"/login.jsp"); return; }
    List<Address> addresses = new AddressDAO().getByUserId(u.getUserId());
    String msg = request.getParameter("msg");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>我的地址</title>
    <style>
        body { font-family: Arial, sans-serif; background: #f5f5f5; margin: 0; }
        .header { background: #2c7be5; color: #fff; padding: 14px 24px; display: flex; justify-content: space-between; }
        .header a { color: #fff; text-decoration: none; font-size: 14px; }
        .container { max-width: 700px; margin: 30px auto; padding: 0 16px; }
        .card { background: #fff; border-radius: 8px; padding: 24px; box-shadow: 0 1px 4px rgba(0,0,0,.1); margin-bottom: 20px; }
        h2 { color: #333; margin-bottom: 16px; }
        .addr-item { border: 1px solid #eee; border-radius: 6px; padding: 14px 16px; margin-bottom: 10px; display: flex; justify-content: space-between; align-items: center; }
        .addr-item.default { border-color: #2c7be5; background: #f0f6ff; }
        .addr-info { font-size: 14px; color: #333; }
        .addr-info .name { font-weight: bold; margin-bottom: 4px; }
        .addr-info .detail { color: #666; }
        .badge-default { background: #2c7be5; color: #fff; font-size: 11px; padding: 2px 8px; border-radius: 10px; margin-left: 8px; }
        .btn { padding: 5px 12px; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; margin-left: 6px; }
        .btn-danger  { background: #dc3545; color: #fff; }
        .btn-default { background: #6c757d; color: #fff; }
        form.add-form input { width: 100%; padding: 8px 10px; margin-bottom: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; box-sizing: border-box; }
        form.add-form button { background: #2c7be5; color: #fff; border: none; padding: 9px 20px; border-radius: 4px; cursor: pointer; font-size: 14px; }
        .msg-ok { color: #198754; margin-bottom: 12px; font-size: 14px; }
        .empty { color: #999; font-size: 14px; }
    </style>
</head>
<body>
<div class="header">
    <span>我的地址</span>
    <a href="${pageContext.request.contextPath}/user/index.jsp">← 返回首页</a>
</div>
<div class="container">
    <% if ("added".equals(msg)) { %><div class="msg-ok">地址添加成功</div><% } %>
    <% if ("deleted".equals(msg)) { %><div class="msg-ok">地址已删除</div><% } %>

    <div class="card">
        <h2>我的收货地址</h2>
        <% if (addresses.isEmpty()) { %>
            <div class="empty">暂无地址，请添加</div>
        <% } else { for (Address a : addresses) { %>
        <div class="addr-item <%= a.getIsDefault()==1 ? "default" : "" %>">
            <div class="addr-info">
                <div class="name">
                    <%= a.getReceiver() %> &nbsp; <%= a.getPhone() %>
                    <% if (a.getIsDefault()==1) { %><span class="badge-default">默认</span><% } %>
                </div>
                <div class="detail"><%= a.getDetail() %></div>
            </div>
            <div>
                <% if (a.getIsDefault()==0) { %>
                <form style="display:inline" method="post" action="<%= request.getContextPath() %>/address">
                    <input type="hidden" name="action" value="setDefault">
                    <input type="hidden" name="addressId" value="<%= a.getAddressId() %>">
                    <button class="btn btn-default" type="submit">设为默认</button>
                </form>
                <% } %>
                <form style="display:inline" method="post" action="<%= request.getContextPath() %>/address"
                      onsubmit="return confirm('确认删除？')">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="addressId" value="<%= a.getAddressId() %>">
                    <button class="btn btn-danger" type="submit">删除</button>
                </form>
            </div>
        </div>
        <% } } %>
    </div>

    <div class="card">
        <h2>添加新地址</h2>
        <form class="add-form" method="post" action="<%= request.getContextPath() %>/address">
            <input type="hidden" name="action" value="add">
            <input type="text" name="receiver" placeholder="收货人姓名" required>
            <input type="text" name="phone"    placeholder="联系电话" required>
            <input type="text" name="detail"   placeholder="详细地址" required>
            <button type="submit">保存地址</button>
        </form>
    </div>
</div>
</body>
</html>
