package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
@TableName("ORDERS")
public class Order {
    @TableId(type = IdType.INPUT)
    private Integer orderId;

    private String orderNo;
    private Integer userId;
    /** ONLINE / CASHIER */
    private String source;
    private Integer addressId;
    /** 收货人信息快照（receiver + phone + province/city/district/detail） */
    @TableField("RECEIVER_SNAPSHOT")
    private String receiverSnapshot;

    private Double totalAmount;
    private Double discountAmount;
    /** 优惠券抵扣金额（明细） */
    private Double couponDiscount;
    /** 积分抵扣金额（明细） */
    private Double pointsDeductAmount;
    @TableField("FREIGHT_AMOUNT")
    private Double freightAmount;

    private Double payAmount;
    private String payMethod;
    private Integer couponId;
    /** user_coupons 的 UC_ID（核销用） */
    private Integer ucId;
    private Integer pointsUsed;

    /** 期望配送时间段（如“明日上午”） */
    private String deliveryTimeSlot;
    private String expressCompany;
    private String expressNo;

    private String remark;
    private String cancelReason;
    private Double refundAmount;

    /** PENDING_PAY / PAID / PENDING_SHIP / SHIPPING / PENDING_RECEIVED / COMPLETED / CANCELLED / REFUNDED */
    private String status;

    private Date payTime;
    private Date shipTime;
    private Date pickupTime;
    @TableField("DELIVER_TIME")
    private Date deliverTime;
    private Date confirmTime;
    private Date completeTime;
    private Date cancelTime;
    private Date refundTime;
    private Date createTime;
    private Date updateTime;

    // 非数据库字段
    @TableField(exist = false)
    private String username;          // 用户名（联表）
    @TableField(exist = false)
    private List<OrderItem> items;    // 订单明细（联表）
    @TableField(exist = false)
    private String receiverName;      // 收货人姓名（从快照解析）
    @TableField(exist = false)
    private String receiverPhone;    // 联系电话（从快照解析）
    @TableField(exist = false)
    private String receiverAddress;  // 收货地址（从快照解析）
}
