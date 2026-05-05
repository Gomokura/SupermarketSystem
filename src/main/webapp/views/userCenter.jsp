<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="include/head.jsp"><jsp:param name="title" value="个人中心"/></jsp:include>
<body>
<jsp:include page="include/header.jsp"/>

<div class="container mt-4">
    <div class="row">
        <div class="col-md-3">
            <div class="list-group">
                <a href="${pageContext.request.contextPath}/userInfo.do" class="list-group-item list-group-item-action active">个人信息</a>
                <a href="${pageContext.request.contextPath}/orderList.do" class="list-group-item list-group-item-action">我的订单</a>
                <a href="${pageContext.request.contextPath}/views/userCenter.jsp" class="list-group-item list-group-item-action">地址管理</a>
                <a href="${pageContext.request.contextPath}/logout.do" class="list-group-item list-group-item-action text-danger">退出登录</a>
            </div>
        </div>
        <div class="col-md-9">
            <div class="card">
                <div class="card-header">个人信息</div>
                <div class="card-body" id="userInfo">
                    加载中...
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="include/footer.jsp"/>

<script>
var ctx = '${pageContext.request.contextPath}';
$(function() {
    $.getJSON(ctx + '/userInfo.do', function(res) {
        if (res.code === 200) {
            var u = res.data;
            $('#userInfo').html(
                '<table class="table table-borderless">' +
                '<tr><td>用户名：</td><td>' + (u.username || '-') + '</td></tr>' +
                '<tr><td>昵称：</td><td>' + (u.nickname || '-') + '</td></tr>' +
                '<tr><td>真实姓名：</td><td>' + (u.realName || '-') + '</td></tr>' +
                '<tr><td>手机号：</td><td>' + (u.phone || '-') + '</td></tr>' +
                '<tr><td>邮箱：</td><td>' + (u.email || '-') + '</td></tr>' +
                '<tr><td>会员等级：</td><td><span class="badge bg-warning">' + (u.memberLevel || 'NORMAL') + '</span></td></tr>' +
                '<tr><td>积分：</td><td>' + (u.points || 0) + '</td></tr>' +
                '</table>'
            );
        } else {
            $('#userInfo').html('<div class="alert alert-danger">' + res.message + '</div>');
        }
    });
});
</script>
</body>
</html>
