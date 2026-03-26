package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("BRANDS")
public class Brand {
    @TableId(type = IdType.AUTO)
    private Integer brandId;
    private String brandName;
    private String logoUrl;
    private String description;
    private Integer sortOrder;
    private String status;        // active / inactive

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private Integer productCount;
}
