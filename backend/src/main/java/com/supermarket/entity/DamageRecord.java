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
    @TableId(value = "DAMAGE_ID", type = IdType.AUTO)  // 数据库主键名为 DAMAGE_ID
    private Integer damageId;
    private Integer productId;
    private Integer quantity;
    private String reason;        // 破损/过期/其他
    private Integer operatorId;
    private Date createTime;

    // 非数据库字段
    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String operatorName;
}
