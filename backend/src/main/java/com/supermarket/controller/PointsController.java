package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.service.PointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/points")
public class PointsController {

    @Autowired
    private PointsService pointsService;

    /** 我的积分余额 */
    @GetMapping("/my")
    public Result<?> getMyPoints(@RequestAttribute Integer userId) {
        return pointsService.getMyPoints(userId);
    }

    /** 我的积分流水 */
    @GetMapping("/logs")
    public Result<?> getMyPointsLogs(
            @RequestAttribute Integer userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String reason) {
        return pointsService.getMyPointsLogs(userId, pageNum, pageSize, reason);
    }
}

