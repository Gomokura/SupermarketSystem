package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("PRODUCT_SKUS")
public class ProductSku {
    @TableId(value = "SKU_ID", type = IdType.INPUT)
    @TableField("sku_id")
    private Integer skuId;
    @TableField("product_id")
    private Integer productId;
    @TableField("sku_name")
    private String skuName;
    @TableField("sku_spec")
    private String skuSpec;
    private Double price;
    @TableField("original_price")
    private Double originalPrice;
    @TableField("cost_price")
    private Double costPrice;
    private Integer stock;
    private String barcode;
    @TableField("sort_order")
    private Integer sortOrder;
    private String status;
}
