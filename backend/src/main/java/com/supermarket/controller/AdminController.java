package com.supermarket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== 管理员管理 ====================

    @GetMapping("/admins")
    public Result<?> getAdmins(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        return adminService.getAdminList(pageNum, pageSize, keyword);
    }

    @PostMapping("/admins")
    public Result<?> createAdmin(
            @RequestAttribute Integer adminId,
            @RequestBody Admin admin) {
        // 权限细分（SUPER_ADMIN 才能创建）在拦截器未做按钮级，这里先按最小可用实现
        return adminService.createAdmin(admin);
    }

    @PutMapping("/admins/{targetAdminId}")
    public Result<?> updateAdmin(
            @RequestAttribute Integer adminId,
            @PathVariable Integer targetAdminId,
            @RequestBody Admin patch) {
        return adminService.updateAdmin(targetAdminId, patch);
    }

    @PutMapping("/admins/{targetAdminId}/reset-password")
    public Result<?> resetPassword(
            @RequestAttribute Integer adminId,
            @PathVariable Integer targetAdminId,
            @RequestParam String newPassword) {
        return adminService.resetAdminPassword(targetAdminId, newPassword);
    }

    // ==================== 用户管理 ====================

    @GetMapping("/users")
    public Result<?> getUserList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return adminService.getUserList(pageNum, pageSize, keyword, status);
    }

    @GetMapping("/users/{userId}")
    public Result<?> getUserDetail(@PathVariable Integer userId) {
        return adminService.getUserDetail(userId);
    }

    @PutMapping("/users/{userId}/status")
    public Result<?> updateUserStatus(
            @PathVariable Integer userId,
            @RequestParam String status,
            @RequestParam(required = false) String reason) {
        return adminService.updateUserStatus(userId, status, reason);
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
        promotion.setActivityId(promotionId);
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

    @GetMapping("/purchase-orders/{poId}")
    public Result<?> getPurchaseOrderDetail(@PathVariable Integer poId) {
        return adminService.getPurchaseOrderDetail(poId);
    }

    @PostMapping("/purchase-orders")
    public Result<?> createPurchaseOrder(
            @RequestBody Map<String, Object> body,
            @RequestAttribute Integer userId) {
        // body: { "order": {...}, "items": [...] }
        // 直接用 Jackson 转换
        PurchaseOrder order = objectMapper.convertValue(body.get("order"), PurchaseOrder.class);
        order.setOperatorId(userId);
        List<PurchaseOrderItem> items = objectMapper.convertValue(body.get("items"),
            objectMapper.getTypeFactory().constructCollectionType(List.class, PurchaseOrderItem.class));
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

    @PutMapping("/purchase-orders/{poId}/cancel")
    public Result<?> cancelPurchaseOrder(@PathVariable Integer poId) {
        return adminService.cancelPurchaseOrder(poId);
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
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return adminService.getAuditLogs(pageNum, pageSize, module, startDate, endDate);
    }

    // ==================== 骑手管理 ====================

    @GetMapping("/couriers")
    public Result<?> getCourierList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return adminService.getCourierList(pageNum, pageSize);
    }

    @PostMapping("/couriers")
    public Result<?> createCourier(@RequestBody Courier courier) {
        return adminService.createCourier(courier);
    }

    @PutMapping("/couriers/{courierId}/status")
    public Result<?> updateCourierStatus(
            @PathVariable Integer courierId,
            @RequestParam Integer isDisabled) {
        return adminService.updateCourierStatus(courierId, isDisabled);
    }

    // ==================== 站内信 ====================

    @PostMapping("/messages")
    public Result<?> sendMessage(
            @RequestAttribute Integer adminId,
            @RequestBody Map<String, Object> body) {
        Integer userId = body.get("userId") != null ? ((Number) body.get("userId")).intValue() : null;
        String title = (String) body.get("title");
        String content = (String) body.get("content");
        String msgType = (String) body.get("msgType");
        Integer refId = body.get("refId") != null ? ((Number) body.get("refId")).intValue() : null;
        return adminService.sendMessageToUser(adminId, userId, title, content, msgType, refId);
    }
}
