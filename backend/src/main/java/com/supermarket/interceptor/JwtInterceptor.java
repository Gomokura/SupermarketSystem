package com.supermarket.interceptor;

import com.supermarket.common.Result;
import com.supermarket.config.JwtConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.io.PrintWriter;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtConfig jwtConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        
        if (!StringUtils.hasText(token) || !token.startsWith("Bearer ")) {
            sendUnauthorizedResponse(response, "未登录或token无效");
            return false;
        }

        token = token.substring(7);
        
        try {
            if (jwtConfig.isTokenExpired(token)) {
                sendUnauthorizedResponse(response, "token已过期");
                return false;
            }

            request.setAttribute("userId", jwtConfig.getUserIdFromToken(token));
            request.setAttribute("username", jwtConfig.getUsernameFromToken(token));
            request.setAttribute("role", jwtConfig.getRoleFromToken(token));
            return true;
        } catch (Exception e) {
            sendUnauthorizedResponse(response, "token解析失败");
            return false;
        }
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print("{\"code\":401,\"message\":\"" + message + "\"}");
        out.flush();
    }
}
