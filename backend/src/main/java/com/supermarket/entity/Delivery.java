package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
    private String status;
    private Date dispatchTime;
    private Date doneTime;
    private String courierName;
}
