package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("DELIVERY_TASKS")
public class Delivery {
    @TableId(value = "TASK_ID", type = IdType.INPUT)
    @TableField("task_id")
    private Integer deliveryId;
    @TableField("order_id")
    private Integer orderId;
    @TableField("courier_id")
    private Integer courierId;
    @TableField("assign_time")
    private Date dispatchTime;
    private String status;
    private Date pickupTime;
    @TableField("deliver_time")
    private Date doneTime;
    @TableField("fail_reason")
    private String failReason;

    // 非数据库字段
    @TableField(exist = false)
    private String address;
    @TableField(exist = false)
    private String receiver;
    @TableField(exist = false)
    private String phone;
    @TableField(exist = false)
    private String courierName;
    @TableField(exist = false)
    private String courierPhone;
    @TableField(exist = false)
    private String orderNo;
}
