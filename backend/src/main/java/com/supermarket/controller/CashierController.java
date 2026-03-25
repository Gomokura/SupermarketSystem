package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.service.CashierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cashier")
public class CashierController {

    @Autowired
    private CashierService cashierService;

    /**
     * 开班（开启收银班次）
     * POST /cashier/shift/open
     * body: { startCash: 500 }
     */
    @PostMapping("/shift/open")
    public Result<?> openShift(
            @RequestBody Map<String, Object> body,
            @RequestAttribute Integer userId) {
        Double startCash = body.get("startCash") != null
                ? ((Number) body.get("startCash")).doubleValue() : 0.0;
        return cashierService.openShift(userId, startCash);
    }

    /**
     * 交班（关闭收银班次）
     * POST /cashier/shift/close
     * body: { endCash: 1234.5 }
     */
    @PostMapping("/shift/close")
    public Result<?> closeShift(
            @RequestBody Map<String, Object> body,
            @RequestAttribute Integer userId) {
        Double endCash = body.get("endCash") != null
                ? ((Number) body.get("endCash")).doubleValue() : 0.0;
        return cashierService.closeShift(userId, endCash);
    }

    /**
     * 当前班次状态查询
     * GET /cashier/shift/current
     */
    @GetMapping("/shift/current")
    public Result<?> getCurrentShift(@RequestAttribute Integer userId) {
        return cashierService.getCurrentShift(userId);
    }

    /**
     * 班次历史记录
     * GET /cashier/shift/history?pageNum=1&pageSize=10
     */
    @GetMapping("/shift/history")
    public Result<?> getShiftHistory(
            @RequestAttribute Integer userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return cashierService.getShiftHistory(userId, pageNum, pageSize);
    }

    /**
     * 班次差额报表
     * GET /cashier/shift/{shiftId}/report
     */
    @GetMapping("/shift/{shiftId}/report")
    public Result<?> getShiftReport(@PathVariable Integer shiftId) {
        return cashierService.getShiftReport(shiftId);
    }

    /**
     * 商品快速搜索（收银台扫码/关键字）
     * GET /cashier/products/search?keyword=&barcode=&pageNum=&pageSize=
     */
    @GetMapping("/products/search")
    public Result<?> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String barcode,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return cashierService.searchProducts(keyword, barcode, pageNum, pageSize);
    }

    /**
     * 收银台汇总统计（当前班次）
     * GET /cashier/shift/{shiftId}/summary
     */
    @GetMapping("/shift/{shiftId}/summary")
    public Result<?> getShiftSummary(
            @PathVariable Integer shiftId,
            @RequestAttribute Integer userId) {
        return cashierService.getShiftSummary(shiftId, userId);
    }
}
