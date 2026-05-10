package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.service.CashierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cashier")
public class CashierController {

    @Autowired
    private CashierService cashierService;

    /** 开班 POST /cashier/shift/open */
    @PostMapping("/shift/open")
    public Result<?> openShift(
            @RequestAttribute Integer adminId,
            @RequestBody Map<String, Object> body) {
        Double startCash = body.get("startCash") != null ? ((Number) body.get("startCash")).doubleValue() : 0.0;
        return cashierService.openShift(adminId, startCash);
    }

    /** 当前班次 GET /cashier/shift/current */
    @GetMapping("/shift/current")
    public Result<?> currentShift(@RequestAttribute Integer adminId) {
        return cashierService.getCurrentShift(adminId);
    }

    /** 交班 POST /cashier/shift/close */
    @PostMapping("/shift/close")
    public Result<?> closeShift(
            @RequestAttribute Integer adminId,
            @RequestBody Map<String, Object> body) {
        Double endCash = body.get("endCash") != null ? ((Number) body.get("endCash")).doubleValue() : 0.0;
        return cashierService.closeShift(adminId, endCash);
    }

    /** 历史班次 GET /cashier/shift/history */
    @GetMapping("/shift/history")
    public Result<?> shiftHistory(@RequestAttribute Integer adminId) {
        return cashierService.getShiftHistory(adminId);
    }

    /** 收银端首页概览 GET /cashier/dashboard */
    @GetMapping("/dashboard")
    public Result<?> dashboard(@RequestAttribute Integer adminId) {
        return cashierService.getDashboard(adminId);
    }

    /** 收银员日结报表 GET /cashier/daily-report */
    @GetMapping("/daily-report")
    public Result<?> dailyReport(@RequestAttribute Integer adminId) {
        return cashierService.getDailyReport(adminId);
    }

    /** K-05 搜索商品 */
    @GetMapping("/products/search")
    public Result<?> searchProducts(
            @RequestParam String keyword,
            @RequestParam(required = false) Integer limit) {
        return cashierService.searchProducts(keyword, limit);
    }

    /** K-10~K-12 结账（创建订单+收银记录+更新班次） */
    @PostMapping("/checkout")
    public Result<?> checkout(
            @RequestAttribute Integer adminId,
            @RequestBody Map<String, Object> body) {
        String memberPhone = (String) body.get("memberPhone");
        Integer couponId = body.get("couponId") != null ? ((Number) body.get("couponId")).intValue() : null;
        String payMethod = (String) body.get("payMethod"); // CASH/MOCK_CARD
        Double receivedAmount = body.get("receivedAmount") != null ? ((Number) body.get("receivedAmount")).doubleValue() : null;
        Integer pointsUsed = body.get("pointsUsed") != null ? ((Number) body.get("pointsUsed")).intValue() : null;
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        return cashierService.checkout(adminId, memberPhone, couponId, payMethod, receivedAmount, pointsUsed, items);
    }

    /** K-13 查询订单（用于退款） */
    @GetMapping("/orders/find")
    public Result<?> findOrder(@RequestParam String orderNo) {
        return cashierService.findCashierOrder(orderNo);
    }

    /** K-13 历史订单查询 */
    @GetMapping("/orders/history")
    public Result<?> orderHistory(
            @RequestAttribute Integer adminId,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return cashierService.getOrderHistory(adminId, orderNo, phone, pageNum, pageSize);
    }

    /** K-14 收银退款（整单） */
    @PostMapping("/refund")
    public Result<?> refund(
            @RequestAttribute Integer adminId,
            @RequestBody Map<String, Object> body) {
        String orderNo = (String) body.get("orderNo");
        String reason = (String) body.get("reason");
        return cashierService.refundCashierOrder(adminId, orderNo, reason);
    }
}
