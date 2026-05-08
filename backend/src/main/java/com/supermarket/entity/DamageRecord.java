package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("DAMAGE_RECORDS")
public class DamageRecord {
    @TableId(value = "DAMAGE_ID", type = IdType.INPUT)
    @TableField("damage_id")
    private Integer damageId;
    @TableField("damage_no")
    private String damageNo;
    @TableField("product_id")
    private Integer productId;
    @TableField("sku_id")
    private Integer skuId;
    private Integer quantity;
    @TableField("unit_cost")
    private Double unitCost;
    @TableField("total_cost")
    private Double totalCost;
    private String reason;
    @TableField("operator_id")
    private Integer operatorId;
    private String status;
    @TableField("create_time")
    private Date createTime;

    // 非数据库字段
    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String operatorName;
}
