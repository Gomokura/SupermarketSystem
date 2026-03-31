package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Coupon;
import com.supermarket.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    // ==================== B端 ====================

    @GetMapping("/admin/list")
    public Result<?> adminGetCoupons(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        return couponService.adminGetCoupons(pageNum, pageSize, status);
    }

    @PostMapping("/admin")
    public Result<?> createCoupon(@RequestBody Coupon coupon) {
        return couponService.createCoupon(coupon);
    }

    @PutMapping("/admin/{couponId}")
    public Result<?> updateCoupon(@PathVariable Integer couponId, @RequestBody Coupon coupon) {
        coupon.setCouponId(couponId);
        return couponService.updateCoupon(coupon);
    }

    @PutMapping("/admin/{couponId}/status")
    public Result<?> toggleCoupon(
            @PathVariable Integer couponId,
            @RequestParam String status) {
        return couponService.toggleCoupon(couponId, status);
    }

    @DeleteMapping("/admin/{couponId}")
    public Result<?> deleteCoupon(@PathVariable Integer couponId) {
        return couponService.deleteCoupon(couponId);
    }

    /**
     * 批量发券（B端）
     * POST /coupons/admin/batch-issue
     * body: { "couponId": 1, "userIds": [1000,1001,...] }
     */
    @PostMapping("/admin/batch-issue")
    public Result<?> batchIssueCoupons(@RequestBody Map<String, Object> body) {
        Integer couponId = body.get("couponId") != null ? ((Number) body.get("couponId")).intValue() : null;
        @SuppressWarnings("unchecked")
        List<Integer> userIds = body.get("userIds") != null ? (List<Integer>) body.get("userIds") : null;
        return couponService.batchIssueCoupons(couponId, userIds);
    }

    // ==================== C端 ====================

    /** 优惠券中心（可领取） */
    @GetMapping("/center")
    public Result<?> couponCenter(@RequestAttribute Integer userId) {
        return couponService.couponCenter(userId);
    }

    /** 领券 */
    @PostMapping("/claim/{couponId}")
    public Result<?> claimCoupon(
            @PathVariable Integer couponId,
            @RequestAttribute Integer userId) {
        return couponService.claimCoupon(couponId, userId);
    }

    /** 我的优惠券 */
    @GetMapping("/my")
    public Result<?> getUserCoupons(
            @RequestAttribute Integer userId,
            @RequestParam(required = false) String status) {
        return couponService.getUserCoupons(userId, status);
    }

    /** 下单时可用优惠券 */
    @GetMapping("/available")
    public Result<?> getAvailableCoupons(
            @RequestAttribute Integer userId,
            @RequestParam(defaultValue = "0") Double orderAmount) {
        return couponService.getAvailableCoupons(userId, orderAmount);
    }
}
