package com.supermarket.servlet.product;

import com.supermarket.entity.Result;
import com.supermarket.service.ProductService;
import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ProductRecommendedServlet - 推荐商品
 * action=productRecommended
 */
public class ProductRecommendedServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer limit = getInteger(req, "limit", 8);
        ProductService productService = new ProductService();
        Result<?> result = productService.getRecommendedProducts(limit);
        if (result.getCode() == 200) json(req, resp, result.getData());
        else jsonError(resp, result.getMessage());
    }
}
