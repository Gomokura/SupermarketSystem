package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.*;
import com.supermarket.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/users")
    public Result<?> getUserList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        return adminService.getUserList(pageNum, pageSize, keyword);
    }

    @PutMapping("/users/{userId}/status")
    public Result<?> updateUserStatus(
            @PathVariable Integer userId,
            @RequestParam String status) {
        return adminService.updateUserStatus(userId, status);
    }

    @GetMapping("/statistics")
    public Result<?> getStatistics() {
        return adminService.getStatistics();
    }

    @PostMapping("/inventory/warehousing")
    public Result<?> warehousing(
            @RequestParam Integer productId,
            @RequestParam Integer quantity,
            @RequestAttribute Integer userId) {
        return adminService.warehousing(productId, quantity, userId);
    }

    @PostMapping("/inventory/outbound")
    public Result<?> outbound(
            @RequestParam Integer productId,
            @RequestParam Integer quantity,
            @RequestAttribute Integer userId) {
        return adminService.outbound(productId, quantity, userId);
    }

    @GetMapping("/inventory/logs")
    public Result<?> getInventoryLogs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return adminService.getInventoryLogs(pageNum, pageSize);
    }

    @GetMapping("/deliveries")
    public Result<?> getDeliveryList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return adminService.getDeliveryList(pageNum, pageSize);
    }

    @PutMapping("/deliveries/{deliveryId}/assign")
    public Result<?> assignCourier(
            @PathVariable Integer deliveryId,
            @RequestParam Integer courierId) {
        return adminService.assignCourier(deliveryId, courierId);
    }

    @PutMapping("/deliveries/{deliveryId}/status")
    public Result<?> updateDeliveryStatus(
            @PathVariable Integer deliveryId,
            @RequestParam String status) {
        return adminService.updateDeliveryStatus(deliveryId, status);
    }

    @GetMapping("/promotions")
    public Result<?> getPromotionList() {
        return adminService.getPromotionList();
    }

    @PostMapping("/promotions")
    public Result<?> createPromotion(@RequestBody Promotion promotion) {
        return adminService.createPromotion(promotion);
    }

    @PutMapping("/promotions")
    public Result<?> updatePromotion(@RequestBody Promotion promotion) {
        return adminService.updatePromotion(promotion);
    }

    @DeleteMapping("/promotions/{promotionId}")
    public Result<?> deletePromotion(@PathVariable Integer promotionId) {
        return adminService.deletePromotion(promotionId);
    }

    @GetMapping("/suppliers")
    public Result<?> getSupplierList() {
        return adminService.getSupplierList();
    }

    @GetMapping("/purchase-orders")
    public Result<?> getPurchaseOrders(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return adminService.getPurchaseOrders(pageNum, pageSize);
    }

    @PostMapping("/purchase-orders")
    public Result<?> createPurchaseOrder(@RequestBody PurchaseOrder order) {
        return adminService.createPurchaseOrder(order);
    }

    @GetMapping("/finance")
    public Result<?> getFinanceData() {
        return adminService.getFinanceData();
    }

    @GetMapping("/audit-logs")
    public Result<?> getAuditLogs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return adminService.getAuditLogs(pageNum, pageSize);
    }
}
