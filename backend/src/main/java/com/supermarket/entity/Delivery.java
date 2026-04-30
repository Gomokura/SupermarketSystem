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
    @TableId(type = IdType.INPUT)
    @TableField("TASK_ID")
    private Integer deliveryId;
    private Integer orderId;
    private Integer courierId;
    @TableField(exist = false)
    private String address;
    @TableField(exist = false)
    private String receiver;
    @TableField(exist = false)
    private String phone;
    private String status;        // ASSIGNED/PICKED_UP/DELIVERED/FAILED
    @TableField(exist = false)
    private Date dispatchTime;
    @TableField("DELIVER_TIME")
    private Date doneTime;
    private Date pickupTime;
    @TableField("FAIL_REASON")
    private String failReason;

    @TableField(exist = false)
    private String courierName;
    @TableField(exist = false)
    private String courierPhone;
}
