package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.AfterSale;
import com.supermarket.service.AfterSaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/after-sales")
public class AfterSaleController {

    @Autowired
    private AfterSaleService afterSaleService;

    // ==================== C端 ====================

    @PostMapping
    public Result<?> applyAfterSale(
            @RequestBody AfterSale afterSale,
            @RequestAttribute Integer userId) {
        return afterSaleService.applyAfterSale(afterSale, userId);
    }

    @GetMapping("/my")
    public Result<?> getUserAfterSales(@RequestAttribute Integer userId) {
        return afterSaleService.getUserAfterSales(userId);
    }

    @GetMapping("/{afterSaleId}")
    public Result<?> getAfterSaleDetail(
            @PathVariable Integer afterSaleId,
            @RequestAttribute Integer userId) {
        return afterSaleService.getAfterSaleDetail(afterSaleId, userId);
    }

    // ==================== B端 ====================

    @GetMapping("/admin/list")
    public Result<?> adminGetAfterSales(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        return afterSaleService.adminGetAfterSales(pageNum, pageSize, status);
    }

    @PutMapping("/admin/{afterSaleId}/handle")
    public Result<?> handleAfterSale(
            @PathVariable Integer afterSaleId,
            @RequestBody Map<String, String> body) {
        return afterSaleService.handleAfterSale(
            afterSaleId, body.get("action"), body.get("remark"));
    }

    @PutMapping("/admin/{afterSaleId}/refund")
    public Result<?> completeRefund(@PathVariable Integer afterSaleId) {
        return afterSaleService.completeRefund(afterSaleId);
    }
}
