package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
@TableName("SECKILL_ACTIVITIES")
public class SeckillActivity {
    @TableId(type = IdType.AUTO)
    private Integer seckillId;
    private String seckillName;
    private Date startTime;
    private Date endTime;
    /** 状态：pending未开始 / running进行中 / paused已暂停 / ended已结束 */
    private String status;
    private Date createTime;

    // 非数据库字段
    @TableField(exist = false)
    private List<SeckillProduct> products;
}
