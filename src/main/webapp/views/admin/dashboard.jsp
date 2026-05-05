<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="adminLayout.jsp"><jsp:param name="title" value="仪表盘"/></jsp:include>

<jsp:include page="adminLayout.jsp"><jsp:param name="title" value="仪表盘"/></jsp:include>
<div class="mt-4">
    <div class="row">
        <div class="col-md-3">
            <div class="card text-center border-primary">
                <div class="card-body">
                    <h3 id="todayOrders" class="text-primary">-</h3>
                    <p class="mb-0">今日订单</p>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card text-center border-success">
                <div class="card-body">
                    <h3 id="todaySales" class="text-success">-</h3>
                    <p class="mb-0">今日销售额</p>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card text-center border-warning">
                <div class="card-body">
                    <h3 id="pendingShipCount" class="text-warning">-</h3>
                    <p class="mb-0">待发货订单</p>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card text-center border-danger">
                <div class="card-body">
                    <h3 id="lowStockCount" class="text-danger">-</h3>
                    <p class="mb-0">低库存商品</p>
                </div>
            </div>
        </div>
    </div>

    <div class="row mt-4">
        <div class="col-md-12">
            <div class="card">
                <div class="card-header">近7天销售趋势</div>
                <div class="card-body">
                    <canvas id="salesChart" height="80"></canvas>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
<script>
var ctx = '${pageContext.request.contextPath}';
$.getJSON(ctx + '/adminDashboard.do', function(res) {
    if (res.code === 200) {
        var d = res.data;
        $('#todayOrders').text(d.todayOrders || 0);
        $('#todaySales').text('¥' + (d.todaySales || 0).toFixed(2));
        $('#pendingShipCount').text(d.pendingShipCount || 0);
        $('#lowStockCount').text(d.lowStockCount || 0);

        var labels = [], sales = [];
        if (d.salesTrend) {
            $.each(d.salesTrend, function(i, item) {
                labels.push(item.date);
                sales.push(item.sales);
            });
        }
        new Chart(document.getElementById('salesChart'), {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{label: '销售额', data: sales, borderColor: '#0d6efd', tension: 0.3, fill: true, backgroundColor: 'rgba(13,110,253,0.1)'}]
            },
            options: { responsive: true }
        });
    }
});
</script>
</body>
</html>
