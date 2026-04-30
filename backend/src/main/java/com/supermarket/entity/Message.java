package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("MESSAGES")
public class Message {
    @TableId(type = IdType.INPUT)
    @TableField("MESSAGE_ID")
    private Integer messageId;
    private Integer userId;
    private String title;
    private String content;
    /** SYSTEM / ORDER / COUPON / AFTER_SALES */
    private String msgType;
    private Integer refId;
    private Integer isRead;       // 0未读/1已读
    private Date createTime;
}
