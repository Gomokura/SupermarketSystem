package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("CASHIER_RECORD_ITEMS")
public class CashierRecordItem {
    @TableId(value = "ITEM_ID", type = IdType.INPUT)
    @TableField("item_id")
    private Integer itemId;
    @TableField("record_id")
    private Integer recordId;
    @TableField("product_id")
    private Integer productId;
    @TableField("sku_id")
    private Integer skuId;
    @TableField("product_name")
    private String productName;
    @TableField("sku_name")
    private String skuName;
    private Double unitPrice;
    private Integer quantity;
    private Double subtotal;
}
