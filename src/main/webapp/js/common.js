/**
 * SupermarketSystem - 公共 JavaScript 工具函数
 */

var Supermarket = window.Supermarket || {};

Supermarket.config = {
    ctx: '',
    apiTimeout: 30000
};

/**
 * 初始化上下文路径
 */
Supermarket.init = function(ctx) {
    Supermarket.config.ctx = ctx || '';
};

/**
 * GET 请求
 */
Supermarket.get = function(url, params, success, error) {
    $.ajax({
        url: Supermarket.config.ctx + url,
        type: 'GET',
        data: params,
        dataType: 'json',
        timeout: Supermarket.config.timeout,
        success: function(res) {
            if (res.code === 401) {
                if (confirm('登录已过期，是否重新登录？')) {
                    location.href = Supermarket.config.ctx + '/views/login.jsp';
                }
                return;
            }
            if (success) success(res);
        },
        error: function(xhr) {
            if (error) error(xhr);
            else console.error('请求失败:', xhr);
        }
    });
};

/**
 * POST 请求
 */
Supermarket.post = function(url, params, success, error) {
    $.ajax({
        url: Supermarket.config.ctx + url,
        type: 'POST',
        data: params,
        dataType: 'json',
        timeout: Supermarket.config.timeout,
        success: function(res) {
            if (res.code === 401) {
                if (confirm('登录已过期，是否重新登录？')) {
                    location.href = Supermarket.config.ctx + '/views/login.jsp';
                }
                return;
            }
            if (success) success(res);
        },
        error: function(xhr) {
            if (error) error(xhr);
            else console.error('请求失败:', xhr);
        }
    });
};

/**
 * 格式化金额
 */
Supermarket.formatPrice = function(price) {
    if (price == null) return '¥0.00';
    return '¥' + parseFloat(price).toFixed(2);
};

/**
 * 显示提示消息
 */
Supermarket.showMessage = function(msg, type) {
    type = type || 'info';
    var html = '<div class="alert alert-' + type + ' alert-dismissible fade show" role="alert" style="position:fixed;top:20px;right:20px;z-index:9999;min-width:250px">'
        + msg
        + '<button type="button" class="btn-close" data-bs-dismiss="alert"></button>'
        + '</div>';
    $('body').append(html);
    setTimeout(function() { $('.alert').alert('close'); }, 3000);
};

/**
 * 确认对话框
 */
Supermarket.confirm = function(message, onConfirm) {
    if (confirm(message)) {
        if (onConfirm) onConfirm();
    }
};

/**
 * 分页加载（通用）
 */
Supermarket.pagination = function(current, total, onPage) {
    if (total <= 1) return '';
    var html = '<nav><ul class="pagination mb-0">';
    for (var i = 1; i <= total; i++) {
        html += '<li class="page-item' + (i === current ? ' active' : '') + '">'
            + '<a class="page-link" href="javascript:void(0)" onclick="return false">' + i + '</a>'
            + '</li>';
    }
    html += '</ul></nav>';
    return html;
};

// 页面加载时自动初始化
$(function() {
    Supermarket.init('');
});
