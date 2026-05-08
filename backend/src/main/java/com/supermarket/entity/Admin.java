package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.util.Date;

@Data
@TableName("ADMIN_USERS")
public class Admin {
    @TableId(value = "ADMIN_ID", type = IdType.INPUT)
    @TableField("admin_id")
    private Integer adminId;
    private String username;
    @JsonIgnore
    private String password;
    @TableField("real_name")
    private String realName;
    private String role;
    @TableField("phone")
    private String phone;
    private String status;
    @TableField("last_login")
    private Date lastLogin;
    @TableField("create_time")
    private Date createTime;
}
