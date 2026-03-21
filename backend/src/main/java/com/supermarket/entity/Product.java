package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("PRODUCTS")
public class Product {
    @TableId(type = IdType.AUTO)
    private Integer productId;
    private String productName;
    private Integer categoryId;
    private Double price;
    private Integer stock;
    private String unit;
    private String supplier;
    private String status;
    private Date createTime;
    private String categoryName;
}
