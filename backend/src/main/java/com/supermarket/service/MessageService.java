package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.BusinessException;
import com.supermarket.common.Result;
import com.supermarket.entity.Message;
import com.supermarket.mapper.MessageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService extends ServiceImpl<MessageMapper, Message> {

    public Result<?> getMyMessages(Integer userId, String status, Integer pageNum, Integer pageSize) {
        Page<Message> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, userId);
        if (status != null && !status.isEmpty()) {
            if ("unread".equalsIgnoreCase(status)) wrapper.eq(Message::getIsRead, 0);
            if ("read".equalsIgnoreCase(status)) wrapper.eq(Message::getIsRead, 1);
        }
        wrapper.orderByDesc(Message::getCreateTime);
        this.page(page, wrapper);
        return Result.success(page);
    }

    @Transactional
    public Result<?> markRead(Integer userId, Integer msgId) {
        Message msg = this.getById(msgId);
        if (msg == null) throw new BusinessException(404, "消息不存在");
        if (!msg.getUserId().equals(userId)) throw new BusinessException(403, "无权操作");
        if (msg.getIsRead() != null && msg.getIsRead() == 1) return Result.success();
        msg.setIsRead(1);
        this.updateById(msg);
        return Result.success();
    }
}

