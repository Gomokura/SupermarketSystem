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

    /** 库存总览 GET /warehouse/inventory */
    @GetMapping("/inventory")
    public Result<?> inventoryOverview(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return warehouseService.inventoryOverview(pageNum, pageSize);
    }

    /** 低库存预警列表 GET /warehouse/inventory/low-stock */
    @GetMapping("/inventory/low-stock")
    public Result<?> lowStockList() {
        return warehouseService.lowStockList();
    }

    /** 库存流水 GET /warehouse/inventory/logs */
    @GetMapping("/inventory/logs")
    public Result<?> inventoryLogs(
            @RequestParam(required = false) Integer productId,
            @RequestParam(required = false) String changeType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return warehouseService.inventoryLogs(productId, changeType, startDate, endDate, pageNum, pageSize);
    }

    /** 报损登记 POST /warehouse/damage */
    @PostMapping("/damage")
    public Result<?> reportDamage(
            @RequestAttribute Integer adminId,
            @RequestBody Map<String, Object> body) {
        Integer productId = (Integer) body.get("productId");
        Integer quantity = (Integer) body.get("quantity");
        String reason = (String) body.get("reason");
        return warehouseService.reportDamage(adminId, productId, quantity, reason);
    }

    /** 报损记录列表 GET /warehouse/damage/list */
    @GetMapping("/damage/list")
    public Result<?> damageList(
            @RequestParam(required = false) Integer productId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return warehouseService.damageList(productId, pageNum, pageSize);
    }
}
