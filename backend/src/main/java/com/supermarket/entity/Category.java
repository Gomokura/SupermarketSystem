package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.List;

@Data
@TableName("CATEGORIES")
public class Category {
    @TableId(type = IdType.AUTO)
    private Integer categoryId;
    private String categoryName;
    private String description;

    // 新增字段
    private Integer parentId;    // 父分类ID，0=一级分类
    private Integer sortOrder;   // 排序序号
    private String icon;         // 分类图标路径

    // 非数据库字段（树形结构用）
    @TableField(exist = false)
    private List<Category> children; // 子分类列表
}
