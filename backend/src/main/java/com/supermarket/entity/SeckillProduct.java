package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@TableName("ACTIVITY_PRODUCTS")
public class SeckillProduct {
    @TableId(value = "ID", type = IdType.INPUT)
    @TableField("id")
    private Integer id;
    @TableField("activity_id")
    private Integer activityId;
    @TableField("product_id")
    private Integer productId;
    @TableField("sku_id")
    private Integer skuId;
    @TableField("activity_price")
    private Double seckillPrice;
    @TableField("activity_stock")
    private Integer stock;
    @TableField("sold_count")
    private Integer soldCount;

    // 非数据库字段
    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String imageUrl;
    @TableField(exist = false)
    private Double originalPrice;
}
