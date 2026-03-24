package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ORDER_ITEMS")
public class OrderItem {
    @TableId(value = "ITEM_ID", type = IdType.AUTO)  // 数据库主键 ITEM_ID
    private Integer itemId;
    private Integer orderId;
    private Integer productId;
    private String productName;   // 商品名快照，数据库字段 PRODUCT_NAME
    private String productImage;  // 商品图快照，数据库字段 PRODUCT_IMAGE
    private Double unitPrice;     // 单价快照，数据库字段 UNIT_PRICE
    private Integer quantity;
    private Double subtotal;
    private Integer skuId;        // SKU ID快照，数据库字段 SKU_ID
    private String specName;      // 规格描述快照，数据库字段 SPEC_NAME
}
