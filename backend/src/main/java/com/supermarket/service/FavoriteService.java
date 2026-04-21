package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.BusinessException;
import com.supermarket.common.Result;
import com.supermarket.entity.Favorite;
import com.supermarket.entity.Product;
import com.supermarket.mapper.FavoriteMapper;
import com.supermarket.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class FavoriteService extends ServiceImpl<FavoriteMapper, Favorite> {

    @Autowired
    private ProductMapper productMapper;

    /** 我的收藏列表 */
    public Result<?> getMyFavorites(Integer userId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.orderByDesc(Favorite::getFavId);
        List<Favorite> list = this.list(wrapper);

        for (Favorite f : list) {
            Product p = productMapper.selectById(f.getProductId());
            if (p != null) {
                f.setProductName(p.getProductName());
                f.setCoverImage(p.getCoverImage());
                f.setPrice(p.getPrice());
                f.setStock(p.getStock());
                f.setUnit(p.getUnit());
            }
        }
        return Result.success(list);
    }

    /** 收藏商品 */
    @Transactional
    public Result<?> addFavorite(Integer userId, Integer productId) {
        Product p = productMapper.selectById(productId);
        if (p == null || p.getIsDeleted() == 1) {
            throw new BusinessException(404, "商品不存在");
        }

        LambdaQueryWrapper<Favorite> check = new LambdaQueryWrapper<>();
        check.eq(Favorite::getUserId, userId).eq(Favorite::getProductId, productId);
        if (this.count(check) > 0) {
            return Result.success("已收藏");
        }

        Integer favId = this.baseMapper.nextId();
        Favorite f = new Favorite();
        f.setFavId(favId);
        f.setUserId(userId);
        f.setProductId(productId);
        f.setCreateTime(new Date());
        this.save(f);
        return Result.success("收藏成功");
    }

    /** 取消收藏 */
    @Transactional
    public Result<?> removeFavorite(Integer userId, Integer productId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId).eq(Favorite::getProductId, productId);
        this.remove(wrapper);
        return Result.success("已取消收藏");
    }
}

