package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("INVENTORY_LOGS")
public class InventoryLog {
    @TableId(value = "LOG_ID", type = IdType.INPUT)
    @TableField("log_id")
    private Integer logId;
    @TableField("product_id")
    private Integer productId;
    @TableField("sku_id")
    private Integer skuId;
    @TableField("change_amount")
    private int changeAmount;
    @TableField("balance_after")
    private int balanceAfter;
    @TableField("log_type")
    private String logType;
    private String remark;
    @TableField("operator_id")
    private Integer operatorId;
    @TableField("ref_id")
    private Integer refId;
    @TableField("create_time")
    private Date createTime;

    // 补充字段（非数据库字段）
    @TableField(exist = false)
    private Integer beforeStock;
    @TableField(exist = false)
    private Integer afterStock;
    @TableField(exist = false)
    private String refNo;

    // 非数据库字段（联表）
    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String operatorName;
}
