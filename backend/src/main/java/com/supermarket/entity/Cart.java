package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.KeySequence;
import lombok.Data;
import java.util.Date;

@Data
@TableName("CART")
@KeySequence("SEQ_CART")
public class Cart {
    @TableId(value = "CART_ID", type = IdType.INPUT)
    @TableField("cart_id")
    private Integer cartId;
    @TableField("user_id")
    private Integer userId;
    @TableField("product_id")
    private Integer productId;
    @TableField("sku_id")
    private Integer skuId;
    @TableField("quantity")
    private Integer quantity;
    @TableField("add_time")
    private Date addTime;
    @TableField("is_checked")
    private Integer isChecked;

    // 非数据库字段（联表用）
    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private Double price;
    @TableField(exist = false)
    private Double subtotal;
    @TableField(exist = false)
    private String imageUrl;      // 商品主图
    @TableField(exist = false)
    private String specName;      // 规格名称
    @TableField(exist = false)
    private Integer stock;        // 当前库存（判断是否失效）
    @TableField(exist = false)
    private String productStatus; // 商品状态（判断是否下架）
}
