package com.supermarket.servlet.user;

import com.supermarket.entity.Result;
import com.supermarket.entity.User;
import com.supermarket.service.AuthService;
import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * UpdateUserServlet - 更新用户信息
 *
 * action=updateUser
 */
public class UpdateUserServlet extends BaseServlet {

    public void doAction(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer userId = getLoginUserId(req);
        if (userId == null) {
            jsonError(resp, "未登录");
            return;
        }

        User updateData = new User();
        updateData.setNickname(getString(req, "nickname"));
        updateData.setAvatarUrl(getString(req, "avatarUrl"));
        updateData.setGender(getString(req, "gender"));
        String birthday = getString(req, "birthday");
        if (birthday != null && !birthday.isEmpty()) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                updateData.setBirthday(sdf.parse(birthday));
            } catch (Exception e) { /* ignore */ }
        }
        updateData.setEmail(getString(req, "email"));
        updateData.setRealName(getString(req, "realName"));
        updateData.setPhone(getString(req, "phone"));

        AuthService authService = new AuthService();
        Result<?> result = authService.updateUserInfo(userId, updateData);

        if (result.getCode() == 200) {
            json(req, resp, result.getData());
        } else {
            jsonError(resp, result.getMessage());
        }
    }
}
