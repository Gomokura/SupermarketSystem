package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.KeySequence;
import lombok.Data;
import java.util.Date;

@Data
@TableName("ADDRESSES")
@KeySequence("SEQ_ADDRESSES")
public class Address {
    @TableId(value = "ADDRESS_ID", type = IdType.INPUT)
    @TableField("address_id")
    private Integer addressId;
    @TableField("user_id")
    private Integer userId;
    @TableField("receiver_name")
    private String receiverName;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    @TableField("is_default")
    private Integer isDefault;
    @TableField("create_time")
    private Date createTime;
}
