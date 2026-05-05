package com.supermarket.servlet.product;

import com.supermarket.entity.Result;
import com.supermarket.service.ProductService;
import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * CategoryTreeServlet - 分类树
 * action=categoryTree
 */
public class CategoryTreeServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ProductService productService = new ProductService();
        Result<?> result = productService.getCategoryTree();
        String ajax = req.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(ajax)) {
            if (result.getCode() == 200) json(req, resp, result.getData());
            else jsonError(resp, result.getMessage());
        } else {
            req.setAttribute("categoryTree", result.getData());
            forward(req, resp, "/views/index.jsp");
        }
    }
}
