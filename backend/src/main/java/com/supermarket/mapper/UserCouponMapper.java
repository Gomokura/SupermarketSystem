package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {
    @Select("SELECT SEQ_USER_COUPONS.NEXTVAL FROM DUAL")
    Integer getNextId();
}
