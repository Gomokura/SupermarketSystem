package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Coupon;
import com.supermarket.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    // ==================== C端 ====================

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
