package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("ORDER_STATUS_LOGS")
public class OrderStatusLog {
    @TableId(type = IdType.INPUT)
    private Integer logId;
    private Integer orderId;
    private String fromStatus;
    private String toStatus;
    /** USER / ADMIN / SYSTEM / COURIER */
    private String operatorType;
    private Integer operatorId;
    private String operatorName;
    private String remark;
    private Date createTime;
}

