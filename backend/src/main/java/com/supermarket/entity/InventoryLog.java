package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("INVENTORY_LOGS")
public class InventoryLog {
    @TableId(type = IdType.AUTO)
    private Integer logId;
    private Integer productId;
    private String changeType;
    private Integer quantity;
    private Integer operatorId;
    private String remark;
    private Date logTime;
    private String productName;
    private String operatorName;
}
