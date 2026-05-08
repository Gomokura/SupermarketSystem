package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
@TableName("CATEGORIES")
public class Category {
    @TableId(value = "CATEGORY_ID", type = IdType.INPUT)
    @TableField("category_id")
    private Integer categoryId;
    @TableField("parent_id")
    private Integer parentId;
    @TableField("category_name")
    private String categoryName;
    @TableField("icon_url")
    private String icon;
    @TableField("sort_order")
    private Integer sortOrder;
    private String status;
    private String description;
    @TableField("create_time")
    private Date createTime;

    // 非数据库字段（树形结构用）
    @TableField(exist = false)
    private List<Category> children;
}
