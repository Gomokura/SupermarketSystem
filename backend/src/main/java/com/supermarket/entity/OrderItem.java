package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ORDER_ITEMS")
public class OrderItem {
    @TableId(value = "ITEM_ID", type = IdType.INPUT)
    @TableField("item_id")
    private Integer itemId;
    @TableField("order_id")
    private Integer orderId;
    @TableField("product_id")
    private Integer productId;
    @TableField("sku_id")
    private Integer skuId;
    @TableField("product_name")
    private String productName;
    @TableField("sku_name")
    private String skuName;
    private String productImage;
    @TableField("unit_price")
    private Double unitPrice;
    @TableField("cost_price")
    private Double costPrice;
    private Integer quantity;
    private Double subtotal;

    @TableField(exist = false)
    private String specName;

    public String getSpecName() {
        return specName != null ? specName : skuName;
    }

    public void setSpecName(String specName) {
        this.specName = specName;
        if (this.skuName == null) this.skuName = specName;
    }
}
