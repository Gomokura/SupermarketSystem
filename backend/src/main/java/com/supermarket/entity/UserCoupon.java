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
    private Integer userId;
    private Integer couponId;
    /** 状态：unused未使用 / used已使用 / expired已过期 */
    private String status;
    private Date getTime;         // 数据库字段名 GET_TIME（默认 camelCase → 下划线正确匹配）
    private Date useTime;
    private Integer orderId;      // 使用时关联的订单ID

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
