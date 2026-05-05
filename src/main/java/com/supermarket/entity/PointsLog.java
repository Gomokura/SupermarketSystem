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
    /** reason 枚举（数据库 NOT NULL）: ORDER_REWARD/ORDER_DEDUCT/ADMIN_ADJUST/REFUND_ROLLBACK/REGISTER_GIFT */
    private String reason;
    private Integer refId;         // 关联订单ID等
    private Integer operatorId;   // 管理员手动调整的操作人（可为空）
    private Date createTime;
}
