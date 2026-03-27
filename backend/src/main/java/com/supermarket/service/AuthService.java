package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.Result;
import com.supermarket.config.JwtConfig;
import com.supermarket.dto.LoginRequest;
import com.supermarket.dto.RegisterRequest;
import com.supermarket.entity.Admin;
import com.supermarket.entity.Courier;
import com.supermarket.entity.User;
import com.supermarket.entity.UserCoupon;
import com.supermarket.mapper.AdminMapper;
import com.supermarket.mapper.CourierMapper;
import com.supermarket.mapper.UserMapper;
import com.supermarket.mapper.UserCouponMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService extends ServiceImpl<UserMapper, User> {

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private CourierMapper courierMapper;

    @Autowired
    private UserCouponMapper userCouponMapper;

    // ==================== C端用户 ====================

    public Result<?> login(LoginRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User user = this.getOne(wrapper);

        if (user == null) {
            return Result.error("用户不存在");
        }

        if ("banned".equals(user.getStatus())) {
            return Result.error("账号已被封禁" + (user.getBanReason() != null ? "：" + user.getBanReason() : ""));
        }

        String md5Password = DigestUtils.md5DigestAsHex(request.getPassword().getBytes(StandardCharsets.UTF_8));
        if (!md5Password.equals(user.getPassword())) {
            return Result.error("密码错误");
        }

        String token = jwtConfig.generateToken(user.getUserId(), user.getUsername(), "user");

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getUserId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("avatarUrl", user.getAvatarUrl());
        data.put("memberLevel", user.getMemberLevel());
        data.put("points", user.getPoints());
        return Result.success(data);
    }

    public Result<?> register(RegisterRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        if (this.getOne(wrapper) != null) {
            return Result.error("用户名已存在");
        }

        // 检查手机号唯一性
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(User::getPhone, request.getPhone());
            if (this.getOne(phoneWrapper) != null) {
                return Result.error("手机号已被注册");
            }
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(DigestUtils.md5DigestAsHex(request.getPassword().getBytes(StandardCharsets.UTF_8)));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        if (request.getNickname() != null && !request.getNickname().isEmpty()) {
            user.setNickname(request.getNickname());
        } else {
            user.setNickname(request.getUsername()); // 默认昵称=用户名
        }
        user.setStatus("active");
        user.setMemberLevel("NORMAL");
        user.setPoints(0);
        user.setCreateTime(new Date());
        this.save(user);
        return Result.success("注册成功");
    }

    public Result<?> getUserInfo(Integer userId) {
        User user = this.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    public Result<?> updateUserInfo(Integer userId, User updateData) {
        User user = this.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 只允许修改以下字段
        if (updateData.getNickname() != null) user.setNickname(updateData.getNickname());
        if (updateData.getAvatarUrl() != null) user.setAvatarUrl(updateData.getAvatarUrl());
        if (updateData.getGender() != null) user.setGender(updateData.getGender());
        if (updateData.getBirthday() != null) user.setBirthday(updateData.getBirthday());
        if (updateData.getEmail() != null) user.setEmail(updateData.getEmail());
        if (updateData.getRealName() != null) user.setRealName(updateData.getRealName());
        user.setUpdateTime(new Date());
        this.updateById(user);
        user.setPassword(null);
        return Result.success(user);
    }

    public Result<?> changePassword(Integer userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        String oldMd5 = DigestUtils.md5DigestAsHex(oldPassword.getBytes(StandardCharsets.UTF_8));
        if (!oldMd5.equals(user.getPassword())) {
            return Result.error("原密码错误");
        }
        user.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes(StandardCharsets.UTF_8)));
        user.setUpdateTime(new Date());
        this.updateById(user);
        return Result.success("密码修改成功");
    }

    // ==================== B端管理员 ====================

    public Result<?> adminLogin(LoginRequest request) {
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getUsername, request.getUsername());
        Admin admin = adminMapper.selectOne(wrapper);

        if (admin == null) {
            return Result.error("管理员不存在");
        }

        String md5Password = DigestUtils.md5DigestAsHex(request.getPassword().getBytes(StandardCharsets.UTF_8));
        if (!md5Password.equals(admin.getPassword())) {
            return Result.error("密码错误");
        }

        if ("disabled".equals(admin.getStatus())) {
            return Result.error("账号已被禁用");
        }

        // 更新最后登录时间
        admin.setLastLogin(new Date());
        adminMapper.updateById(admin);

        String token = jwtConfig.generateToken(admin.getAdminId(), admin.getUsername(), admin.getRole());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("adminId", admin.getAdminId());
        data.put("username", admin.getUsername());
        data.put("realName", admin.getRealName());
        data.put("role", admin.getRole());
        return Result.success(data);
    }

    public Result<?> getAdminInfo(Integer adminId) {
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            return Result.error("管理员不存在");
        }
        admin.setPassword(null);
        return Result.success(admin);
    }

    // ==================== 配送员端 ====================

    public Result<?> courierLogin(LoginRequest request) {
        LambdaQueryWrapper<Courier> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Courier::getPhone, request.getUsername()); // 配送员用手机号登录
        Courier courier = courierMapper.selectOne(wrapper);

        if (courier == null) {
            // 也支持用courierName登录（兜底）
            LambdaQueryWrapper<Courier> nameWrapper = new LambdaQueryWrapper<>();
            nameWrapper.eq(Courier::getCourierName, request.getUsername());
            courier = courierMapper.selectOne(nameWrapper);
        }

        if (courier == null) {
            return Result.error("配送员账号不存在");
        }

        String md5Password = DigestUtils.md5DigestAsHex(request.getPassword().getBytes(StandardCharsets.UTF_8));
        if (!md5Password.equals(courier.getPassword())) {
            return Result.error("密码错误");
        }

        if ("inactive".equals(courier.getStatus())) {
            return Result.error("账号已被禁用");
        }

        String token = jwtConfig.generateToken(courier.getCourierId(), courier.getCourierName(), "courier");

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("courierId", courier.getCourierId());
        data.put("courierName", courier.getCourierName());
        data.put("phone", courier.getPhone());
        data.put("status", courier.getStatus());
        return Result.success(data);
    }

    /**
     * JWT 续期：基于当前已解析的 token claims 重新签发
     * 说明：本项目未实现 token 黑名单，所以 logout 仅返回成功。
     */
    public Result<?> refreshToken(Integer userId, String username, String role) {
        if (userId == null || username == null || role == null) {
            return Result.error("token刷新参数不完整");
        }
        String token = jwtConfig.generateToken(userId, username, role);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", userId);
        data.put("username", username);
        data.put("role", role);
        return Result.success(data);
    }

    /** 收银端按手机号查会员 */
    public Result<?> getMemberByPhone(String phone) {
        if (phone == null || phone.isEmpty()) return Result.error("手机号不能为空");
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        User user = this.getOne(wrapper);
        if (user == null) return Result.error(404, "会员不存在");

        long couponCount = userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, user.getUserId())
                .eq(UserCoupon::getStatus, "unused"));
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getUserId());
        data.put("nickname", user.getNickname());
        data.put("phone", user.getPhone().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
        data.put("memberLevel", user.getMemberLevel());
        data.put("points", user.getPoints());
        data.put("availableCouponCount", couponCount);
        return Result.success(data);
    }
}
