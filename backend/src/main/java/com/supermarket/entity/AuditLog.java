package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("AUDIT_LOGS")
public class AuditLog {
    @TableId(type = IdType.AUTO)
    private Integer logId;
    private Integer operatorId;
    private String action;
    private String target;
    private String ip;
    private Date createTime;
    private String operatorName;
}
