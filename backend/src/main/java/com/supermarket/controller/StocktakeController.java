package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.service.StocktakeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stocktake")
public class StocktakeController {

    @Autowired
    private StocktakeService stocktakeService;

    /** 创建盘点任务 POST /stocktake/create */
    @PostMapping("/create")
    public Result<?> create(
            @RequestAttribute Integer adminId,
            @RequestBody Map<String, Object> body) {
        String scope = (String) body.get("scope");
        Integer categoryId = body.get("categoryId") != null ? (Integer) body.get("categoryId") : null;
        return stocktakeService.create(adminId, scope, categoryId);
    }

    /** 盘点任务列表 GET /stocktake/list */
    @GetMapping("/list")
    public Result<?> list(@RequestParam(required = false) String status) {
        return stocktakeService.list(status);
    }

    /** 盘点任务详情 GET /stocktake/{taskId} */
    @GetMapping("/{taskId}")
    public Result<?> detail(@PathVariable Integer taskId) {
        return stocktakeService.detail(taskId);
    }

    /** 录入实际数量 PUT /stocktake/{taskId}/input */
    @PutMapping("/{taskId}/input")
    public Result<?> inputActual(
            @PathVariable Integer taskId,
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> inputs = (List<Map<String, Object>>) body.get("items");
        return stocktakeService.inputActual(taskId, inputs);
    }

    /** 提交盘点结果 PUT /stocktake/{taskId}/submit */
    @PutMapping("/{taskId}/submit")
    public Result<?> submit(
            @PathVariable Integer taskId,
            @RequestAttribute Integer adminId) {
        return stocktakeService.submit(taskId, adminId);
    }
}
