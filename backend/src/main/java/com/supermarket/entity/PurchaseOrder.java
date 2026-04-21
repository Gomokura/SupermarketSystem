package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
@TableName("PURCHASE_ORDERS")
public class PurchaseOrder {
    @TableId(type = IdType.AUTO)
    private Integer poId;
    private Integer supplierId;
    private Integer operatorId;   // 创建人（管理员ID）
    private String status;        // draft草稿/approved已审批/received已收货/cancelled已取消
    private String poNo;          // 采购单号，数据库字段 PO_NO
    private Double totalAmount;   // 采购总金额，数据库字段 TOTAL_AMOUNT（原totalCost）
    private String remark;        // 备注
    private Date createTime;
    private Date completeTime;    // 实际到货时间，数据库字段 COMPLETE_TIME

    // 新增字段
    private Date expectedDate;    // 预计到货日期

    // 非数据库字段
    @TableField(exist = false)
    private String supplierName;
    @TableField(exist = false)
    private String operatorName;
    @TableField(exist = false)
    private List<PurchaseOrderItem> items; // 采购明细
}
