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
    @TableId(type = IdType.AUTO)
    private Integer logId;
    private Integer operatorId;
    private String operatorName;  // 数据库字段 OPERATOR_NAME（直接存储，非联表）
    private String module;        // 操作模块，数据库字段 MODULE
    private String action;        // 操作类型：CREATE/UPDATE/DELETE
    private Integer targetId;     // 操作对象的记录ID，数据库字段 TARGET_ID
    private String beforeData;    // 操作前数据快照（JSON），数据库字段 BEFORE_DATA
    private String afterData;     // 操作后数据快照（JSON），数据库字段 AFTER_DATA
    private String ipAddress;     // IP地址，数据库字段 IP_ADDRESS
    private Date createTime;
}
