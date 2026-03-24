package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("ORDERS")
public class Order {
    @TableId(type = IdType.AUTO)
    private Integer orderId;
    private Integer userId;
    private Integer addressId;      // 收货地址ID → DB列: address_id
    private Double totalAmount;
    private String orderStatus;
    private String paymentMethod;
    private Date orderTime;
    private Date shipTime;          // 发货时间    → DB列: ship_time
    private Date completeTime;      // 完成时间    → DB列: complete_time
    private Date cancelTime;        // 取消时间    → DB列: cancel_time
    private String username;
}
