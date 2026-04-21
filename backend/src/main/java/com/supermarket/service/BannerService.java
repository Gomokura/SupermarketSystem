package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.BusinessException;
import com.supermarket.common.Result;
import com.supermarket.entity.Banner;
import com.supermarket.mapper.BannerMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class BannerService extends ServiceImpl<BannerMapper, Banner> {

    /** C端：查询当前有效的轮播图（status=active） */
    public Result<?> getActiveBanners() {
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Banner::getStatus, "active")
               .orderByAsc(Banner::getSortOrder);
        List<Banner> list = this.list(wrapper);
        // 兼容前端 isActive 字段
        list.forEach(b -> b.setIsActive("active".equals(b.getStatus()) ? 1 : 0));
        return Result.success(list);
    }

    /** B端：查询所有轮播图 */
    public Result<?> getAllBanners() {
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Banner::getSortOrder);
        List<Banner> list = this.list(wrapper);
        list.forEach(b -> b.setIsActive("active".equals(b.getStatus()) ? 1 : 0));
        return Result.success(list);
    }

    @Transactional
    public Result<?> addBanner(Banner banner) {
        if (banner.getStatus() == null) banner.setStatus("active");
        if (banner.getSortOrder() == null) banner.setSortOrder(99);
        banner.setCreateTime(new Date());
        this.save(banner);
        return Result.success();
    }

    @Transactional
    public Result<?> updateBanner(Banner banner) {
        if (banner.getBannerId() == null) throw new BusinessException("bannerId不能为空");
        this.updateById(banner);
        return Result.success();
    }

    public Result<?> deleteBanner(Integer bannerId) {
        this.removeById(bannerId);
        return Result.success();
    }

    @Transactional
    public Result<?> toggleBanner(Integer bannerId, Integer isActive) {
        Banner banner = this.getById(bannerId);
        if (banner == null) throw new BusinessException(404, "轮播图不存在");
        banner.setStatus(isActive != null && isActive == 1 ? "active" : "inactive");
        this.updateById(banner);
        return Result.success();
    }
}
