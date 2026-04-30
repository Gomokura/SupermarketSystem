package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.util.Date;

@Data
@TableName("ADMIN_USERS")
public class Admin {
    @TableId(type = IdType.INPUT)
    private Integer adminId;
    private String username;
    @JsonIgnore
    private String password;
    private String realName;
    /** 角色：SUPER_ADMIN/MANAGER/PRODUCT/FINANCE/SERVICE/WAREHOUSE/CASHIER */
    private String role;
    private String phone;
    private String status;        // active / inactive
    @TableField(exist = false)
    private Date lastLogin;
    private Date createTime;
}
