package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("CART")
public class Cart {
    @TableId(type = IdType.AUTO)
    private Integer cartId;
    private Integer userId;
    private Integer productId;
    private Integer quantity;
    private String productName;
    private Double price;
    private Double subtotal;
}
