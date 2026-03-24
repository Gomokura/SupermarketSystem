package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("POINTS_LOGS")
public class PointsLog {
    @TableId(type = IdType.AUTO)
    private Integer logId;
    private Integer userId;
    private Integer changeAmount;  // 正数=增加，负数=消耗
    private Integer balanceAfter;  // 变动后积分余额
    /** 类型：purchase购物获得 / deduct消费抵扣 / manual手动调整 / refund退款回滚 */
    private String logType;
    private String remark;
    private Integer refId;         // 关联订单ID等
    private Date createTime;
}
