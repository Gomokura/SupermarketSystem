package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("PROMOTIONS")
public class Promotion {
    @TableId(type = IdType.AUTO)
    private Integer promotionId;
    private String promoName;
    private String promoType;
    private Double conditionVal;
    private Double discountVal;
    private Date startTime;
    private Date endTime;
    private String status;
}
