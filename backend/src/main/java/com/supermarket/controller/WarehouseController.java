package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/warehouse")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    // ==================== 采购单管理 ====================

    /**
     * 采购单列表
     * GET /warehouse/purchase-orders?status=&pageNum=&pageSize=
     */
    @GetMapping("/purchase-orders")
    public Result<?> getPurchaseOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return warehouseService.getPurchaseOrders(status, pageNum, pageSize);
    }

    /**
     * 采购单详情
     * GET /warehouse/purchase-orders/{poId}
     */
    @GetMapping("/purchase-orders/{poId}")
    public Result<?> getPurchaseOrderDetail(@PathVariable Integer poId) {
        return warehouseService.getPurchaseOrderDetail(poId);
    }

    /**
     * 创建采购单
     * POST /warehouse/purchase-orders
     * body: { supplierId, expectedDate, remark, items: [{productId, quantity, unitPrice}] }
     */
    @PostMapping("/purchase-orders")
    public Result<?> createPurchaseOrder(
            @RequestBody Map<String, Object> body,
            @RequestAttribute Integer userId) {
        return warehouseService.createPurchaseOrder(body, userId);
    }

    /**
     * 审批采购单
     * PUT /warehouse/purchase-orders/{poId}/approve
     */
    @PutMapping("/purchase-orders/{poId}/approve")
    public Result<?> approvePurchaseOrder(@PathVariable Integer poId) {
        return warehouseService.approvePurchaseOrder(poId);
    }

    /**
     * 取消采购单
     * PUT /warehouse/purchase-orders/{poId}/cancel
     */
    @PutMapping("/purchase-orders/{poId}/cancel")
    public Result<?> cancelPurchaseOrder(@PathVariable Integer poId) {
        return warehouseService.cancelPurchaseOrder(poId);
    }

    /**
     * 部分到货入库
     * PUT /warehouse/purchase-orders/{poId}/receive
     * body: [{ itemId, arrivedQuantity }]
     */
    @PutMapping("/purchase-orders/{poId}/receive")
    public Result<?> receivePurchaseOrder(
            @PathVariable Integer poId,
            @RequestBody Map<String, Object> body,
            @RequestAttribute Integer userId) {
        return warehouseService.receivePurchaseOrder(poId, body, userId);
    }

    // ==================== 报损管理 ====================

    /**
     * 报损记录列表
     * GET /warehouse/damage-records?pageNum=&pageSize=
     */
    @GetMapping("/damage-records")
    public Result<?> getDamageRecords(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return warehouseService.getDamageRecords(pageNum, pageSize);
    }

    /**
     * 创建报损记录（直接出库+记录）
     * POST /warehouse/damage-records
     * body: { productId, quantity, reason }
     */
    @PostMapping("/damage-records")
    public Result<?> createDamageRecord(
            @RequestBody Map<String, Object> body,
            @RequestAttribute Integer userId) {
        return warehouseService.createDamageRecord(body, userId);
    }

    // ==================== 库存流水 ====================

    /**
     * 库存流水查询
     * GET /warehouse/inventory-logs?productId=&logType=&pageNum=&pageSize=
     */
    @GetMapping("/inventory-logs")
    public Result<?> getInventoryLogs(
            @RequestParam(required = false) Integer productId,
            @RequestParam(required = false) String logType,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return warehouseService.getInventoryLogs(productId, logType, pageNum, pageSize);
    }

    /**
     * 库存查询
     * GET /warehouse/inventory?pageNum=&pageSize=&keyword=
     */
    @GetMapping("/inventory")
    public Result<?> getInventoryList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        return warehouseService.getInventoryList(pageNum, pageSize, keyword);
    }

    /**
     * 手动调整库存
     * PUT /warehouse/inventory/{productId}/adjust
     * body: { newStock, remark }
     */
    @PutMapping("/inventory/{productId}/adjust")
    public Result<?> adjustInventory(
            @PathVariable Integer productId,
            @RequestBody Map<String, Object> body,
            @RequestAttribute Integer userId) {
        Integer newStock = ((Number) body.get("newStock")).intValue();
        String remark = (String) body.get("remark");
        return warehouseService.adjustInventory(productId, newStock, remark, userId);
    }

    /**
     * 低库存预警
     * GET /warehouse/low-stock?pageNum=&pageSize=
     */
    @GetMapping("/low-stock")
    public Result<?> getLowStockProducts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return warehouseService.getLowStockProducts(pageNum, pageSize);
    }
}
