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

    // ==================== C绔?=============    }

    /**
     * 绠＄悊鍛樺彇娑堣鍗?
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

    // ==================== 鏀堕摱鍙扮 ====================

    /**
     * 鏀堕摱鍙板揩閫熶笅鍗曪紙鎵爜/鎵嬪姩褰曞叆锛?
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

        List<CreateOrderRequest.CartItem> items = new java.util.ArrayList<>();
        for (Map<String, Object> raw : rawItems) {
            CreateOrderRequest.CartItem item = new CreateOrderRequest.CartItem();
            item.setProductId((Integer) raw.get("productId"));
            item.setQuantity((Integer) raw.get("quantity"));
            if (raw.get("skuId") != null) item.setSkuId((Integer) raw.get("skuId"));
            items.add(item);
        }

        return orderService.cashierCreateOrder(userId, items, payMethod, receivedAmount);
    }

    // ==================== 閰嶉€佷笌杞ㄨ抗 ====================

    /**
     * 璁㈠崟閰嶉€佽建杩?
     * GET /orders/{orderId}/delivery-trace
     */
    @GetMapping("/{orderId}/delivery-trace")
    public Result<?> getDeliveryTrace(
            @PathVariable Integer orderId,
            @RequestAttribute(required = false) Integer userId) {
        return orderService.getDeliveryTrace(orderId, userId);
    }

    /**
     * 璁㈠崟鏃堕棿绾匡紙璁㈠崟鍏ㄧ敓鍛藉懆鏈熻妭鐐癸級
     * GET /orders/{orderId}/timeline
     */
    @GetMapping("/{orderId}/timeline")
    public Result<?> getOrderTimeline(
            @PathVariable Integer orderId,
            @RequestAttribute(required = false) Integer userId) {
        return orderService.getOrderTimeline(orderId, userId);
    }

    /**
     * 绠＄悊鍛樻寚娲鹃厤閫佸憳
     * PUT /orders/{orderId}/assign-courier
     * body: { courierId }
     */
    @PutMapping("/{orderId}/assign-courier")
    public Result<?> assignCourier(
            @PathVariable Integer orderId,
            @RequestBody Map<String, Object> body,
            @RequestAttribute Integer userId) {
        Integer courierId = (Integer) body.get("courierId");
        return orderService.assignCourier(orderId, courierId, userId);
    }
}
