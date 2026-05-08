package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("BRANDS")
public class Brand {
    @TableId(value = "BRAND_ID", type = IdType.INPUT)
    @TableField("brand_id")
    private Integer brandId;
    @TableField("brand_name")
    private String brandName;
    @TableField("logo_url")
    private String logoUrl;
    private String description;
    @TableField("product_count")
    private Integer productCount;
    private String status;
    @TableField("create_time")
    private Date createTime;
}
