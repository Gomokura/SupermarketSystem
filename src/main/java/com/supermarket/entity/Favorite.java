package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("FAVORITES")
public class Favorite {
    @TableId(value = "FAV_ID", type = IdType.INPUT)
    private Integer favId;

    private Integer userId;
    private Integer productId;

    private Date createTime;

    // 非数据库字段：联表展示商品信息
    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String coverImage;
    @TableField(exist = false)
    private Double price;
    @TableField(exist = false)
    private Integer stock;
    @TableField(exist = false)
    private String unit;
}

