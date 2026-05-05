package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("COUPONS")
public class Coupon {
    @TableId(type = IdType.INPUT)
    private Integer couponId;
    private String couponName;
    private String description;
    /** 类型：full_reduction满减 / discount折扣 / category品类券 */
    private String couponType;
    private Double faceValue;     // 数据库字段 FACE_VALUE（默认 camelCase → 下划线正确匹配）
    private Double minAmount;     // 使用门槛金额
    private Integer categoryId;   // 品类券指定分类ID，null=全场
    private Integer totalCount;   // 总发放量，0=不限
    private Integer issuedCount;  // 已发放数量
    /** 每人限领次数，-1=不限 */
    private Integer perLimit;
    private Date startTime;
    private Date endTime;
    private String status;        // active / paused / expired
    private Date createTime;
}
