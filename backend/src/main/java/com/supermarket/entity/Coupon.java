package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("COUPONS")
public class Coupon {
    @TableId(value = "COUPON_ID", type = IdType.INPUT)
    @TableField("coupon_id")
    private Integer couponId;
    @TableField("coupon_name")
    private String couponName;
    private String description;
    @TableField("coupon_type")
    private String couponType;
    @TableField("face_value")
    private Double faceValue;
    @TableField("min_amount")
    private Double minAmount;
    @TableField("category_id")
    private Integer categoryId;
    private Integer totalCount;
    private Integer perLimit;
    @TableField("issued_count")
    private Integer issuedCount;
    @TableField("used_count")
    private Integer usedCount;
    @TableField("start_time")
    private Date startTime;
    @TableField("end_time")
    private Date endTime;
    private String status;
    @TableField("create_time")
    private Date createTime;
}
