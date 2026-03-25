package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.service.StocktakeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/stocktake")
public class StocktakeController {

    @Autowired
    private StocktakeService stocktakeService;

    /**
     * 创建盘点任务
     * POST /stocktake
     * body: { scope: "all" | "category", categoryId }
     */
    @PostMapping
    public Result<?> createStocktakeTask(
            @RequestBody Map<String, Object> body,
            @RequestAttribute Integer userId) {
        String scope = (String) body.get("scope");
        Integer categoryId = (Integer) body.get("categoryId");
        return stocktakeService.createStocktakeTask(scope, categoryId, userId);
    }

    /**
     * 盘点任务列表
     * GET /stocktake?status=&pageNum=&pageSize=
     */
    @GetMapping
    public Result<?> getStocktakeTasks(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return stocktakeService.getStocktakeTasks(status, pageNum, pageSize);
    }

    /**
     * 盘点任务详情（含盘点项列表）
     * GET /stocktake/{taskId}
     */
    @GetMapping("/{taskId}")
    public Result<?> getStocktakeDetail(@PathVariable Integer taskId) {
        return stocktakeService.getStocktakeDetail(taskId);
    }

    /**
     * 提交盘点结果（盘盈/盘亏自动调整库存 + 写流水）
     * PUT /stocktake/{taskId}/submit
     * body: { items: [{ itemId, actualStock, diffReason }] }
     */
    @PutMapping("/{taskId}/submit")
    public Result<?> submitStocktake(
            @PathVariable Integer taskId,
            @RequestBody Map<String, Object> body,
            @RequestAttribute Integer userId) {
        return stocktakeService.submitStocktake(taskId, body, userId);
    }
}
