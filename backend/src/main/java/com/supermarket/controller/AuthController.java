package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.dto.LoginRequest;
import com.supermarket.dto.RegisterRequest;
import com.supermarket.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @GetMapping("/userinfo")
    public Result<?> getUserInfo(@RequestAttribute Integer userId) {
        return authService.getUserInfo(userId);
    }
}
