package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("STOCKTAKE_ITEMS")
public class StocktakeItem {
    @TableId(type = IdType.INPUT)
    private Integer id;
    private Integer taskId;
    private Integer productId;
    private Integer bookStock;    // 账面库存（任务创建时快照）
    private Integer actualStock;  // 实盘数量
    private Integer difference;   // 差异=实盘-账面
    private String diffReason;    // 差异原因

    // 非数据库字段
    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String unit;
    @TableField(exist = false)
    private String categoryName;
}
