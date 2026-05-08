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
    @TableId(value = "AS_ID", type = IdType.INPUT)
    private Integer afterSaleId;
    @TableField("as_no")
    private String asNo;
    @TableField("order_id")
    private Integer orderId;
    @TableField("item_id")
    private Integer orderItemId;
    @TableField("user_id")
    private Integer userId;
    private String asType;
    private String reason;
    private String images;
    @TableField("item_id")
    private Integer itemId;
    private String status;
    private Double refundAmount;
    @TableField("ADMIN_REMARK")
    private String adminRemark;
    @TableField("handler_id")
    private Integer handlerId;
    @TableField("create_time")
    private Date createTime;
    @TableField("handle_time")
    private Date handleTime;

    // 非数据库字段
    @TableField(exist = false)
    private String username;
    @TableField(exist = false)
    private String orderNo;
}
