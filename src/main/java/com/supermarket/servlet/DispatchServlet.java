package com.supermarket.servlet;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * DispatchServlet - 前端控制器
 *
 * 职责：
 * 1. 读取 url.properties，加载 action → Servlet 类名映射
 * 2. 根据请求参数 action 的值，分发到具体的 Servlet 处理
 * 3. 统一捕获异常，返回 JSON 错误信息
 *
 * URL 格式：/context-path/actionName.do?sub=xxx
 * 例如：/login.do
 *      /productList.do
 *      /adminProductAdd.do?method=add
 *
 * 注意：每个子 Servlet 可以定义 public void doAction(HttpServletRequest, HttpServletResponse)
 * 方法来处理具体业务逻辑，DispatchServlet 通过反射调用该方法。
 */
public class DispatchServlet extends HttpServlet {

    private Properties urlMapping = new Properties();
    private WebApplicationContext springContext;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        // 加载 url.properties
        InputStream in = getClass().getClassLoader().getResourceAsStream("url.properties");
        if (in != null) {
            try {
                urlMapping.load(in);
                in.close();
            } catch (IOException e) {
                throw new ServletException("加载 url.properties 失败", e);
            }
        }

        // 获取 Spring 容器
        springContext = WebApplicationContextUtils
                .getWebApplicationContext(config.getServletContext());
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 设置请求编码
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // 获取 action 参数
        String action = req.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            // 默认 action = uri 路径去掉 .do 后缀
            String uri = req.getRequestURI();
            int lastSlash = uri.lastIndexOf('/');
            int lastDot = uri.lastIndexOf('.');
            if (lastSlash >= 0 && lastDot > lastSlash) {
                action = uri.substring(lastSlash + 1, lastDot);
            } else {
                action = "index";
            }
        }

        // 查找对应的 Servlet 类名
        String servletClassName = urlMapping.getProperty(action);

        if (servletClassName == null) {
            BaseServlet bs = new BaseServlet();
            bs.jsonError(resp, 404, "未找到对应处理 action: " + action);
            return;
        }

        try {
            // 获取 Spring 容器中的 Bean（优先），否则反射创建
            Object servlet = null;
            if (springContext != null) {
                try {
                    servlet = springContext.getBean(Class.forName(servletClassName));
                } catch (Exception ignored) {
                    // 容器中没有，则反射
                }
            }
            if (servlet == null) {
                servlet = Class.forName(servletClassName).getDeclaredConstructor().newInstance();
            }

            // 获取子 action（method 参数）
            String methodName = req.getParameter("method");
            if (methodName == null || methodName.trim().isEmpty()) {
                methodName = "doAction";
            }

            // 调用处理方法
            Method method = servlet.getClass().getMethod(methodName,
                    HttpServletRequest.class, HttpServletResponse.class);
            method.invoke(servlet, req, resp);

        } catch (NoSuchMethodException e) {
            // 子 Servlet 没有 doAction 方法，尝试直接调用 doPost
            try {
                Method doPost = servlet.getClass().getMethod("doPost",
                        HttpServletRequest.class, HttpServletResponse.class);
                doPost.invoke(servlet, req, resp);
            } catch (Exception ex) {
                BaseServlet bs = new BaseServlet();
                bs.jsonError(resp, 500, "Servlet 处理方法不存在: " + methodName);
            }
        } catch (Exception e) {
            // 统一异常处理
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            cause.printStackTrace();
            BaseServlet bs = new BaseServlet();
            bs.jsonError(resp, 500, "系统错误: " + cause.getMessage());
        }
    }
}
