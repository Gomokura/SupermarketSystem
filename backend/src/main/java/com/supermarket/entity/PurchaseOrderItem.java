package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("PURCHASE_ORDER_ITEMS")
public class PurchaseOrderItem {
    @TableId(type = IdType.INPUT)
    private Integer itemId;
    private Integer poId;
    private Integer productId;
    private Integer orderQuantity;   // 采购数量，数据库字段 ORDER_QUANTITY
    private Integer arrivedQuantity; // 实际收货数量，数据库字段 ARRIVED_QUANTITY
    private Double unitPrice;

    // 非数据库字段
    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String unit;
}
