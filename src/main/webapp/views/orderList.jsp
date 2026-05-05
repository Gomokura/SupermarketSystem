<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="include/head.jsp"><jsp:param name="title" value="我的订单"/></jsp:include>
<body>
<jsp:include page="include/header.jsp"/>

<div class="container mt-4">
    <h2>我的订单</h2>

    <!-- 订单状态筛选 -->
    <div class="mb-3">
        <a href="?status=" class="btn btn-sm btn-outline-primary">全部</a>
        <a href="?status=PENDING_PAY" class="btn btn-sm btn-outline-secondary">待支付</a>
        <a href="?status=PENDING_SHIP" class="btn btn-sm btn-outline-secondary">待发货</a>
        <a href="?status=SHIPPING" class="btn btn-sm btn-outline-secondary">配送中</a>
        <a href="?status=COMPLETED" class="btn btn-sm btn-outline-secondary">已完成</a>
        <a href="?status=CANCELLED" class="btn btn-sm btn-outline-secondary">已取消</a>
    </div>

    <div id="orderList">
        <div class="text-center text-muted py-5">加载中...</div>
    </div>
</div>

<jsp:include page="include/footer.jsp"/>

<script>
var ctx = '${pageContext.request.contextPath}';
$(function() {
    var status = '${not empty param.status ? param.status : ""}';
    $.getJSON(ctx + '/orderList.do', {status: status}, function(res) {
        if (res.code !== 200 || !res.data || !res.data.records) {
            $('#orderList').html('<div class="text-center text-muted py-5">暂无订单</div>'); return;
        }
        var records = res.data.records;
        if (records.length === 0) {
            $('#orderList').html('<div class="text-center text-muted py-5">暂无订单，<a href="' + ctx + '/productList.do">去购物</a></div>');
            return;
        }
        var html = '';
        $.each(records, function(i, order) {
            var statusMap = {
                'PENDING_PAY': '<span class="badge bg-warning">待支付</span>',
                'PENDING_SHIP': '<span class="badge bg-info">待发货</span>',
                'SHIPPING': '<span class="badge bg-primary">配送中</span>',
                'COMPLETED': '<span class="badge bg-success">已完成</span>',
                'CANCELLED': '<span class="badge bg-secondary">已取消</span>'
            };
            html += '<div class="card mb-3">'
                + '<div class="card-header d-flex justify-content-between align-items-center">'
                + '<span>订单号：' + order.orderNo + '</span>'
                + statusMap[order.status] || ''
                + '</div>'
                + '<div class="card-body">'
                + '<p class="mb-1">收货人：' + (order.receiverName || '') + '</p>'
                + '<p class="mb-1">支付金额：<strong class="text-danger">¥' + (order.payAmount || 0).toFixed(2) + '</strong></p>'
                + '<p class="text-muted small">下单时间：' + (order.createTime || '') + '</p>'
                + '</div>'
                + '<div class="card-footer text-end">'
                + '<a href="' + ctx + '/orderDetail.do?orderId=' + order.orderId + '" class="btn btn-sm btn-outline-primary">查看详情</a> ';
            if (order.status === 'PENDING_PAY') {
                html += '<button class="btn btn-sm btn-primary" onclick="payOrder(' + order.orderId + ')">去支付</button>'
                    + ' <button class="btn btn-sm btn-outline-danger" onclick="cancelOrder(' + order.orderId + ')">取消</button>';
            }
            if (order.status === 'SHIPPING') {
                html += '<button class="btn btn-sm btn-success" onclick="confirmReceipt(' + order.orderId + ')">确认收货</button>';
            }
            html += '</div></div>';
        });
        $('#orderList').html(html);
    });
});

function payOrder(orderId) {
    $.post(ctx + '/orderPay.do', {orderId: orderId, paymentMethod: 'ALIPAY'}, function(res) {
        alert(res.message);
        location.reload();
    });
}
function cancelOrder(orderId) {
    if (!confirm('确认取消订单？')) return;
    $.post(ctx + '/orderCancel.do', {orderId: orderId}, function(res) {
        alert(res.message);
        location.reload();
    });
}
function confirmReceipt(orderId) {
    if (!confirm('确认已收到货物？')) return;
    $.post(ctx + '/orderConfirm.do', {orderId: orderId}, function(res) {
        alert(res.message);
        location.reload();
    });
}
</script>
</body>
</html>
