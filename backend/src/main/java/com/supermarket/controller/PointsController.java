package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.service.PointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    /** 管理员手动调整积分 POST /points/admin/adjust */
    @PostMapping("/admin/adjust")
    public Result<?> adminAdjust(
            @RequestAttribute Integer adminId,
            @RequestBody Map<String, Object> body) {
        Integer targetUserId = (Integer) body.get("userId");
        Integer amount = (Integer) body.get("amount");
        String remark = (String) body.get("remark");
        return pointsService.adminAdjust(adminId, targetUserId, amount, remark);
    }

    /** 管理员查看指定用户积分流水 GET /points/admin/logs */
    @GetMapping("/admin/logs")
    public Result<?> getUserLogs(
            @RequestParam Integer userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return pointsService.getUserPointsLogs(userId, pageNum, pageSize);
    }
}


