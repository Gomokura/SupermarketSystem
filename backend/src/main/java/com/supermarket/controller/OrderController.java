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

    // ==================== C端 ====================

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
                request.getCouponId(),
                request.getPointsUsed(),
                request.getRemark(),
                request.getDeliveryTimeSlot()
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

    // ==================== B端管理 ====================

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

    /**
     * 管理员发货（带快递信息）
     * PUT /orders/{orderId}/ship
     * body: { "company": "顺丰", "trackingNo": "SF123" }
     */
    @PutMapping("/{orderId}/ship")
    public Result<?> shipOrder(
            @PathVariable Integer orderId,
            @RequestBody Map<String, String> body) {
        return orderService.shipOrder(orderId, body.get("company"), body.get("trackingNo"));
    }

    /**
     * 管理员取消订单
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

    // ==================== 收银台端 ====================
    /** C-46 再次购买：将历史订单商品加入购物车 */
    @PostMapping("/{orderId}/reorder")
    public Result<?> reorder(
            @PathVariable Integer orderId,
            @RequestAttribute Integer userId) {
        return orderService.reorder(orderId, userId);
    }
    /**
     * 收银台快速下单（扫码/手动录入）
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
            item.setProductId((Integer) raw.get("productId"));
            item.setQuantity((Integer) raw.get("quantity"));
            items.add(item);
        }

        return orderService.cashierCreateOrder(userId, items, payMethod, receivedAmount);
    }
}
