package com.supermarket.servlet.product;

import com.supermarket.entity.Result;
import com.supermarket.service.ProductService;
import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ProductSuggestionsServlet - 搜索联想词
 * action=productSuggestions
 */
public class ProductSuggestionsServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String keyword = getString(req, "keyword");
        Integer limit = getInteger(req, "limit", 10);
        ProductService productService = new ProductService();
        Result<?> result = productService.getSearchSuggestions(keyword, limit);
        if (result.getCode() == 200) json(req, resp, result.getData());
        else jsonError(resp, result.getMessage());
    }
}
