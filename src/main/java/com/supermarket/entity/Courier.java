package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.util.Date;

@Data
@TableName("DELIVERY_PERSONS")
public class Courier {
    @TableId(type = IdType.INPUT)
    private Integer courierId;
    @TableField("REAL_NAME")
    private String courierName;
    private String phone;
    @JsonIgnore
    private String password;
    private String status;        // active / inactive
    @TableField(exist = false)
    private Integer isDisabled;   // 非数据库字段，兼容旧接口
    @TableField(exist = false)
    private Integer todayCount;   // 非数据库字段
    @TableField("TOTAL_DELIVERY_COUNT")
    private Integer totalCount;
    private Date createTime;
}
