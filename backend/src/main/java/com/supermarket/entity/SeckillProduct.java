package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("SECKILL_PRODUCTS")
public class SeckillProduct {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer seckillId;
    private Integer productId;
    private Double seckillPrice;  // 秒杀价
    private Integer seckillStock; // 秒杀库存
    private Integer soldCount;    // 已售数量

    // 非数据库字段
    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String imageUrl;
    @TableField(exist = false)
    private Double originalPrice;
}
