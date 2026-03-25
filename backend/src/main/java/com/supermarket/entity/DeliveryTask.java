package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("DELIVERY_TASKS")
public class DeliveryTask {
    @TableId(type = IdType.AUTO)
    private Integer taskId;
    private Integer orderId;
    private Integer courierId;
    /** 状态：pending待取件 / picking配送中 / done已送达 / failed配送失败 */
    private String status;
    private Date assignTime;
    private Date pickupTime;
    @TableField("done_time")
    private Date deliverTime;    // 数据库字段名 done_time
    private String failReason;

    // 非数据库字段
    @TableField(exist = false)
    private String courierName;
    @TableField(exist = false)
    private String courierPhone;
    @TableField(exist = false)
    private String orderNo;
    @TableField(exist = false)
    private String receiverName;
    @TableField(exist = false)
    private String receiverPhone;
    @TableField(exist = false)
    private String address;
}
