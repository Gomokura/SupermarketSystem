package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("ORDER_STATUS_LOGS")
public class OrderStatusLog {
    @TableId(value = "LOG_ID", type = IdType.INPUT)
    @TableField("log_id")
    private Integer logId;
    @TableField("order_id")
    private Integer orderId;
    private String fromStatus;
    private String toStatus;
    @TableField("operator_type")
    private String operatorType;
    @TableField("operator_id")
    private Integer operatorId;
    private String operatorName;
    private String remark;
    @TableField("create_time")
    private Date createTime;
}

