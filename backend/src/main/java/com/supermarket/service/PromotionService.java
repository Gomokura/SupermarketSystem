package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.Result;
import com.supermarket.entity.Promotion;
import com.supermarket.mapper.PromotionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class PromotionService extends ServiceImpl<PromotionMapper, Promotion> {

    public Result<?> listAll(String status, String promoType) {
        LambdaQueryWrapper<Promotion> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) wrapper.eq(Promotion::getStatus, status);
        if (promoType != null && !promoType.isEmpty()) wrapper.eq(Promotion::getPromoType, promoType);
        wrapper.orderByDesc(Promotion::getStartTime);
        List<Promotion> list = this.list(wrapper);
        return Result.success(list);
    }

    @Transactional
    public Result<?> create(Promotion promotion) {
        if (promotion.getStatus() == null) promotion.setStatus("active");
        this.save(promotion);
        return Result.success(promotion);
    }

    @Transactional
    public Result<?> update(Integer promotionId, Promotion promotion) {
        if (this.getById(promotionId) == null) return Result.error("活动不存在");
        promotion.setPromotionId(promotionId);
        this.updateById(promotion);
        return Result.success(promotion);
    }

    @Transactional
    public Result<?> updateStatus(Integer promotionId, String status) {
        Promotion p = this.getById(promotionId);
        if (p == null) return Result.error("活动不存在");
        p.setStatus(status);
        this.updateById(p);
        return Result.success(null);
    }

    public Result<?> detail(Integer promotionId) {
        Promotion p = this.getById(promotionId);
        if (p == null) return Result.error("活动不存在");
        return Result.success(p);
    }

    /** 获取当前进行中的满减活动（C端使用） */
    public Result<?> activePromotions() {
        Date now = new Date();
        LambdaQueryWrapper<Promotion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Promotion::getStatus, "active")
               .le(Promotion::getStartTime, now)
               .ge(Promotion::getEndTime, now);
        return Result.success(this.list(wrapper));
    }
}
