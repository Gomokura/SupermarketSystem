package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("PURCHASE_ORDERS")
public class PurchaseOrder {
    @TableId(type = IdType.AUTO)
    private Integer poId;
    private Integer supplierId;
    private Integer operatorId;
    private String status;
    private Double totalCost;
    private Date createTime;
    private String supplierName;
    private String operatorName;
}
