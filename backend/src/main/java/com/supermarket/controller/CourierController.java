package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.service.DeliveryTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/courier")
public class CourierController {

    @Autowired
    private DeliveryTaskService deliveryTaskService;

    /** 骑手端：查看我的配送任务 */
    @GetMapping("/tasks")
    public Result<?> getMyCourierTasks(
            @RequestAttribute Integer userId,
            @RequestParam(required = false) String status) {
        return deliveryTaskService.getMyCourierTasks(userId, status);
    }

    /** 骑手端：取件（开始配送） */
    @PutMapping("/tasks/{taskId}/pickup")
    public Result<?> pickupTask(
            @PathVariable Integer taskId,
            @RequestAttribute Integer userId) {
        return deliveryTaskService.pickupTask(taskId, userId);
    }

    /** 骑手端：完成配送 */
    @PutMapping("/tasks/{taskId}/complete")
    public Result<?> completeTask(
            @PathVariable Integer taskId,
            @RequestAttribute Integer userId) {
        return deliveryTaskService.completeTask(taskId, userId);
    }

    /** 骑手端：配送失败 */
    @PutMapping("/tasks/{taskId}/fail")
    public Result<?> failTask(
            @PathVariable Integer taskId,
            @RequestAttribute Integer userId,
            @RequestBody Map<String, String> body) {
        return deliveryTaskService.failTask(taskId, userId, body.get("failReason"));
    }

    /** 骑手端：更新在线状态 */
    @PutMapping("/status")
    public Result<?> updateOnlineStatus(
            @RequestAttribute Integer userId,
            @RequestParam String status) {
        return deliveryTaskService.updateOnlineStatus(userId, status);
    }
}
