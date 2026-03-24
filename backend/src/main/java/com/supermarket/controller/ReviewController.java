package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Review;
import com.supermarket.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /** C端：查看商品评价（无需登录） */
    @GetMapping("/product/{productId}")
    public Result<?> getProductReviews(
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return reviewService.getProductReviews(productId, pageNum, pageSize);
    }

    /** C端：提交评价 */
    @PostMapping
    public Result<?> submitReview(
            @RequestBody Review review,
            @RequestAttribute Integer userId) {
        return reviewService.submitReview(review, userId);
    }

    /** B端：查询所有评价 */
    @GetMapping("/admin/list")
    public Result<?> adminGetReviews(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer productId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String keyword) {
        return reviewService.adminGetReviews(pageNum, pageSize, productId, rating, keyword);
    }

    /** B端：回复评价 */
    @PutMapping("/admin/{reviewId}/reply")
    public Result<?> replyReview(
            @PathVariable Integer reviewId,
            @RequestBody Map<String, String> body) {
        return reviewService.replyReview(reviewId, body.get("reply"));
    }

    /** B端：显示/隐藏 */
    @PutMapping("/admin/{reviewId}/hidden")
    public Result<?> toggleHidden(
            @PathVariable Integer reviewId,
            @RequestParam Integer isHidden) {
        return reviewService.toggleHidden(reviewId, isHidden);
    }

    /** B端：删除评价 */
    @DeleteMapping("/admin/{reviewId}")
    public Result<?> deleteReview(@PathVariable Integer reviewId) {
        return reviewService.deleteReview(reviewId);
    }
}
