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
    @TableId(value = "ORDER_ID", type = IdType.INPUT)
    @TableField("order_id")
    private Integer orderId;

    @TableField("order_no")
    private String orderNo;
    @TableField("user_id")
    private Integer userId;
    private String source;
    @TableField("address_id")
    private Integer addressId;
    private String receiverSnapshot;

    @TableField("total_amount")
    private Double totalAmount;
    @TableField("discount_amount")
    private Double discountAmount;
    @TableField("coupon_discount")
    private Double couponDiscount;
    @TableField("points_deduct_amount")
    private Double pointsDeductAmount;
    @TableField("freight_amount")
    private Double freightAmount;

    private Double payAmount;
    private String payMethod;
    @TableField("coupon_id")
    private Integer couponId;
    @TableField("uc_id")
    private Integer ucId;
    @TableField("points_used")
    private Integer pointsUsed;

    @TableField("delivery_time_slot")
    private String deliveryTimeSlot;
    @TableField("express_company")
    private String expressCompany;
    @TableField("express_no")
    private String expressNo;

    private String remark;
    private String cancelReason;
    private Double refundAmount;

    private String status;
    private Date payTime;
    private Date shipTime;
    private Date pickupTime;
    @TableField("deliver_time")
    private Date deliverTime;
    private Date confirmTime;
    private Date completeTime;
    @TableField("cancel_time")
    private Date cancelTime;
    @TableField("refund_time")
    private Date refundTime;
    @TableField("create_time")
    private Date createTime;
    @TableField("update_time")
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
    @TableField(exist = false)
    private Integer deliveryTaskId;
    @TableField(exist = false)
    private String deliveryStatus;
    @TableField(exist = false)
    private String courierName;
    @TableField(exist = false)
    private String courierPhone;
    @TableField(exist = false)
    private String deliveryFailReason;
}
