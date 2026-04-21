package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /** 我的消息（支持 unread/read/all） */
    @GetMapping("/my")
    public Result<?> getMyMessages(
            @RequestAttribute Integer userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return messageService.getMyMessages(userId, status, pageNum, pageSize);
    }

    /** 标记已读 */
    @PutMapping("/{msgId}/read")
    public Result<?> markRead(
            @RequestAttribute Integer userId,
            @PathVariable Integer msgId) {
        return messageService.markRead(userId, msgId);
    }

    /** 全部已读 */
    @PutMapping("/read-all")
    public Result<?> markAllRead(@RequestAttribute Integer userId) {
        return messageService.markAllRead(userId);
    }

    /** 未读消息数 */
    @GetMapping("/unread-count")
    public Result<?> unreadCount(@RequestAttribute Integer userId) {
        return messageService.unreadCount(userId);
    }
}

