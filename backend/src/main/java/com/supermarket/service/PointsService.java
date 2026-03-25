package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.Result;
import com.supermarket.entity.PointsLog;
import com.supermarket.entity.User;
import com.supermarket.mapper.PointsLogMapper;
import com.supermarket.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PointsService extends ServiceImpl<PointsLogMapper, PointsLog> {

    @Autowired
    private UserMapper userMapper;

    public Result<?> getMyPoints(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) return Result.error("用户不存在");
        return Result.success(user.getPoints());
    }

    public Result<?> getMyPointsLogs(Integer userId, Integer pageNum, Integer pageSize, String reason) {
        Page<PointsLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PointsLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsLog::getUserId, userId)
                .orderByDesc(PointsLog::getCreateTime);
        if (reason != null && !reason.isEmpty()) {
            wrapper.eq(PointsLog::getReason, reason);
        }
        this.page(page, wrapper);
        return Result.success(page);
    }
}

