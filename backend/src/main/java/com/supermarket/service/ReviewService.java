package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.BusinessException;
import com.supermarket.common.Result;
import com.supermarket.entity.Product;
import com.supermarket.entity.Review;
import com.supermarket.entity.User;
import com.supermarket.mapper.ProductMapper;
import com.supermarket.mapper.ReviewMapper;
import com.supermarket.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Service
public class ReviewService extends ServiceImpl<ReviewMapper, Review> {

    @Autowired private UserMapper userMapper;
    @Autowired private ProductMapper productMapper;

    /** C端：查询商品的公开评价 */
    public Result<?> getProductReviews(Integer productId, Integer pageNum, Integer pageSize) {
        Page<Review> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getProductId, productId)
               .eq(Review::getIsHidden, 0)
               .orderByDesc(Review::getCreateTime);
        this.page(page, wrapper);
        fillUserInfo(page.getRecords());
        return Result.success(page);
    }

    /** C端：提交评价 */
    @Transactional
    public Result<?> submitReview(Review review, Integer userId) {
        review.setUserId(userId);
        review.setIsHidden(0);
        review.setCreateTime(new Date());
        this.save(review);

        // 更新商品平均评分
        updateProductAvgRating(review.getProductId());
        return Result.success();
    }

    /** B端：查询所有评价（含隐藏） */
    public Result<?> adminGetReviews(Integer pageNum, Integer pageSize,
                                      Integer productId, Integer rating, String keyword) {
        Page<Review> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        if (productId != null) wrapper.eq(Review::getProductId, productId);
        if (rating != null) wrapper.eq(Review::getRating, rating);
        if (StringUtils.hasText(keyword)) wrapper.like(Review::getContent, keyword);
        wrapper.orderByDesc(Review::getCreateTime);
        this.page(page, wrapper);
        fillUserInfo(page.getRecords());
        return Result.success(page);
    }

    /** B端：回复评价 */
    @Transactional
    public Result<?> replyReview(Integer reviewId, String reply) {
        Review review = this.getById(reviewId);
        if (review == null) throw new BusinessException(404, "评价不存在");
        review.setReply(reply);
        review.setReplyTime(new Date());
        this.updateById(review);
        return Result.success();
    }

    /** B端：显示/隐藏评价 */
    @Transactional
    public Result<?> toggleHidden(Integer reviewId, Integer isHidden) {
        Review review = this.getById(reviewId);
        if (review == null) throw new BusinessException(404, "评价不存在");
        review.setIsHidden(isHidden);
        this.updateById(review);
        return Result.success();
    }

    /** 删除评价 */
    public Result<?> deleteReview(Integer reviewId) {
        this.removeById(reviewId);
        return Result.success();
    }

    // ---- 内部工具 ----

    private void fillUserInfo(List<Review> reviews) {
        for (Review r : reviews) {
            Product p = productMapper.selectById(r.getProductId());
            if (p != null) r.setProductName(p.getProductName());

            if (r.getIsAnonymous() != null && r.getIsAnonymous() == 1) {
                r.setUsername("匿名用户");
            } else {
                User u = userMapper.selectById(r.getUserId());
                if (u != null) {
                    r.setUsername(u.getNickname() != null ? u.getNickname() : u.getUsername());
                    r.setAvatar(u.getAvatarUrl());
                }
            }
        }
    }

    private void updateProductAvgRating(Integer productId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getProductId, productId).eq(Review::getIsHidden, 0);
        List<Review> all = this.list(wrapper);
        if (all.isEmpty()) return;
        double avg = all.stream().mapToInt(r -> r.getRating() != null ? r.getRating() : 0)
                        .average().orElse(0);
        Product product = productMapper.selectById(productId);
        if (product != null) {
            product.setAvgRating(Math.round(avg * 10.0) / 10.0);
            productMapper.updateById(product);
        }
    }
}
