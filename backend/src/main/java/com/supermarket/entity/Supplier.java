package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("SUPPLIERS")
public class Supplier {
    @TableId(type = IdType.AUTO)
    private Integer supplierId;
    private String supplierName;
    private String contactName;   // 联系人姓名 → DB列: contact_name
    private String contactPhone;  // 联系电话   → DB列: contact_phone
    private String address;       // 供应商地址 → DB列: address
    private String status;        // active / inactive
    private Date createTime;
}
