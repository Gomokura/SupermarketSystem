package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.*;
import com.supermarket.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // ==================== 用户管理 ====================

    @GetMapping("/users")
    public Result<?> getUserList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        return adminService.getUserList(pageNum, pageSize, keyword);
    }

    @GetMapping("/users/{userId}")
    public Result<?> getUserDetail(@PathVariable Integer userId) {
        return adminService.getUserDetail(userId);
    }

    @PutMapping("/users/{userId}/status")
    public Result<?> updateUserStatus(
            @PathVariable Integer userId,
            @RequestParam String status) {
        return adminService.updateUserStatus(userId, status);
    }

    // ==================== 统计 ====================

    @GetMapping("/statistics")
    public Result<?> getStatistics() {
        return adminService.getStatistics();
    }

    /**
     * 完整统计看板（B 端）
     * GET /admin/dashboard?days=30&topN=10
     */
    @GetMapping("/dashboard")
    public Result<?> getDashboard(
            @RequestParam(defaultValue = "30") Integer days,
            @RequestParam(defaultValue = "10") Integer topN) {
        return adminService.getDashboard(days, topN);
    }

    // ==================== 库存管理 ====================

    @PostMapping("/inventory/warehousing")
    public Result<?> warehousing(
            @RequestParam Integer productId,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String remark,
            @RequestAttribute Integer userId) {
        return adminService.warehousing(productId, quantity, remark, userId);
    }

    @PostMapping("/inventory/outbound")
    public Result<?> outbound(
            @RequestParam Integer productId,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String remark,
            @RequestAttribute Integer userId) {
        return adminService.outbound(productId, quantity, remark, userId);
    }

    @GetMapping("/inventory/logs")
    public Result<?> getInventoryLogs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer productId,
            @RequestParam(required = false) String logType) {
        return adminService.getInventoryLogs(pageNum, pageSize, productId, logType);
    }

    // ==================== 配送管理 ====================

    @GetMapping("/deliveries")
    public Result<?> getDeliveryList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        return adminService.getDeliveryList(pageNum, pageSize, status);
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

    // ==================== 促销管理 ====================

    @GetMapping("/promotions")
    public Result<?> getPromotionList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return adminService.getPromotionList(pageNum, pageSize);
    }

    @PostMapping("/promotions")
    public Result<?> createPromotion(@RequestBody Promotion promotion) {
        return adminService.createPromotion(promotion);
    }

    @PutMapping("/promotions/{promotionId}")
    public Result<?> updatePromotion(
            @PathVariable Integer promotionId,
            @RequestBody Promotion promotion) {
        promotion.setPromotionId(promotionId);
        return adminService.updatePromotion(promotion);
    }

    @DeleteMapping("/promotions/{promotionId}")
    public Result<?> deletePromotion(@PathVariable Integer promotionId) {
        return adminService.deletePromotion(promotionId);
    }

    // ==================== 供应商管理 ====================

    @GetMapping("/suppliers")
    public Result<?> getSupplierList(@RequestParam(required = false) String keyword) {
        return adminService.getSupplierList(keyword);
    }

    @PostMapping("/suppliers")
    public Result<?> createSupplier(@RequestBody Supplier supplier) {
        return adminService.createSupplier(supplier);
    }

    @PutMapping("/suppliers/{supplierId}")
    public Result<?> updateSupplier(
            @PathVariable Integer supplierId,
            @RequestBody Supplier supplier) {
        supplier.setSupplierId(supplierId);
        return adminService.updateSupplier(supplier);
    }

    @DeleteMapping("/suppliers/{supplierId}")
    public Result<?> deleteSupplier(@PathVariable Integer supplierId) {
        return adminService.deleteSupplier(supplierId);
    }

    // ==================== 采购单管理 ====================

    @GetMapping("/purchase-orders")
    public Result<?> getPurchaseOrders(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        return adminService.getPurchaseOrders(pageNum, pageSize, status);
    }

    @PostMapping("/purchase-orders")
    public Result<?> createPurchaseOrder(
            @RequestBody Map<String, Object> body,
            @RequestAttribute Integer userId) {
        // body: { "order": {...}, "items": [...] }
        // 直接用 Jackson 转换
        com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
        PurchaseOrder order = om.convertValue(body.get("order"), PurchaseOrder.class);
        order.setOperatorId(userId);
        List<PurchaseOrderItem> items = om.convertValue(body.get("items"),
            om.getTypeFactory().constructCollectionType(List.class, PurchaseOrderItem.class));
        return adminService.createPurchaseOrder(order, items);
    }

    @PutMapping("/purchase-orders/{poId}/approve")
    public Result<?> approvePurchaseOrder(@PathVariable Integer poId) {
        return adminService.approvePurchaseOrder(poId);
    }

    @PutMapping("/purchase-orders/{poId}/receive")
    public Result<?> receivePurchaseOrder(
            @PathVariable Integer poId,
            @RequestBody List<PurchaseOrderItem> arrivals,
            @RequestAttribute Integer userId) {
        return adminService.receivePurchaseOrder(poId, arrivals, userId);
    }

    // ==================== 财务数据 ====================

    @GetMapping("/finance")
    public Result<?> getFinanceData() {
        return adminService.getFinanceData();
    }

    // ==================== 审计日志 ====================

    @GetMapping("/audit-logs")
    public Result<?> getAuditLogs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String module) {
        return adminService.getAuditLogs(pageNum, pageSize, module);
    }

    // ==================== 骑手管理 ====================

    @GetMapping("/couriers")
    public Result<?> getCourierList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return adminService.getCourierList(pageNum, pageSize);
    }

    @PutMapping("/couriers/{courierId}/status")
    public Result<?> updateCourierStatus(
            @PathVariable Integer courierId,
            @RequestParam Integer isDisabled) {
        return adminService.updateCourierStatus(courierId, isDisabled);
    }
}
