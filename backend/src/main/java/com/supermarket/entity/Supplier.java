package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("SUPPLIERS")
public class Supplier {
    @TableId(type = IdType.AUTO)
    private Integer supplierId;
    private String supplierName;
    private String contact;
    private String phone;
    private String status;
}
