package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.dto.LoginRequest;
import com.supermarket.dto.RegisterRequest;
import com.supermarket.entity.User;
import com.supermarket.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // ==================== C端顾客 ====================

    /** 用户登录 */
    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /** 用户注册 */
    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /** 获取当前登录用户信息 */
    @GetMapping("/userinfo")
    public Result<?> getUserInfo(@RequestAttribute Integer userId) {
        return authService.getUserInfo(userId);
    }

    /** 修改用户信息（昵称/头像/性别/生日/邮箱） */
    @PutMapping("/userinfo")
    public Result<?> updateUserInfo(
            @RequestAttribute Integer userId,
            @RequestBody User updateData) {
        return authService.updateUserInfo(userId, updateData);
    }

    /** 修改密码 */
    @PutMapping("/password")
    public Result<?> changePassword(
            @RequestAttribute Integer userId,
            @RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            return Result.error("参数不完整");
        }
        if (newPassword.length() < 6) {
            return Result.error("新密码长度不能少于6位");
        }
        return authService.changePassword(userId, oldPassword, newPassword);
    }

    // ==================== B端管理员 ====================

    /** 管理员登录 */
    @PostMapping("/admin/login")
    public Result<?> adminLogin(@RequestBody LoginRequest request) {
        return authService.adminLogin(request);
    }

    /** 获取管理员自身信息 */
    @GetMapping("/admin/info")
    public Result<?> getAdminInfo(@RequestAttribute Integer userId) {
        return authService.getAdminInfo(userId);
    }

    // ==================== 配送员端 ====================

    /** 配送员登录（手机号+密码） */
    @PostMapping("/courier/login")
    public Result<?> courierLogin(@RequestBody LoginRequest request) {
        return authService.courierLogin(request);
    }

    // ==================== 收银端 ====================

    /** 收银端按手机号查询会员信息 GET /auth/cashier/member?phone=xxx */
    @GetMapping("/cashier/member")
    public Result<?> getMemberByPhone(@RequestParam String phone) {
        return authService.getMemberByPhone(phone);
    }

    // ==================== 通用：JWT 续期/退出 ====================

    /**
     * JWT 续期（需要携带有效 token）
     * POST /auth/refresh
     */
    @PostMapping("/refresh")
    public Result<?> refreshToken(
            @RequestAttribute Integer userId,
            @RequestAttribute String username,
            @RequestAttribute String role) {
        return authService.refreshToken(userId, username, role);
    }

    /**
     * 退出登录
     * POST /auth/logout
     */
    @PostMapping("/logout")
    public Result<?> logout() {
        return Result.success("退出成功");
    }
}
