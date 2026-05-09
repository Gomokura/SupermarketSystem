package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("INVENTORY_CHECK_ITEMS")
public class StocktakeItem {
    @TableId(value = "ITEM_ID", type = IdType.INPUT)
    @TableField("item_id")
    private Integer id;
    @TableField("check_id")
    private Integer taskId;
    @TableField("product_id")
    private Integer productId;
    @TableField("system_quantity")
    private Integer bookStock;
    @TableField("actual_quantity")
    private Integer actualStock;
    @TableField("difference")
    private Integer difference;
    @TableField("remark")
    private String diffReason;

    // 非数据库字段
    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String unit;
    @TableField(exist = false)
    private String categoryName;
}
