<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="/views/include/head.jsp"><jsp:param name="title" value="配送任务"/></jsp:include>
<style>
body { background: #f0f2f5; }
.header-bar { background: #2c3e50; color: #fff; padding: 15px; display: flex; justify-content: space-between; align-items: center; }
.task-card { background: #fff; border-radius: 8px; padding: 15px; margin-bottom: 10px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
</style>
<body>
<div class="header-bar">
    <h5 class="mb-0">配送员：${sessionScope.courierName}</h5>
    <a href="${pageContext.request.contextPath}/courierLogout.do" class="btn btn-sm btn-outline-light">退出</a>
</div>

<div class="container mt-3" id="taskList">
    <div class="text-center text-muted py-5">加载中...</div>
</div>

<script>
var ctx = '${pageContext.request.contextPath}';
$(function() {
    $.getJSON(ctx + '/courierTaskList.do', function(res) {
        if (res.code === 200 && res.data && res.data.length > 0) {
            var html = '';
            $.each(res.data, function(i, task) {
                html += '<div class="task-card">'
                    + '<h6>订单号：' + task.orderNo + '</h6>'
                    + '<p class="mb-1 text-muted small">收货人：' + task.receiverName + '</p>'
                    + '<p class="mb-2 text-muted small">' + task.address + '</p>'
                    + '<button class="btn btn-sm btn-success w-100" onclick="confirmPickup(' + task.orderId + ')">确认取货</button>'
                    + '</div>';
            });
            $('#taskList').html(html);
        } else {
            $('#taskList').html('<div class="text-center text-muted py-5">暂无配送任务</div>');
        }
    });
});
function confirmPickup(id) { alert('功能开发中'); }
</script>
</body></html>
