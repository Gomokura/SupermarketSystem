package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 秒杀活动表（数据库：ACTIVITIES）
 * 说明：该表同时包含 SECKILL / FULL_REDUCE，因此通过 activityType=SECKILL 过滤。
 */
@Data
@TableName("ACTIVITIES")
public class SeckillActivityModel {
    @TableId(value = "ACTIVITY_ID", type = IdType.INPUT)
    private Integer seckillId;

    @TableField("ACTIVITY_NAME")
    private String seckillName;

    @TableField("ACTIVITY_TYPE")
    private String activityType; // SECKILL / FULL_REDUCE

    @TableField("STATUS")
    private String status; // active / inactive

    @TableField("START_TIME")
    private Date startTime;

    @TableField("END_TIME")
    private Date endTime;

    @TableField("SECKILL_STOCK")
    private Integer seckillStock;

    private Date createTime;

    // 非数据库字段：返回前端秒杀活动展示状态（pending/running/paused/ended）
    @TableField(exist = false)
    private String currentState;

    @TableField(exist = false)
    private List<SeckillActivityProductModel> products;
}
