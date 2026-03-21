package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("CATEGORIES")
public class Category {
    @TableId(type = IdType.AUTO)
    private Integer categoryId;
    private String categoryName;
    private String description;
}
