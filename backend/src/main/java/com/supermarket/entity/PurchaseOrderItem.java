package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("PURCHASE_ORDER_ITEMS")
public class PurchaseOrderItem {
    @TableId(value = "ITEM_ID", type = IdType.INPUT)
    @TableField("item_id")
    private Integer itemId;
    @TableField("po_id")
    private Integer poId;
    @TableField("product_id")
    private Integer productId;
    @TableField("sku_id")
    private Integer skuId;
    @TableField("order_quantity")
    private Integer orderQuantity;
    @TableField("arrived_quantity")
    private Integer arrivedQuantity;
    @TableField("unit_price")
    private Double unitPrice;
    @TableField("subtotal")
    private Double subtotal;

    // 非数据库字段
    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String unit;
}
