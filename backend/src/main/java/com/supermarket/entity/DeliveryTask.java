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
    @TableId(type = IdType.INPUT)
    private Integer taskId;
    private Integer orderId;
    private Integer courierId;
    /** 状态：ASSIGNED待分配 / PICKED_UP已取件 / DELIVERED已送达 / FAILED配送失败 */
    private String status;
    private Date assignTime;
    private Date pickupTime;
    private Date deliverTime;
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
