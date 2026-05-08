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
    @TableId(value = "COURIER_ID", type = IdType.INPUT)
    @TableField("courier_id")
    private Integer courierId;
    @TableField("real_name")
    private String courierName;
    private String phone;
    @JsonIgnore
    private String password;
    @TableField("total_delivery_count")
    private Integer totalCount;
    private String status;
    private Date createTime;

    // 非数据库字段
    @TableField(exist = false)
    private Integer todayCount;
    @TableField(exist = false)
    private Integer isDisabled;
}
