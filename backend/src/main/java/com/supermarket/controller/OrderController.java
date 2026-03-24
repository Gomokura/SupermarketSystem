package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.dto.CreateOrderRequest;
import com.supermarket.entity.Order;
import com.supermarket.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/list")
    public Result<?> getUserOrders(
            @RequestAttribute Integer userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return orderService.getUserOrders(userId, pageNum, pageSize);
    }

    @GetMapping("/{orderId}")
    public Result<?> getOrderDetail(@PathVariable Integer orderId) {
        return orderService.getOrderDetail(orderId);
    }

    @PostMapping("/create")
    public Result<?> createOrder(
            @RequestAttribute Integer userId,
            @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(userId, request.getAddressId(), 
                request.getPaymentMethod(), request.getItems());
    }

    @PutMapping("/cancel/{orderId}")
    public Result<?> cancelOrder(
            @PathVariable Integer orderId,
            @RequestAttribute Integer userId) {
        return orderService.cancelOrder(orderId, userId);
    }

    @PutMapping("/confirm/{orderId}")
    public Result<?> confirmReceipt(
            @PathVariable Integer orderId,
            @RequestAttribute Integer userId) {
        return orderService.confirmReceipt(orderId, userId);
    }
}
