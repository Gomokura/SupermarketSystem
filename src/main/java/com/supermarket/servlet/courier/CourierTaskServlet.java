package com.supermarket.servlet.courier;

import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/** CourierTaskServlet - 配送任务列表 */
public class CourierTaskServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer courierId = getLoginCourierId(req);
        if (courierId == null) { jsonError(resp, "请先登录"); return; }

        String ajax = req.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(ajax)) {
            json(req, resp, new ArrayList<>());
        } else {
            forward(req, resp, "/views/courier/tasks.jsp");
        }
    }
}
