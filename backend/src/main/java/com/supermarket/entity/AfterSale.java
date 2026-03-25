package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("AFTER_SALES")
public class AfterSale {
    @TableId(value = "after_sale_id", type = IdType.AUTO)
    private Integer afterSaleId;
    private Integer orderId;
    private Integer userId;
    @TableField("after_type")
    private String asType;       // 类型：refund_only / return_refund
    private String reason;
    private String description;   // 详细描述
    private String images;       // 图片逗号分隔
    @TableField("STATUS")
    private String status;
    private Double refundAmount; // 退款金额
    @TableField("ADMIN_REMARK")
    private String adminRemark;  // 管理员备注
    private String rejectReason; // 拒绝原因
    private Integer handlerId;   // 处理人
    @TableField("handle_time")
    private Date handleTime;
    private Date createTime;

    // 非数据库字段
    @TableField(exist = false)
    private String username;
    @TableField(exist = false)
    private String orderNo;
}
