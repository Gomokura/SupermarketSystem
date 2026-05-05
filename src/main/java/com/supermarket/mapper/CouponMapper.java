package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {
    @Select("SELECT SEQ_COUPONS.NEXTVAL FROM DUAL")
    Integer getNextId();
}
