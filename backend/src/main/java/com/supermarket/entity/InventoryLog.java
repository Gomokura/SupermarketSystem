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
    @TableId(type = IdType.AUTO)
    private Integer logId;
    private Integer productId;
    /** SKU ID（有多规格时记录 SKU 级变动），对应数据库 SKU_ID */
    private Integer skuId;
    /** 数据库字段 LOG_TYPE，类型：sale_out/purchase_in/manual/stocktake/damage */
    private String logType;
    private Integer changeAmount;  // 数据库字段 CHANGE_AMOUNT（正=增加，负=减少）
    private Integer balanceAfter;  // 数据库字段 BALANCE_AFTER（变动后库存）
    private Integer operatorId;
    private String remark;
    private Integer refId;         // 数据库字段 REF_ID（关联记录ID）
    private Date createTime;       // 数据库字段 CREATE_TIME

    // 补充字段
    private Integer beforeStock;   // 变动前库存
    private Integer afterStock;    // 变动后库存
    private String refNo;          // 关联单号文字

    // 非数据库字段（联表）
    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String operatorName;
}
