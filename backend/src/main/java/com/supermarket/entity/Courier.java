package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.util.Date;

@Data
@TableName("COURIERS")
public class Courier {
    @TableId(type = IdType.AUTO)
    private Integer courierId;
    private String courierName;
    private String phone;
    @JsonIgnore
    private String password;
    private String status;        // online在线 / offline离线
    private Integer isDisabled;   // 0正常/1禁用
    private Integer todayCount;   // 今日已送单数
    private Integer totalCount;   // 累计配送单数
    @TableField("total_delivered")
    private Integer totalDelivered; // 配送成功累计数
    @TableField("total_failed")
    private Integer totalFailed;    // 配送失败累计数
    private Date createTime;
}
