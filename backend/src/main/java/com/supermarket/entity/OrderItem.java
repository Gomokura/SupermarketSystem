package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ORDER_ITEMS")
public class OrderItem {
    @TableId(type = IdType.AUTO)
    private Integer orderItemId;
    private Integer orderId;
    private Integer productId;
    private String productName;
    private Integer quantity;
    private Double price;
    private Double subtotal;
}
