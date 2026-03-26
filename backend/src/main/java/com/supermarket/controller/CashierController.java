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
}
