package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("POINTS_LOGS")
public class PointsLog {
    @TableId(value = "LOG_ID", type = IdType.INPUT)
    @TableField("log_id")
    private Integer logId;
    @TableField("user_id")
    private Integer userId;
    @TableField("change_amount")
    private Integer changeAmount;
    @TableField("balance_after")
    private Integer balanceAfter;
    private String reason;
    @TableField("ref_id")
    private Integer refId;
    @TableField("operator_id")
    private Integer operatorId;
    @TableField("create_time")
    private Date createTime;
}
