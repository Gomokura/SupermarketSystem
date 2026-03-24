package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("AFTER_SALES")
public class AfterSale {
    @TableId(value = "AS_ID", type = IdType.AUTO)
    private Integer afterSaleId;
    private Integer orderId;
    private Integer userId;
    /** 类型：refund_only仅退款 / return_refund退货退款，数据库字段 AS_TYPE */
    @TableField("AS_TYPE")
    private String asType;
    private String reason;
    private String status;
    private Double refundAmount;
    /** 管理员备注，数据库字段 ADMIN_REMARK */
    @TableField("ADMIN_REMARK")
    private String adminRemark;
    private Date handleTime;
    private Date createTime;

    // 非数据库字段
    @TableField(exist = false)
    private String username;
    @TableField(exist = false)
    private String orderNo;
}
