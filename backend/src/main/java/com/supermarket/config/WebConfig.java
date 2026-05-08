package com.supermarket.config;

import com.supermarket.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/register",
                        "/auth/admin/login",
                        "/auth/courier/login",
                        "/auth/cashier/member",
                        "/init/**",
                        "/products/list",
                        "/products/{productId}",
                        "/products/barcode/**",
                        "/products/recommended",
                        // C 端：秒杀活动查询（不要求登录）
                        "/seckill/activities",
                        "/seckill/activities/**",
                        "/products/categories/tree",
                        "/products/categories/list",
                        "/categories/list",
                        "/banners/list",
                        "/reviews/product/**",
                        "/static/**",
                        "/error",
                        "/auth/debug/**"
                );
    }
}
