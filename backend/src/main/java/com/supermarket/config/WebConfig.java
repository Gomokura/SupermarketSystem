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
                        "/products/list",
                        "/products/{productId}",
                        "/products/barcode/**",
                        "/products/recommended",
                        "/products/categories/tree",
                        "/products/categories/list",
                        "/categories/list",
                        "/banners/list",
                        "/reviews/product/**",
                        "/static/**",
                        "/error"
                );
    }
}
