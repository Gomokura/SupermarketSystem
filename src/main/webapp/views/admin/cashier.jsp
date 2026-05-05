<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="adminLayout.jsp"><jsp:param name="title" value="收银台"/></jsp:include>

<div class="mt-4">
    <div class="row">
        <div class="col-md-7">
            <div class="card">
                <div class="card-header">商品扫描</div>
                <div class="card-body">
                    <div class="input-group mb-3">
                        <input type="text" id="barcodeInput" class="form-control" placeholder="输入商品条码或名称" autofocus>
                        <button class="btn btn-primary" onclick="scanProduct()">添加到购物车</button>
                    </div>
                    <table class="table table-hover" id="cashierTable">
                        <thead class="table-light">
                            <tr>
                                <th>商品名称</th>
                                <th>单价</th>
                                <th>数量</th>
                                <th>小计</th>
                                <th><button class="btn btn-sm btn-outline-danger" onclick="clearCashier()">清空</button></th>
                            </tr>
                        </thead>
                        <tbody id="cashierBody">
                            <tr id="cashierEmptyRow"><td colspan="5" class="text-center text-muted py-4">请扫描或输入商品条码</td></tr>
                        </tbody>
                    </table>
                    <div class="input-group mt-3" style="max-width:200px">
                        <label class="input-group-text">数量</label>
                        <input type="number" id="quickQty" class="form-control" value="1" min="1">
                    </div>
                </div>
            </div>
        </div>
        <div class="col-md-5">
            <div class="card">
                <div class="card-header bg-primary text-white">结算</div>
                <div class="card-body">
                    <h4>合计：<span id="cashierTotal" class="text-danger fw-bold">¥0.00</span></h4>
                    <hr>
                    <div class="mb-3">
                        <label class="form-label">会员手机号</label>
                        <div class="input-group">
                            <input type="text" id="memberPhone" class="form-control" placeholder="输入手机号查询会员">
                            <button class="btn btn-outline-secondary" onclick="searchMember()">查询</button>
                        </div>
                        <div id="memberInfo" class="small text-muted mt-1"></div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">支付方式</label>
                        <select id="payMethod" class="form-select">
                            <option value="CASH">现金</option>
                            <option value="WECHAT">微信</option>
                            <option value="ALIPAY">支付宝</option>
                            <option value="CARD">银行卡</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">实收金额</label>
                        <input type="number" id="receivedAmount" class="form-control" placeholder="输入实收金额">
                    </div>
                    <button class="btn btn-success btn-lg w-100 mb-2" onclick="checkout()">结算收款</button>
                    <button class="btn btn-outline-danger w-100" onclick="clearCashier()">取消交易</button>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
var ctx = '${pageContext.request.contextPath}';
var cashierItems = [];
var totalAmount = 0;

function scanProduct() {
    var barcode = $('#barcodeInput').val();
    if (!barcode) return;
    $.getJSON(ctx + '/productBarcode.do', {barcode: barcode}, function(res) {
        if (res.code === 200) {
            var p = res.data;
            var qty = parseInt($('#quickQty').val()) || 1;
            addCashierItem(p, qty);
            $('#barcodeInput').val('').focus();
        } else {
            alert('商品未找到：' + barcode);
        }
    });
}

function addCashierItem(product, qty) {
    var subtotal = (product.price || 0) * qty;
    cashierItems.push({productId: product.productId, productName: product.productName, price: product.price, quantity: qty, subtotal: subtotal});
    renderCashierTable();
    updateTotal();
}

function renderCashierTable() {
    if (cashierItems.length === 0) {
        $('#cashierBody').html('<tr id="cashierEmptyRow"><td colspan="5" class="text-center text-muted py-4">请扫描或输入商品条码</td></tr>');
        return;
    }
    var html = '';
    $.each(cashierItems, function(i, item) {
        html += '<tr>'
            + '<td>' + item.productName + '</td>'
            + '<td>¥' + (item.price || 0).toFixed(2) + '</td>'
            + '<td>' + item.quantity + '</td>'
            + '<td class="fw-bold text-danger">¥' + (item.subtotal || 0).toFixed(2) + '</td>'
            + '<td><button class="btn btn-sm btn-outline-danger" onclick="removeItem(' + i + ')">×</button></td></tr>';
    });
    $('#cashierBody').html(html);
}

function removeItem(index) { cashierItems.splice(index, 1); renderCashierTable(); updateTotal(); }
function clearCashier() { cashierItems = []; renderCashierTable(); updateTotal(); }

function updateTotal() {
    totalAmount = cashierItems.reduce(function(s, item) { return s + (item.subtotal || 0); }, 0);
    $('#cashierTotal').text('¥' + totalAmount.toFixed(2));
}

function searchMember() {
    var phone = $('#memberPhone').val();
    if (!phone) return;
    $.getJSON(ctx + '/memberSearch.do', {phone: phone}, function(res) {
        if (res.code === 200) {
            var m = res.data;
            $('#memberInfo').text('会员：' + m.nickname + ' | 等级：' + m.memberLevel + ' | 积分：' + m.points);
        } else {
            $('#memberInfo').text('非会员或未找到').addClass('text-danger');
        }
    });
}

function checkout() {
    if (cashierItems.length === 0) { alert('请先添加商品'); return; }
    var received = parseFloat($('#receivedAmount').val()) || 0;
    if (received < totalAmount) { alert('实收金额不能少于商品总额'); return; }
    var items = cashierItems.map(function(i) { return {productId: i.productId, quantity: i.quantity}; });
    $.post(ctx + '/cashierOrder.do', {
        method: 'order',
        cartItems: JSON.stringify(items),
        payMethod: $('#payMethod').val(),
        receivedAmount: received
    }, function(res) {
        if (res.code === 200) {
            var change = (received - totalAmount).toFixed(2);
            alert('交易成功！\n订单号：' + res.data.orderNo + '\n找零：¥' + change);
            clearCashier();
        } else {
            alert('结算失败：' + res.message);
        }
    });
}

$('#barcodeInput').keypress(function(e) {
    if (e.which === 13) { e.preventDefault(); scanProduct(); }
});
</script>
</body>
</html>
