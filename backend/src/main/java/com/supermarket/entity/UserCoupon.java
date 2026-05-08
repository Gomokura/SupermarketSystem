package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("USER_COUPONS")
public class UserCoupon {
    @TableId(value = "UC_ID", type = IdType.AUTO)
    private Integer ucId;
    
    @TableField(exist = false)
    private Integer userCouponId;
    @TableField("user_id")
    private Integer userId;
    @TableField("coupon_id")
    private Integer couponId;
    private String status;
    @TableField("get_time")
    private Date getTime;
    @TableField("use_time")
    private Date useTime;
    @TableField("order_id")
    private Integer orderId;

    // 非数据库字段（联表）
    @TableField(exist = false)
    private String couponName;
    @TableField(exist = false)
    private String couponType;
    @TableField(exist = false)
    private Double minAmount;
    @TableField(exist = false)
    private Double discount;
    @TableField(exist = false)
    private Date endTime;
}
