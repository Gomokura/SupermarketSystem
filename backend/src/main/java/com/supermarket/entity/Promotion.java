package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@KeySequence("SEQ_ACTIVITIES")
@TableName("ACTIVITIES")
public class Promotion {
    @TableId(value = "ACTIVITY_ID", type = IdType.INPUT)
    private Integer promotionId;
    @TableField("ACTIVITY_NAME")
    private String promoName;
    @TableField("ACTIVITY_TYPE")
    private String promoType;
    @TableField(exist = false)
    private Double conditionVal;
    @TableField(exist = false)
    private Double discountVal;
    private Date startTime;
    private Date endTime;
    private String status;
}
