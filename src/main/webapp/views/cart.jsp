<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="include/head.jsp"><jsp:param name="title" value="购物车"/></jsp:include>
<body>
<jsp:include page="include/header.jsp"/>

<div class="container mt-4">
    <h2>我的购物车</h2>
    <div class="table-responsive mt-3" id="cartContainer">
        <table class="table table-hover align-middle">
            <thead class="table-light">
                <tr>
                    <th><input type="checkbox" id="checkAll"></th>
                    <th>商品</th>
                    <th>规格</th>
                    <th>单价</th>
                    <th>数量</th>
                    <th>小计</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody id="cartBody">
                <tr><td colspan="7" class="text-center text-muted">购物车是空的</td></tr>
            </tbody>
        </table>
    </div>

    <div class="d-flex justify-content-between align-items-center border-top pt-3">
        <div>
            <button class="btn btn-outline-danger btn-sm" onclick="batchDelete()">批量删除</button>
        </div>
        <div>
            <p class="mb-1">已选 <span id="totalCount" class="fw-bold">0</span> 件商品</p>
            <p class="mb-1">合计：<span id="totalAmount" class="fw-bold text-danger fs-5">¥0.00</span></p>
            <button class="btn btn-primary btn-lg" onclick="checkout()">去结算</button>
        </div>
    </div>
</div>

<jsp:include page="include/footer.jsp"/>

<script>
var ctx = '${pageContext.request.contextPath}';
var selectedIds = [];

$(function() {
    loadCart();

    $('#checkAll').change(function() {
        var checked = $(this).prop('checked');
        $('#cartBody input[type=checkbox]').prop('checked', checked);
        if (checked) {
            selectedIds = $('#cartBody input[type=checkbox]:checked').map(function() { return $(this).val(); }).get();
        } else {
            selectedIds = [];
        }
        updateSummary();
    });
});

function loadCart() {
    $.getJSON(ctx + '/cartList.do', function(res) {
        if (res.code !== 200 || !res.data || res.data.length === 0) {
            $('#cartBody').html('<tr><td colspan="7" class="text-center text-muted py-5">购物车是空的，<a href="' + ctx + '/productList.do">去逛逛</a></td></tr>');
            return;
        }
        var html = '';
        $.each(res.data, function(i, item) {
            html += '<tr>'
                + '<td><input type="checkbox" class="item-check" value="' + item.cartId + '" data-price="' + (item.price || 0) + '" data-quantity="' + item.quantity + '"></td>'
                + '<td><div class="d-flex align-items-center"><img src="' + (item.imageUrl || '/images/default.png') + '" style="width:60px;height:60px;object-fit:cover" class="me-2 rounded">'
                + '<a href="' + ctx + '/productDetail.do?productId=' + item.productId + '">' + item.productName + '</a></div></td>'
                + '<td>' + (item.specName || '-') + '</td>'
                + '<td class="price-cell">¥' + (item.price || 0).toFixed(2) + '</td>'
                + '<td><input type="number" class="form-control form-control-sm quantity-input" value="' + item.quantity + '" min="1" max="999" style="width:70px" data-cartId="' + item.cartId + '"></td>'
                + '<td class="subtotal-cell fw-bold text-danger">¥' + ((item.price || 0) * item.quantity).toFixed(2) + '</td>'
                + '<td><button class="btn btn-sm btn-outline-danger" onclick="removeItem(' + item.cartId + ')">删除</button></td>'
                + '</tr>';
        });
        $('#cartBody').html(html);

        $('.item-check').change(function() {
            selectedIds = $('#cartBody input.item-check:checked').map(function() { return $(this).val(); }).get();
            updateSummary();
        });

        $('.quantity-input').change(function() {
            var cartId = $(this).data('cartid');
            var qty = $(this).val();
            $.post(ctx + '/cartUpdate.do', {cartId: cartId, quantity: qty}, function() { loadCart(); });
        });
    });
}

function removeItem(cartId) {
    if (!confirm('确认删除？')) return;
    $.post(ctx + '/cartRemove.do', {cartId: cartId}, function() { loadCart(); });
}

function batchDelete() {
    if (selectedIds.length === 0) { alert('请先选择商品'); return; }
    if (!confirm('确认删除选中的 ' + selectedIds.length + ' 件商品？')) return;
    $.post(ctx + '/cartBatchDelete.do', {cartIds: selectedIds.join(',')}, function() { loadCart(); });
}

function updateSummary() {
    var totalCount = 0, totalAmount = 0;
    $('#cartBody input.item-check:checked').each(function() {
        totalCount += parseInt($(this).data('quantity'));
        totalAmount += parseFloat($(this).data('price')) * parseInt($(this).data('quantity'));
    });
    $('#totalCount').text(totalCount);
    $('#totalAmount').text('¥' + totalAmount.toFixed(2));
}

function checkout() {
    if (selectedIds.length === 0) { alert('请先选择商品'); return; }
    location.href = ctx + '/views/orderConfirm.jsp?cartIds=' + selectedIds.join(',');
}
</script>
</body>
</html>
