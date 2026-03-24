package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("DELIVERIES")
public class Delivery {
    @TableId(type = IdType.AUTO)
    private Integer deliveryId;
    private Integer orderId;
    private Integer courierId;
    private String address;
    private String receiver;
    private String phone;
    private String status;        // pending待取件/picking配送中/done已送达/failed配送失败
    private Date dispatchTime;    // 发货时间（分配配送员时）
    private Date doneTime;        // 完成时间

    // 新增字段
    private Date pickupTime;      // 配送员取件时间
    private String failReason;    // 配送失败原因

    // 非数据库字段
    @TableField(exist = false)
    private String courierName;   // 配送员姓名（联表）
    @TableField(exist = false)
    private String courierPhone;  // 配送员手机（联表）
}
