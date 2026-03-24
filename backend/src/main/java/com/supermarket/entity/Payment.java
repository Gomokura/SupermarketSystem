package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("PAYMENT")
public class Payment {
    @TableId(type = IdType.AUTO)
    private Integer paymentId;
    private Integer orderId;
    private Double amount;
    private String status;
    private Date payTime;
}
