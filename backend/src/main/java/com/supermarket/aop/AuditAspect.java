package com.supermarket.aop;

import com.supermarket.common.Result;
import com.supermarket.entity.AuditLog;
import com.supermarket.mapper.AuditLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.extern.slf4j.Slf4j;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Aspect
@Component
public class AuditAspect {

    private static final Pattern LAST_NUMBER_IN_URI = Pattern.compile("(?:/|^)(\\d+)(?:/|$)(?!.*\\d)");

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Around("execution(* com.supermarket.controller..*(..))")
    public Object auditIfNeeded(ProceedingJoinPoint pjp) throws Throwable {
        Object ret = pjp.proceed();

        HttpServletRequest request = getCurrentRequest();
        if (request == null) return ret;

        String method = request.getMethod();
        if (!"POST".equalsIgnoreCase(method) && !"PUT".equalsIgnoreCase(method) && !"DELETE".equalsIgnoreCase(method)) {
            return ret;
        }

        if (!isBWritePath(request)) {
            return ret;
        }

        if (ret instanceof Result<?> result) {
            Integer code = result.getCode();
            if (code == null || code != 200) {
                return ret; // 只记录成功操作
            }
        }

        Integer operatorId = (Integer) request.getAttribute("userId");
        String operatorName = (String) request.getAttribute("username");
        if (operatorId == null || operatorName == null) return ret;

        String uri = request.getRequestURI();
        String module = resolveModule(uri);
        if (module == null) return ret;

        String action = resolveAction(method, uri);

        Integer targetId = extractLastNumber(uri);

        AuditLog log = new AuditLog();
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setModule(module);
        log.setAction(action);
        log.setTargetId(targetId);
        log.setIpAddress(request.getRemoteAddr());
        // before_data/after_data 不做快照（避免侵入业务逻辑）

        try {
            auditLogMapper.insert(log);
        } catch (Exception e) {
            log.warn("审计日志写入失败: {}", e.getMessage());
        }

        return ret;
    }

    private HttpServletRequest getCurrentRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes servletAttrs)) return null;
        return servletAttrs.getRequest();
    }

    private boolean isBWritePath(HttpServletRequest request) {
        String uri = request.getRequestURI();

        // 排除 A 域：订单/履约/库存/售后订单侧/配送任务运行态
        if (uri.startsWith("/orders") || uri.startsWith("/after-sales") || uri.startsWith("/courier/")) return false;
        if (uri.startsWith("/admin/inventory") || uri.startsWith("/admin/deliveries") || uri.startsWith("/admin/purchase-orders")) return false;

        // 仅对 B 域写操作记录
        return uri.startsWith("/products")
                || uri.startsWith("/banners")
                || uri.startsWith("/reviews")
                || uri.startsWith("/coupons")
                || uri.startsWith("/seckill")
                || uri.startsWith("/favorites")
                || uri.startsWith("/cart")
                || uri.startsWith("/addresses")
                || uri.startsWith("/points")
                || uri.startsWith("/messages")
                || uri.startsWith("/admin/users")
                || uri.startsWith("/admin/promotions")
                || uri.startsWith("/admin/suppliers")
                || uri.startsWith("/admin/couriers");
    }

    private String resolveModule(String uri) {
        if (uri == null) return null;
        if (uri.contains("/products/categories") || uri.contains("/categories")) return "CATEGORY";
        if (uri.contains("/products") || uri.contains("/skus")) return "PRODUCT";
        if (uri.contains("/banners")) return "BANNER";
        if (uri.contains("/reviews")) return "REVIEW";
        if (uri.contains("/coupons")) return "COUPON";
        if (uri.contains("/seckill")) return "SECKILL";
        if (uri.contains("/favorites")) return "FAVORITE";
        if (uri.contains("/cart")) return "CART";
        if (uri.contains("/addresses")) return "ADDRESS";
        if (uri.contains("/points")) return "POINTS";
        if (uri.contains("/messages")) return "MESSAGE";
        if (uri.contains("/admin/users")) return "USER";
        if (uri.contains("/admin/couriers")) return "COURIER";
        if (uri.contains("/admin/promotions")) return "PROMOTION";
        if (uri.contains("/admin/suppliers")) return "SUPPLIER";
        return null;
    }

    private String resolveAction(String httpMethod, String uri) {
        if ("POST".equalsIgnoreCase(httpMethod)) return "CREATE";
        if ("DELETE".equalsIgnoreCase(httpMethod)) return "DELETE";
        // PUT
        if (uri != null && (uri.contains("/status") || uri.contains("/toggle") || uri.contains("/hidden") || uri.contains("/read"))) {
            return "STATUS_CHANGE";
        }
        return "UPDATE";
    }

    private Integer extractLastNumber(String uri) {
        if (uri == null) return null;
        Matcher m = LAST_NUMBER_IN_URI.matcher(uri);
        if (!m.find()) return null;
        try {
            return Integer.parseInt(m.group(1));
        } catch (Exception e) {
            return null;
        }
    }
}

