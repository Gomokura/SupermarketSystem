package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("ACTIVITIES")
public class Promotion {
    @TableId(value = "ACTIVITY_ID", type = IdType.INPUT)
    @TableField("activity_id")
    private Integer activityId;
    @TableField("activity_name")
    private String title;
    @TableField("activity_type")
    private String promoType;
    @TableField("scope_type")
    private String scopeType;
    @TableField("scope_category_id")
    private Integer scopeCategoryId;
    @TableField("seckill_stock")
    private Integer seckillStock;
    @TableField("start_time")
    private Date startTime;
    @TableField("end_time")
    private Date endTime;
    private String status;
    @TableField("create_time")
    private Date createTime;
}
