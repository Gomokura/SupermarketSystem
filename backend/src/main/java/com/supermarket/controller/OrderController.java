package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.dto.CreateOrderRequest;
import com.supermarket.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // ==================== C�?====================

    /**
     * 用户订单列表
     * GET /orders/list?status=&pageNum=&pageSize=
     */
    @GetMapping("/list")
    public Result<?> getUserOrders(
            @RequestAttribute Integer userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return orderService.getUserOrders(userId, status, pageNum, pageSize);
    }

    /**
     * 订单详情
     * GET /orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public Result<?> getOrderDetail(
            @PathVariable Integer orderId,
            @RequestAttribute Integer userId) {
        return orderService.getOrderDetail(orderId, userId);
    }

    /**
     * 提交订单
     * POST /orders/create
     * body: { addressId, paymentMethod, couponId, pointsUsed, remark, items: [{productId, quantity, skuId, specName}] }
     */
    @PostMapping("/create")
    public Result<?> createOrder(
            @RequestAttribute Integer userId,
            @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(
                userId,
                request.getAddressId(),
                request.getPaymentMethod(),
                request.getItems(),
                selectedCoupon(request),
                request.getPointsUsed(),
                request.getRemark(),
                request.getDeliveryTimeSlot()
        );
    }

    /** 结算预览（不创建订单�?*/
    @PostMapping("/preview")
    public Result<?> previewOrder(
            @RequestAttribute Integer userId,
            @RequestBody CreateOrderRequest request) {
        return orderService.previewOrder(
                userId,
                request.getAddressId(),
                request.getItems(),
                selectedCoupon(request),
                request.getPointsUsed()
        );
    }

    /**
     * 支付订单
     * POST /orders/{orderId}/pay
     * body: { "payMethod": "wechat" }
     */
    @PostMapping("/{orderId}/pay")
    public Result<?> payOrder(
            @PathVariable Integer orderId,
            @RequestAttribute Integer userId,
            @RequestBody Map<String, String> body) {
        return orderService.payOrder(orderId, userId, body.get("payMethod"));
    }

    /**
     * 取消订单
     * PUT /orders/{orderId}/cancel
     */
    @PutMapping("/{orderId}/cancel")
    public Result<?> cancelOrder(
            @PathVariable Integer orderId,
            @RequestAttribute Integer userId) {
        return orderService.cancelOrder(orderId, userId);
    }

    /**
     * 确认收货
     * PUT /orders/{orderId}/confirm
     */
    @PutMapping("/{orderId}/confirm")
    public Result<?> confirmReceipt(
            @PathVariable Integer orderId,
            @RequestAttribute Integer userId) {
        return orderService.confirmReceipt(orderId, userId);
    }

    /** 订单时间�?*/
    @GetMapping("/{orderId}/timeline")
    public Result<?> getOrderTimeline(
            @PathVariable Integer orderId,
            @RequestAttribute Integer userId) {
        return orderService.getOrderStatusLogs(orderId, userId, false);
    }

    // ==================== B端管�?====================

    /**
     * 管理后台订单列表
     * GET /orders/admin/list?status=&orderNo=&userId=&pageNum=&pageSize=
     */
    @GetMapping("/admin/list")
    public Result<?> adminGetOrderList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return orderService.adminGetOrderList(status, orderNo, userId, startDate, endDate, pageNum, pageSize);
    }

    /** 管理后台查看订单时间�?*/
    @GetMapping("/admin/{orderId}/timeline")
    public Result<?> adminGetOrderTimeline(
            @PathVariable Integer orderId,
            @RequestAttribute Integer adminId) {
        return orderService.getOrderStatusLogs(orderId, adminId, true);
    }

    /**
     * 管理后台订单详情（管理员可查看任意订单）
     * GET /orders/admin/{orderId}
     */
    @GetMapping("/admin/{orderId}")
    public Result<?> adminGetOrderDetail(
            @PathVariable Integer orderId) {
        return orderService.adminGetOrderDetail(orderId);
    }

    /**
     * 管理员发货（带快递信息）
     * PUT /orders/{orderId}/ship
     * body: { "company": "顺丰", "trackingNo": "SF123" }
     */
    @PutMapping("/{orderId}/ship")
    public Result<?> shipOrder(
            @PathVariable Integer orderId,
            @RequestAttribute Integer adminId,
            @RequestBody Map<String, String> body) {
        return orderService.shipOrder(orderId, adminId, body.get("company"), body.get("trackingNo"));
    }

    /** 管理员按订单分配配送员 */
    @PutMapping("/{orderId}/assign-courier")
    public Result<?> assignCourier(
            @PathVariable Integer orderId,
            @RequestParam Integer courierId,
            @RequestAttribute Integer adminId) {
        return orderService.assignCourier(orderId, courierId, adminId);
    }

    /**
     * 管理员取消订�?
     * PUT /orders/{orderId}/admin-cancel
     * body: { "reason": "..." }
     */
    @PutMapping("/{orderId}/admin-cancel")
    public Result<?> adminCancelOrder(
            @PathVariable Integer orderId,
            @RequestAttribute Integer userId,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return orderService.adminCancelOrder(orderId, userId, reason);
    }

    /**
     * 管理员修改收货地址
     * PUT /orders/{orderId}/address
     * body: { "name": "...", "phone": "...", "address": "..." }
     */
    @PutMapping("/{orderId}/address")
    public Result<?> updateOrderAddress(
            @PathVariable Integer orderId,
            @RequestAttribute Integer adminId,
            @RequestBody Map<String, String> body) {
        return orderService.updateOrderAddress(orderId, adminId,
                body.get("name"), body.get("phone"), body.get("address"));
    }

    // ==================== 收银台端 ====================
    /** C-46 再次购买：将历史订单商品加入购物�?*/
    @PostMapping("/{orderId}/reorder")
    public Result<?> reorder(
            @PathVariable Integer orderId,
            @RequestAttribute Integer userId) {
        return orderService.reorder(orderId, userId);
    }
    /**
     * 收银台快速下单（扫码/手动录入�?
     * POST /orders/cashier
     * body: { payMethod, receivedAmount, items: [{productId, quantity}] }
     */
    @PostMapping("/cashier")
    public Result<?> cashierCreateOrder(
            @RequestAttribute Integer userId,
            @RequestBody Map<String, Object> body) {
        String payMethod = (String) body.get("payMethod");
        Double receivedAmount = body.get("receivedAmount") != null
                ? ((Number) body.get("receivedAmount")).doubleValue() : null;
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawItems = (List<Map<String, Object>>) body.get("items");
        if (rawItems == null || rawItems.isEmpty()) return Result.error("items不能为空");

        List<CreateOrderRequest.CartItem> items = new java.util.ArrayList<>();
        for (Map<String, Object> raw : rawItems) {
            CreateOrderRequest.CartItem item = new CreateOrderRequest.CartItem();
            item.setProductId(toInteger(raw.get("productId")));
            item.setQuantity(toStrictInteger(raw.get("quantity")));
            items.add(item);
        }

        return orderService.cashierCreateOrder(userId, userId, items, payMethod, receivedAmount);
    }
    private Integer toInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Integer i) return i;
        if (value instanceof Number n) return n.intValue();
        return Integer.valueOf(value.toString());
    }

    private Integer selectedCoupon(CreateOrderRequest request) {
        if (request == null) return null;
        return request.getUserCouponId() != null ? request.getUserCouponId() : request.getCouponId();
    }

    private Integer toStrictInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Integer i) return i;
        if (value instanceof Number n) {
            double d = n.doubleValue();
            if (d % 1 != 0) throw new IllegalArgumentException("quantity must be integer");
            return n.intValue();
        }
        String text = value.toString();
        if (!text.matches("\\d+")) throw new IllegalArgumentException("quantity must be integer");
        return Integer.valueOf(text);
    }
}
