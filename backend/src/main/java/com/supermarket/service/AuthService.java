package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.Result;
import com.supermarket.config.JwtConfig;
import com.supermarket.dto.LoginRequest;
import com.supermarket.dto.RegisterRequest;
import com.supermarket.entity.User;
import com.supermarket.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class AuthService extends ServiceImpl<UserMapper, User> {

    @Autowired
    private JwtConfig jwtConfig;

    public Result<?> login(LoginRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User user = this.getOne(wrapper);

        if (user == null) {
            return Result.error("用户不存在");
        }

        String md5Password = DigestUtils.md5DigestAsHex((request.getPassword()).getBytes(StandardCharsets.UTF_8));
        if (!md5Password.equals(user.getPassword())) {
            return Result.error("密码错误");
        }

        if ("冻结".equals(user.getStatus())) {
            return Result.error("账号已被冻结");
        }

        String token = jwtConfig.generateToken(user.getUserId(), user.getUsername(), user.getRole());
        return Result.success(token);
    }

    public Result<?> register(RegisterRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        if (this.getOne(wrapper) != null) {
            return Result.error("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(DigestUtils.md5DigestAsHex(request.getPassword().getBytes(StandardCharsets.UTF_8)));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setRole("user");
        user.setStatus("正常");
        user.setCreateTime(new Date());
        this.save(user);
        return Result.success();
    }

    public Result<?> getUserInfo(Integer userId) {
        User user = this.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }
}
