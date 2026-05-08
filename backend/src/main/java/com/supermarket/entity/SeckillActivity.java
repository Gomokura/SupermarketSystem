package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
@TableName("ACTIVITIES")
public class SeckillActivity {
    @TableId(value = "ACTIVITY_ID", type = IdType.INPUT)
    @TableField("activity_id")
    private Integer activityId;
    @TableField("activity_name")
    private String name;
    @TableField("activity_type")
    private String activityType;
    @TableField("scope_type")
    private String scopeType;
    @TableField("start_time")
    private Date startTime;
    @TableField("end_time")
    private Date endTime;
    private String status;
    @TableField("create_time")
    private Date createTime;

    // 非数据库字段
    @TableField(exist = false)
    private List<SeckillProduct> products;
}
