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
    private Integer itemId;
    private Integer orderId;
    private Integer productId;
    private Integer skuId;
    private String productName;
    /** v3 列名 sku_name，这里用字段映射 */
    @TableField("SKU_NAME")
    private String skuName;
    private String productImage;
    private Double unitPrice;
    /** 成本价快照（毛利分析） */
    private Double costPrice;
    private Integer quantity;
    private Double subtotal;
    /** 兼容旧前端字段名：specName -> skuName */
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
