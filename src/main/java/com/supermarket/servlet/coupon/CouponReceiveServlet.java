package com.supermarket.servlet.coupon;

import com.supermarket.entity.Coupon;
import com.supermarket.entity.UserCoupon;
import com.supermarket.mapper.CouponMapper;
import com.supermarket.mapper.UserCouponMapper;
import com.supermarket.servlet.BaseServlet;
import com.supermarket.util.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class CouponReceiveServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer userId = getLoginUserId(req);
        if (userId == null) { jsonError(resp, "请先登录"); return; }
        Integer couponId = getInteger(req, "couponId");
        if (couponId == null) { jsonError(resp, "优惠券ID不能为空"); return; }

        CouponMapper couponMapper = new CouponMapper();
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) { jsonError(resp, "优惠券不存在"); return; }
        if (!"active".equals(coupon.getStatus())) { jsonError(resp, "优惠券已停发"); return; }
        if (coupon.getTotalCount() != null && coupon.getTotalCount() > 0
                && coupon.getIssuedCount() >= coupon.getTotalCount()) {
            jsonError(resp, "优惠券已领完"); return;
        }

        UserCouponMapper ucMapper = new UserCouponMapper();
        UserCoupon uc = new UserCoupon();
        uc.setUcId(DBUtil.getNextId("SEQ_USER_COUPONS"));
        uc.setUserId(userId);
        uc.setCouponId(couponId);
        uc.setStatus("unused");
        uc.setGetTime(new Date());
        ucMapper.insert(uc);

        coupon.setIssuedCount(coupon.getIssuedCount() + 1);
        couponMapper.updateById(coupon);
        jsonMsg(req, resp, "领取成功");
    }
}
