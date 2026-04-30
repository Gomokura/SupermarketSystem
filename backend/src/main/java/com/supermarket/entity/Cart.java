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
    @TableId(type = IdType.INPUT)
    private Integer cartId;
    private Integer userId;
    private Integer productId;
    private Integer quantity;
    private Date addTime;         // 加入时间

    // 新增字段
    private Integer skuId;        // 选择的SKU ID
    private Integer isChecked;    // 是否勾选参与结算 1/0

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
