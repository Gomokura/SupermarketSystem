<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    com.supermarket.bean.User u = (com.supermarket.bean.User) session.getAttribute("user");
    if (u == null || !"courier".equals(u.getRole())) { response.sendRedirect(request.getContextPath()+"/login.jsp"); return; }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>我的配送任务</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 0; background: #f5f5f5; }
        .header { background: #2c7be5; color: #fff; padding: 14px 24px; display: flex; justify-content: space-between; align-items: center; }
        .header a { color: #fff; text-decoration: none; font-size: 14px; }
        .container { max-width: 900px; margin: 30px auto; padding: 0 16px; }
        h2 { color: #333; }
        table { width: 100%; border-collapse: collapse; background: #fff; border-radius: 8px; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,.1); }
        th { background: #2c7be5; color: #fff; padding: 12px; text-align: left; font-size: 14px; }
        td { padding: 11px 12px; border-bottom: 1px solid #eee; font-size: 14px; }
        tr:last-child td { border-bottom: none; }
        .badge { padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: bold; }
        .badge-dispatched { background: #fff3cd; color: #856404; }
        .badge-delivering { background: #cfe2ff; color: #084298; }
        .badge-done       { background: #d1e7dd; color: #0a3622; }
        .btn { padding: 5px 14px; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
        .btn-primary { background: #2c7be5; color: #fff; }
        .btn-success { background: #198754; color: #fff; }
        .empty { text-align: center; padding: 40px; color: #999; }
    </style>
</head>
<body>
<div class="header">
    <span>配送员工作台 — 欢迎，${sessionScope.user.username}</span>
    <a href="${pageContext.request.contextPath}/login.jsp">退出</a>
</div>
<div class="container">
    <h2>我的配送任务</h2>
    <c:choose>
        <c:when test="${empty tasks}">
            <div class="empty">暂无待处理任务</div>
        </c:when>
        <c:otherwise>
            <table>
                <tr>
                    <th>配送单号</th><th>订单号</th><th>客户</th>
                    <th>收货人</th><th>电话</th><th>地址</th>
                    <th>金额</th><th>状态</th><th>操作</th>
                </tr>
                <c:forEach var="t" items="${tasks}">
                    <tr>
                        <td>${t.deliveryId}</td>
                        <td>${t.orderId}</td>
                        <td>${t.username}</td>
                        <td>${t.receiver}</td>
                        <td>${t.phone}</td>
                        <td>${t.address}</td>
                        <td>¥${t.totalAmount}</td>
                        <td>
                            <span class="badge badge-${t.status}">
                                <c:choose>
                                    <c:when test="${t.status == 'dispatched'}">已派单</c:when>
                                    <c:when test="${t.status == 'delivering'}">配送中</c:when>
                                    <c:otherwise>${t.status}</c:otherwise>
                                </c:choose>
                            </span>
                        </td>
                        <td>
                            <c:if test="${t.status == 'dispatched'}">
                                <form method="post" action="${pageContext.request.contextPath}/courier/tasks" style="display:inline">
                                    <input type="hidden" name="deliveryId" value="${t.deliveryId}">
                                    <input type="hidden" name="newStatus" value="delivering">
                                    <button class="btn btn-primary" type="submit">开始配送</button>
                                </form>
                            </c:if>
                            <c:if test="${t.status == 'delivering'}">
                                <form method="post" action="${pageContext.request.contextPath}/courier/tasks" style="display:inline"
                                      onsubmit="return confirm('确认已送达？')">
                                    <input type="hidden" name="deliveryId" value="${t.deliveryId}">
                                    <input type="hidden" name="newStatus" value="done">
                                    <button class="btn btn-success" type="submit">确认送达</button>
                                </form>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </c:otherwise>
    </c:choose>
</div>
</body>
</html>
