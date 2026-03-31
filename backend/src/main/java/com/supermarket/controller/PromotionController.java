package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Promotion;
import com.supermarket.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/promotions")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    /** 活动列表 GET /promotions/list */
    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String promoType) {
        return promotionService.listAll(status, promoType);
    }

    /** 进行中的活动（C端） GET /promotions/active */
    @GetMapping("/active")
    public Result<?> active() {
        return promotionService.activePromotions();
    }

    /** 活动详情 GET /promotions/{promotionId} */
    @GetMapping("/{promotionId}")
    public Result<?> detail(@PathVariable Integer promotionId) {
        return promotionService.detail(promotionId);
    }

    /** 创建活动 POST /promotions/create */
    @PostMapping("/create")
    public Result<?> create(@RequestBody Promotion promotion) {
        return promotionService.create(promotion);
    }

    /** 编辑活动 PUT /promotions/{promotionId} */
    @PutMapping("/{promotionId}")
    public Result<?> update(@PathVariable Integer promotionId, @RequestBody Promotion promotion) {
        return promotionService.update(promotionId, promotion);
    }

    /** 更新活动状态 PUT /promotions/{promotionId}/status */
    @PutMapping("/{promotionId}/status")
    public Result<?> updateStatus(
            @PathVariable Integer promotionId,
            @RequestBody Map<String, String> body) {
        return promotionService.updateStatus(promotionId, body.get("status"));
    }
}
