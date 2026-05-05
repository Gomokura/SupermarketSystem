package com.supermarket.servlet.admin;

import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** FinanceServlet - 财务报表 */
public class FinanceServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("totalOrders", 0);
        data.put("totalSales", 0.0);
        data.put("totalProfit", 0.0);
        json(req, resp, data);
    }
}
