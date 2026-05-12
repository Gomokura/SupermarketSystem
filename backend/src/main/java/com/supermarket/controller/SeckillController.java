package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.SeckillActivityModel;
import com.supermarket.entity.SeckillActivityProductModel;
import com.supermarket.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    /** C 端：秒杀活动列表（支持 state=pending/running/paused/ended） */
    @GetMapping("/activities")
    public Result<?> listActivities(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String state) {
        return seckillService.listActivities(pageNum, pageSize, state);
    }

    /** C 端：活动下的秒杀商品（仅返回剩余库存 > 0） */
    @GetMapping("/activities/{seckillId}/products")
    public Result<?> getActivityProducts(@PathVariable Integer seckillId) {
        return seckillService.getActivityProducts(seckillId);
    }

    // ==================== B 端：秒杀配置 ====================

    @GetMapping("/admin/activities")
    public Result<?> adminListActivities(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String state) {
        return seckillService.listActivities(pageNum, pageSize, state);
    }

    @PostMapping("/admin/activities")
    public Result<?> adminCreateActivity(@RequestBody SeckillActivityModel activity) {
        return seckillService.adminCreateActivity(activity);
    }

    @PutMapping("/admin/activities/{seckillId}")
    public Result<?> adminUpdateActivity(@PathVariable Integer seckillId, @RequestBody SeckillActivityModel activity) {
        return seckillService.adminUpdateActivity(seckillId, activity);
    }

    @PostMapping("/admin/activities/{seckillId}/products")
    public Result<?> adminUpsertProducts(
            @PathVariable Integer seckillId,
            @RequestBody List<SeckillActivityProductModel> items) {
        return seckillService.adminUpsertSeckillProducts(seckillId, items);
    }

    @GetMapping("/admin/activities/{seckillId}/products")
    public Result<?> adminGetActivityProducts(@PathVariable Integer seckillId) {
        return seckillService.getActivityProducts(seckillId);
    }

    @DeleteMapping("/admin/activities/{seckillId}/products/{productId}")
    public Result<?> adminDeleteActivityProduct(
            @PathVariable Integer seckillId,
            @PathVariable Integer productId) {
        return seckillService.adminDeleteSeckillProduct(seckillId, productId);
    }
}

