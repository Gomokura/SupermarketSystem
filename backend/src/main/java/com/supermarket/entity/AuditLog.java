package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("AUDIT_LOGS")
public class AuditLog {
    @TableId(value = "LOG_ID", type = IdType.INPUT)
    @TableField("log_id")
    private Integer logId;
    @TableField("operator_id")
    private Integer operatorId;
    private String operatorName;
    private String module;
    private String action;
    @TableField("target_id")
    private Integer targetId;
    @TableField("before_data")
    private String beforeData;
    @TableField("after_data")
    private String afterData;
    @TableField("ip_address")
    private String ipAddress;
    @TableField("create_time")
    private Date createTime;
}
