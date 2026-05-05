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
    /** 售后单号 */
    private String asNo;
    private Integer orderId;
    private Integer userId;
    /** v3: REFUND / EXCHANGE */
    @TableField("AS_TYPE")
    private String asType;
    private String reason;
    /** 凭证图片URL逗号分隔 */
    private String images;
    /** 退哪个订单商品（NULL=整单退） */
    private Integer itemId;
    private String status;
    private Double refundAmount;
    /** 管理员备注，数据库字段 ADMIN_REMARK */
    @TableField("ADMIN_REMARK")
    private String adminRemark;
    private Integer handlerId;
    private Date handleTime;
    private Date createTime;

    // 非数据库字段
    @TableField(exist = false)
    private String username;
    @TableField(exist = false)
    private String orderNo;
}
