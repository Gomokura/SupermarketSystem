package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("MESSAGES")
public class Message {
    @TableId(type = IdType.AUTO)
    private Integer msgId;
    private Integer userId;
    private String title;
    private String content;
    /** 类型：order订单 / promotion促销 / system系统 / refund退款 */
    private String msgType;
    private Integer isRead;       // 0未读/1已读
    private Date createTime;
}
