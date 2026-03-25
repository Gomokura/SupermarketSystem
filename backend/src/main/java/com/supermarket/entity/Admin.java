package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.util.Date;

@Data
@TableName("ADMINS")
public class Admin {
    @TableId(value = "admin_id", type = IdType.AUTO)
    private Integer adminId;
    private String username;
    @JsonIgnore
    private String password;
    private String realName;
    /** 角色：super_admin / store_manager / product_staff / finance / customer_service / warehouse */
    private String role;
    private String phone;
    private String status;        // active / disabled
    @TableField("LAST_LOGIN_TIME")
    private Date lastLogin;       // 数据库字段名 LAST_LOGIN_TIME
    private Date createTime;
}
