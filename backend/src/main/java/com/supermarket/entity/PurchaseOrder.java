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
    @TableId(value = "PO_ID", type = IdType.INPUT)
    @TableField("po_id")
    private Integer poId;
    @TableField("po_no")
    private String poNo;
    @TableField("supplier_id")
    private Integer supplierId;
    @TableField("operator_id")
    private Integer operatorId;
    @TableField("total_amount")
    private Double totalAmount;
    private String status;
    private String remark;
    @TableField("create_time")
    private Date createTime;
    @TableField("approved_time")
    private Date approveTime;
    @TableField("expected_arrive_time")
    private Date expectedDate;
    @TableField("complete_time")
    private Date completeTime;

    // 非数据库字段
    @TableField(exist = false)
    private String supplierName;
    @TableField(exist = false)
    private String operatorName;
    @TableField(exist = false)
    private List<PurchaseOrderItem> items; // 采购明细
}
