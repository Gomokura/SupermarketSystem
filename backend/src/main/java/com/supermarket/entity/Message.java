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
    @TableId(value = "MESSAGE_ID", type = IdType.INPUT)
    @TableField("message_id")
    private Integer messageId;
    @TableField("user_id")
    private Integer userId;
    private String title;
    private String content;
    /** SYSTEM / ORDER / COUPON / AFTER_SALES */
    @TableField("msg_type")
    private String msgType;
    @TableField("ref_id")
    private Integer refId;
    @TableField("is_read")
    private Integer isRead;
    @TableField("create_time")
    private Date createTime;
}
