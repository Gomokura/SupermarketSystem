package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
@TableName("ORDERS")
public class Order {
    @TableId(type = IdType.AUTO)
    private Integer orderId;
    private Integer userId;
    private String orderNo;           // 业务唯一订单号
    private Integer addressId;        // 收货地址ID
    private Double totalAmount;       // 商品总价
    private Double discountAmount;    // 优惠金额
    @TableField("PAY_AMOUNT")
    private Double payAmount;         // 实付金额
    @TableField("PAY_METHOD")
    private String payMethod;         // 支付方式
    private Integer couponId;         // 使用的优惠券ID
    private Integer pointsUsed;       // 使用积分数
    @TableField("STATUS")
    private String status;            // 订单状态: pending/paid/shipped/completed/cancelled
    @TableField("DELIVERY_PERSON_ID")
    private Integer deliveryPersonId; // 配送员ID
    private String remark;            // 订单备注
    private Date payTime;             // 支付时间
    @TableField("SHIP_TIME")
    private Date shipTime;            // 发货时间
    @TableField("COMPLETE_TIME")
    private Date completeTime;        // 完成时间
    @TableField("CANCEL_TIME")
    private Date cancelTime;          // 取消时间
    private Date createTime;          // 下单时间
    private Double freight;           // 运费
    private String addressSnapshot;   // 地址快照
    private String receiverSnapshot;    // 收货人信息快照（receiver_snapshot）
    private String expressCompany;      // 快递公司（express_company）
    private String expressNo;           // 快递单号（express_no）
    private String source;            // 来源 online/cashier
    private String deliveryTimeSlot;   // 期望配送时间段（delivery_time_slot）
    private Date pickupTime;          // 配送员取件时间
    private Date deliverTime;         // 送达时间
    private Date updateTime;          // 更新时间

    // 非数据库字段
    @TableField(exist = false)
    private String username;          // 用户名（联表）
    @TableField(exist = false)
    private List<OrderItem> items;    // 订单明细（联表）
}
