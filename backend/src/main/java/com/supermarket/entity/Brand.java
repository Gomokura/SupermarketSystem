package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("BRANDS")
public class Brand {
    @TableId(type = IdType.INPUT)
    private Integer brandId;
    private String brandName;
    private String logoUrl;
    private String description;
    @TableField(exist = false)
    private Integer sortOrder;
    private String status;        // active / inactive

    @TableField(exist = false)
    private Integer productCount;
}
