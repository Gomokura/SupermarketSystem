package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

/**
 * 秒杀活动商品表（数据库：ACTIVITY_PRODUCTS）
 */
@Data
@TableName("ACTIVITY_PRODUCTS")
public class SeckillActivityProductModel {
    @TableId(value = "ID", type = IdType.INPUT)
    private Integer id;

    @TableField("ACTIVITY_ID")
    private Integer seckillId;

    @TableField("PRODUCT_ID")
    private Integer productId;

    @TableField("SKU_ID")
    private Integer skuId;

    @TableField("ACTIVITY_PRICE")
    private Double seckillPrice;

    @TableField("ACTIVITY_STOCK")
    private Integer seckillStock;

    @TableField("SOLD_COUNT")
    private Integer soldCount;

    // 非数据库字段（用于返回前端展示）
    @TableField(exist = false)
    private String productName;

    @TableField(exist = false)
    private String imageUrl;

    @TableField(exist = false)
    private Double originalPrice;

    @TableField(exist = false)
    private Integer remainingStock; // activity_stock - sold_count
}

