package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("PAYMENTS")
public class Payment {
    @TableId(value = "PAYMENT_ID", type = IdType.INPUT)
    @TableField("payment_id")
    private Integer paymentId;
    @TableField("order_id")
    private Integer orderId;
    private Double amount;
    private String status;
    @TableField("pay_method")
    private String payMethod;
    @TableField("transaction_id")
    private String transactionId;
    @TableField("create_time")
    private Date createTime;
}
