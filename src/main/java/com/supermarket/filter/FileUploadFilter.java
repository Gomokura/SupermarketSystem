package com.supermarket.filter;

import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 文件上传过滤器
 * 将 multipart/form-data 请求封装为 Commons FileUpload
 */
public class FileUploadFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;

        if (ServletFileUpload.isMultipartContent(req)) {
            // 包装请求，使 getParameter() 在文件上传表单中也能正常工作
            chain.doFilter(new MultipartRequestWrapper(req), servletResponse);
        } else {
            chain.doFilter(req, servletResponse);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
