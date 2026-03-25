package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.BusinessException;
import com.supermarket.common.Result;
import com.supermarket.entity.Coupon;
import com.supermarket.entity.UserCoupon;
import com.supermarket.mapper.CouponMapper;
import com.supermarket.mapper.UserCouponMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CouponService extends ServiceImpl<CouponMapper, Coupon> {

    @Autowired private UserCouponMapper userCouponMapper;

    /** B端：分页查询优惠券列表 */
    public Result<?> adminGetCoupons(Integer pageNum, Integer pageSize, String status) {
        Page<Coupon> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) wrapper.eq(Coupon::getStatus, status);
        wrapper.orderByDesc(Coupon::getCreateTime);
        this.page(page, wrapper);
        return Result.success(page);
    }

    /** B端：创建优惠券 */
    @Transactional
    public Result<?> createCoupon(Coupon coupon) {
        coupon.setIssuedCount(0);
        coupon.setStatus("active");
        coupon.setCreateTime(new Date());
        this.save(coupon);
        return Result.success();
    }

    /** B端：更新优惠券 */
    @Transactional
    public Result<?> updateCoupon(Coupon coupon) {
        if (coupon.getCouponId() == null) throw new BusinessException("couponId不能为空");
        this.updateById(coupon);
        return Result.success();
    }

    /** B端：暂停/恢复优惠券 */
    @Transactional
    public Result<?> toggleCoupon(Integer couponId, String status) {
        Coupon coupon = this.getById(couponId);
        if (coupon == null) throw new BusinessException(404, "优惠券不存在");
        coupon.setStatus(status);
        this.updateById(coupon);
        return Result.success();
    }

    /** B端：删除优惠券 */
    public Result<?> deleteCoupon(Integer couponId) {
        this.removeById(couponId);
        return Result.success();
    }

    /** C端：用户领取优惠券 */
    @Transactional
    public Result<?> claimCoupon(Integer couponId, Integer userId) {
        Coupon coupon = this.getById(couponId);
        if (coupon == null) throw new BusinessException(404, "优惠券不存在");
        if (!"active".equals(coupon.getStatus())) throw new BusinessException("优惠券已下架");
        Date now = new Date();
        if (coupon.getEndTime() != null && coupon.getEndTime().before(now))
            throw new BusinessException("优惠券已过期");
        if (coupon.getTotalCount() != null && coupon.getTotalCount() > 0
                && coupon.getIssuedCount() >= coupon.getTotalCount())
            throw new BusinessException("优惠券已领完");

        // 检查是否已领过
        LambdaQueryWrapper<UserCoupon> check = new LambdaQueryWrapper<>();
        check.eq(UserCoupon::getUserId, userId).eq(UserCoupon::getCouponId, couponId);
        if (userCouponMapper.selectCount(check) > 0)
            throw new BusinessException("您已领取过该优惠券");

        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setCouponId(couponId);
        uc.setStatus("unused");
        uc.setGetTime(now);
        userCouponMapper.insert(uc);

        // 更新已发放数量
        coupon.setIssuedCount(coupon.getIssuedCount() + 1);
        this.updateById(coupon);
        return Result.success();
    }

    /** C端：我的优惠券列表 */
    public Result<?> getUserCoupons(Integer userId, String status) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId);
        if (StringUtils.hasText(status)) wrapper.eq(UserCoupon::getStatus, status);
        wrapper.orderByDesc(UserCoupon::getGetTime);
        List<UserCoupon> list = userCouponMapper.selectList(wrapper);

        // 填充优惠券详情
        for (UserCoupon uc : list) {
            Coupon coupon = this.getById(uc.getCouponId());
            if (coupon != null) {
                uc.setCouponName(coupon.getCouponName());
                uc.setCouponType(coupon.getCouponType());
                uc.setMinAmount(coupon.getMinAmount());
                uc.setDiscount(coupon.getFaceValue());
                uc.setEndTime(coupon.getEndTime());
            }
        }
        return Result.success(list);
    }

    /** C端：获取可用优惠券列表（下单时） */
    public Result<?> getAvailableCoupons(Integer userId, Double orderAmount) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId).eq(UserCoupon::getStatus, "unused");
        List<UserCoupon> list = userCouponMapper.selectList(wrapper);

        Date now = new Date();
        List<UserCoupon> available = new ArrayList<>();
        for (UserCoupon uc : list) {
            Coupon coupon = this.getById(uc.getCouponId());
            if (coupon == null) continue;
            if (coupon.getEndTime() != null && coupon.getEndTime().before(now)) continue;
            if (coupon.getMinAmount() != null && orderAmount < coupon.getMinAmount()) continue;
            uc.setCouponName(coupon.getCouponName());
            uc.setCouponType(coupon.getCouponType());
            uc.setMinAmount(coupon.getMinAmount());
            uc.setDiscount(coupon.getFaceValue());
            uc.setEndTime(coupon.getEndTime());
            available.add(uc);
        }
        return Result.success(available);
    }

    // ==================== B端：批量发券 ====================

    /**
     * 批量发券：对每个 userId 插入一条 user_coupon（若已存在则跳过）。
     * 仅操作优惠券发放记录（user_coupons）和 coupons.issued_count，不触碰订单/库存履约流转。
     */
    @Transactional
    public Result<?> batchIssueCoupons(Integer couponId, List<Integer> userIds) {
        if (couponId == null) throw new BusinessException("couponId不能为空");
        if (userIds == null || userIds.isEmpty()) throw new BusinessException("userIds不能为空");

        Coupon coupon = this.getById(couponId);
        if (coupon == null) throw new BusinessException(404, "优惠券不存在");
        if (!"active".equals(coupon.getStatus())) throw new BusinessException("优惠券已下架");

        Date now = new Date();
        if (coupon.getStartTime() != null && now.before(coupon.getStartTime())) {
            throw new BusinessException("优惠券尚未开始");
        }
        if (coupon.getEndTime() != null && coupon.getEndTime().before(now)) {
            throw new BusinessException("优惠券已过期");
        }

        Integer totalCount = coupon.getTotalCount();
        int totalLimit = (totalCount != null ? totalCount : -1);

        int issuedCount = coupon.getIssuedCount() != null ? coupon.getIssuedCount() : 0;

        int inserted = 0;
        int skipped = 0;

        for (Integer userId : userIds) {
            if (userId == null) continue;

            LambdaQueryWrapper<UserCoupon> check = new LambdaQueryWrapper<>();
            check.eq(UserCoupon::getUserId, userId).eq(UserCoupon::getCouponId, couponId);
            if (userCouponMapper.selectCount(check) > 0) {
                skipped++;
                continue;
            }

            // 有总量限制时，发放到上限后停止
            if (totalLimit > 0 && issuedCount >= totalLimit) {
                break;
            }

            UserCoupon uc = new UserCoupon();
            uc.setUserId(userId);
            uc.setCouponId(couponId);
            uc.setStatus("unused");
            uc.setGetTime(now);
            userCouponMapper.insert(uc);

            inserted++;
            issuedCount++;
        }

        coupon.setIssuedCount(issuedCount);
        this.updateById(coupon);

        Map<String, Object> data = new HashMap<>();
        data.put("successCount", inserted);
        data.put("skippedCount", skipped);
        data.put("requestedCount", userIds.size());
        data.put("issuedCount", issuedCount);
        return Result.success(data);
    }
}
