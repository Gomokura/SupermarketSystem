<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="adminLayout.jsp"><jsp:param name="title" value="订单管理"/></jsp:include>

<div class="mt-4">
    <div class="card">
        <div class="card-header">订单管理</div>
        <div class="card-body">
            <div class="row mb-3">
                <div class="col-md-3">
                    <input type="text" id="searchOrderNo" class="form-control" placeholder="订单号">
                </div>
                <div class="col-md-2">
                    <select id="searchStatus" class="form-select">
                        <option value="">全部状态</option>
                        <option value="PENDING_PAY">待支付</option>
                        <option value="PENDING_SHIP">待发货</option>
                        <option value="SHIPPING">配送中</option>
                        <option value="COMPLETED">已完成</option>
                        <option value="CANCELLED">已取消</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <button class="btn btn-primary" onclick="loadOrders()">搜索</button>
                </div>
            </div>
            <table class="table table-bordered table-hover">
                <thead class="table-light">
                    <tr>
                        <th>订单号</th>
                        <th>用户</th>
                        <th>金额</th>
                        <th>支付方式</th>
                        <th>订单状态</th>
                        <th>下单时间</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody id="orderTableBody">
                    <tr><td colspan="7" class="text-center py-4">加载中...</td></tr>
                </tbody>
            </table>
        </div>
    </div>
</div>

<script>
var ctx = '${pageContext.request.contextPath}';
function loadOrders(pageNum) {
    var status = $('#searchStatus').val();
    var orderNo = $('#searchOrderNo').val();
    $.getJSON(ctx + '/adminOrderList.do', {status: status, orderNo: orderNo, pageNum: pageNum || 1}, function(res) {
        if (res.code !== 200 || !res.data || !res.data.records) {
            $('#orderTableBody').html('<tr><td colspan="7" class="text-center text-muted py-4">暂无订单</td></tr>'); return;
        }
        var html = '';
        var statusMap = {
            'PENDING_PAY': '<span class="badge bg-warning">待支付</span>',
            'PENDING_SHIP': '<span class="badge bg-info">待发货</span>',
            'SHIPPING': '<span class="badge bg-primary">配送中</span>',
            'COMPLETED': '<span class="badge bg-success">已完成</span>',
            'CANCELLED': '<span class="badge bg-secondary">已取消</span>'
        };
        $.each(res.data.records, function(i, o) {
            html += '<tr>'
                + '<td>' + o.orderNo + '</td>'
                + '<td>' + (o.username || o.userId) + '</td>'
                + '<td class="text-danger fw-bold">¥' + (o.payAmount || 0).toFixed(2) + '</td>'
                + '<td>' + (o.payMethod || '-') + '</td>'
                + '<td>' + (statusMap[o.status] || o.status) + '</td>'
                + '<td>' + (o.createTime || '') + '</td>'
                + '<td>';
            if (o.status === 'PENDING_SHIP') {
                html += '<button class="btn btn-sm btn-success" onclick="shipOrder(' + o.orderId + ')">发货</button> ';
            }
            if (o.status !== 'COMPLETED' && o.status !== 'CANCELLED') {
                html += '<button class="btn btn-sm btn-outline-danger" onclick="cancelOrder(' + o.orderId + ')">取消</button> ';
            }
            html += '<button class="btn btn-sm btn-outline-primary" onclick="viewOrder(' + o.orderId + ')">详情</button>'
                + '</td></tr>';
        });
        $('#orderTableBody').html(html);
    });
}
function shipOrder(id) { $.post(ctx + '/adminOrderShip.do', {orderId: id, method: 'ship'}, function() { loadOrders(); }); }
function cancelOrder(id) { if (!confirm('确认取消订单？')) return; $.post(ctx + '/adminOrderCancel.do', {orderId: id, method: 'cancel'}, function() { loadOrders(); }); }
function viewOrder(id) { location.href = ctx + '/views/admin/orderDetail.jsp?orderId=' + id; }
$(function() { loadOrders(); });
</script>
</body>
</html>
