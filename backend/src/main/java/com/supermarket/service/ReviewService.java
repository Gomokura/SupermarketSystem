package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.BusinessException;
import com.supermarket.common.Result;
import com.supermarket.entity.Order;
import com.supermarket.entity.OrderItem;
import com.supermarket.entity.Product;
import com.supermarket.entity.Review;
import com.supermarket.entity.User;
import com.supermarket.mapper.OrderItemMapper;
import com.supermarket.mapper.OrderMapper;
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
    @Autowired private ReviewMapper reviewMapper;
    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderItemMapper orderItemMapper;

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
        validateReviewRequest(review, userId);

        LambdaQueryWrapper<Review> duplicateWrapper = new LambdaQueryWrapper<>();
        duplicateWrapper.eq(Review::getOrderItemId, review.getOrderItemId());
        if (this.count(duplicateWrapper) > 0) {
            throw new BusinessException("该商品已评价，请勿重复提交");
        }

        review.setUserId(userId);
        review.setIsHidden(0);
        review.setCreateTime(new Date());
        review.setReviewId(reviewMapper.getNextId());
        try {
            this.save(review);
        } catch (RuntimeException e) {
            if (isDuplicateReviewException(e)) {
                throw new BusinessException("该商品已评价，请勿重复提交");
            }
            throw e;
        }

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

    /** 删除评价（同时更新商品评分） */
    @Transactional
    public Result<?> deleteReview(Integer reviewId) {
        Review review = this.getById(reviewId);
        if (review == null) return Result.success();
        Integer productId = review.getProductId();
        this.removeById(reviewId);
        // 重新计算平均评分
        updateProductAvgRating(productId);
        return Result.success();
    }

    // ---- 内部工具 ----

    private void validateReviewRequest(Review review, Integer userId) {
        if (review == null) throw new BusinessException("评价内容不能为空");
        if (review.getOrderId() == null) throw new BusinessException("订单ID不能为空");
        if (review.getOrderItemId() == null) throw new BusinessException("订单商品ID不能为空");
        if (review.getProductId() == null) throw new BusinessException("商品ID不能为空");
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new BusinessException("评分必须在1到5之间");
        }
        if (!StringUtils.hasText(review.getContent())) {
            throw new BusinessException("评价内容不能为空");
        }

        Order order = orderMapper.selectById(review.getOrderId());
        if (order == null) throw new BusinessException(404, "订单不存在");
        if (!order.getUserId().equals(userId)) throw new BusinessException(403, "无权评价该订单");
        if (!"COMPLETED".equals(order.getStatus())) {
            throw new BusinessException("订单完成后才能评价");
        }

        OrderItem item = orderItemMapper.selectById(review.getOrderItemId());
        if (item == null
                || !review.getOrderId().equals(item.getOrderId())
                || !review.getProductId().equals(item.getProductId())) {
            throw new BusinessException("订单商品信息不匹配");
        }
    }

    private boolean isDuplicateReviewException(Throwable e) {
        Throwable current = e;
        while (current != null) {
            String message = current.getMessage();
            if (message != null
                    && (message.contains("UQ_REV_ORDER_ITEM")
                    || message.contains("ORA-00001"))) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

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
